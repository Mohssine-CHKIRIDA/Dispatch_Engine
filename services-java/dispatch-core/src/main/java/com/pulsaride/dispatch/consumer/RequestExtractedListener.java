package com.pulsaride.dispatch.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsaride.dispatch.embedding.EmbeddingClient;
import com.pulsaride.dispatch.domain.RequestContext;
import com.pulsaride.dispatch.queue.PriorityQueueService;
import com.pulsaride.dispatch.registry.RequestContextStore;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Reads request.extracted events (published by nlp-triage / IA1) and
 * turns each one into a cached case embedding on RequestContext,
 * keyed by request_id - the missing half of the join with
 * UrgencyScoredListener. Whichever of the two events arrives second
 * completes the context and triggers enqueue.
 *
 * Text embedded: raw_text + symptoms, same "give the model more than
 * bare description" principle used for doctor profiles in
 * ProProfileUpdatedListener - so both sides of the cosine similarity
 * are computed the same way, with the same embedding model.
 */
@Component
public class RequestExtractedListener {

    private static final Logger log = LoggerFactory.getLogger(RequestExtractedListener.class);

    @Value("${pulsaride.streams.request-extracted:requests:extracted}")
    private String streamKey;

    @Value("${pulsaride.streams.consumer-group:dispatch-core-group}")
    private String consumerGroup;

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final EmbeddingClient embeddingClient;
    private final RequestContextStore contextStore;
    private final PriorityQueueService priorityQueueService;
    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    public RequestExtractedListener(StringRedisTemplate redis,
            ObjectMapper objectMapper,
            EmbeddingClient embeddingClient,
            RequestContextStore contextStore,
            PriorityQueueService priorityQueueService,
            RedisConnectionFactory connectionFactory) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.embeddingClient = embeddingClient;
        this.contextStore = contextStore;
        this.priorityQueueService = priorityQueueService;
        this.container = StreamMessageListenerContainer.create(
                connectionFactory,
                StreamMessageListenerContainerOptions.builder()
                        .pollTimeout(Duration.ofSeconds(2))
                        .build());
    }

    @PostConstruct
    public void start() {
        ensureGroupExists();

        String consumerName = "dispatch-core-" + UUID.randomUUID().toString().substring(0, 8);

        container.receive(
                Consumer.from(consumerGroup, consumerName),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                this::handle);
        container.start();
        log.info("RequestExtractedListener started, group={} consumer={} stream={}",
                consumerGroup, consumerName, streamKey);
    }

    @PreDestroy
    public void stop() {
        if (container != null) {
            container.stop();
        }
    }

    private void ensureGroupExists() {
        try {
            redis.opsForStream().createGroup(streamKey, ReadOffset.from("0"), consumerGroup);
        } catch (Exception e) {
            Throwable cause = e.getCause();
            boolean isBusyGroup = String.valueOf(e.getMessage()).contains("BUSYGROUP")
                    || (cause != null && String.valueOf(cause.getMessage()).contains("BUSYGROUP"));
            if (!isBusyGroup) {
                throw e;
            }
            log.info("Consumer group {} already exists on stream {} - continuing", consumerGroup, streamKey);
        }
    }

    private void handle(MapRecord<String, String, String> message) {
        try {
            String payloadJson = message.getValue().get("payload");
            JsonNode envelope = objectMapper.readTree(payloadJson);

            JsonNode requestIdNode = envelope.get("request_id");
            JsonNode payload = envelope.get("payload");
            if (requestIdNode == null || payload == null) {
                throw new IllegalArgumentException(
                        "Invalid request.extracted envelope: missing request_id or payload");
            }

            UUID requestId = UUID.fromString(requestIdNode.asText());
            String rawText = payload.path("raw_text").asText("");
            String symptoms = symptomsToText(payload.get("symptoms"));

            String textToEmbed = (symptoms.isBlank() ? rawText : symptoms + ". " + rawText).trim();
            double[] embedding = textToEmbed.isBlank()
                    ? new double[0]
                    : embeddingClient.embed(textToEmbed);

            String specialtyHint = payload.path("specialty_hint").asText(null);
            Optional<RequestContext> context = contextStore.saveExtracted(requestId, embedding, textToEmbed,
                    specialtyHint);
            context.ifPresent(ctx -> priorityQueueService.enqueue(
                    ctx.requestId().toString(), ctx.urgency()));

            redis.opsForStream().acknowledge(streamKey, consumerGroup, message.getId());

            log.info("Stored case embedding for request_id={} dims={}", requestId, embedding.length);

        } catch (Exception e) {
            log.error("Failed to process request.extracted message_id={}", message.getId(), e);
        }
    }

    private String symptomsToText(JsonNode symptomsNode) {
        if (symptomsNode == null || !symptomsNode.isArray()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        symptomsNode.forEach(n -> {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(n.asText());
        });
        return sb.toString();
    }
}