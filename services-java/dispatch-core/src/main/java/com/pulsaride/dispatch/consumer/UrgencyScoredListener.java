package com.pulsaride.dispatch.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Reads request.urgency_score events (published by urgency-scoring)
 * and turns each one into: (1) a cached RequestContext, and (2) an
 * entry in the priority queue. This is the bridge between "a request
 * has been triaged" and "the matching engine will consider it".
 *
 * Uses Spring Data Redis's StreamMessageListenerContainer rather than
 * a hand-rolled polling loop (the approach used on the Python side for
 * nlp-triage) - it's the idiomatic way to run a Redis Streams consumer
 * group in a Spring app, and it manages its own polling thread.
 */
@Component
public class UrgencyScoredListener {

    private static final Logger log = LoggerFactory.getLogger(UrgencyScoredListener.class);

    @Value("${pulsaride.streams.request-urgency-score:requests:urgency_score}")
    private String streamKey;

    @Value("${pulsaride.streams.consumer-group:dispatch-core-group}")
    private String consumerGroup;

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final PriorityQueueService priorityQueueService;
    private final RequestContextStore contextStore;
    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    public UrgencyScoredListener(StringRedisTemplate redis,
            ObjectMapper objectMapper,
            PriorityQueueService priorityQueueService,
            RequestContextStore contextStore,
            RedisConnectionFactory connectionFactory) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.priorityQueueService = priorityQueueService;
        this.contextStore = contextStore;
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
        log.info("UrgencyScoredListener started, group={} consumer={} stream={}",
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
            // BUSYGROUP: group already exists - fine on restart. The actual
            // "BUSYGROUP" text lives on e.getCause() (Lettuce's
            // RedisBusyException) - Spring wraps it in RedisSystemException
            // whose own getMessage() is just "Error in execution", so
            // checking e.getMessage() alone never matches.
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
            JsonNode event = objectMapper.readTree(payloadJson);

            JsonNode requestIdNode = event.get("request_id");
            JsonNode payload = event.get("payload");
            if (requestIdNode == null || payload == null || payload.get("score") == null) {
                throw new IllegalArgumentException(
                        "Invalid request.urgency_score envelope: missing request_id or payload.score");
            }

            UUID requestId = UUID.fromString(requestIdNode.asText());
            int urgency = payload.get("score").asInt();
            Optional<RequestContext> context = contextStore.saveUrgency(requestId, urgency);
            context.ifPresent(ctx -> priorityQueueService.enqueue(
                    ctx.requestId().toString(), ctx.urgency()));

            redis.opsForStream().acknowledge(streamKey, consumerGroup, message.getId());

            log.info("Received urgency score request_id={} score={}", requestId, urgency);

        } catch (Exception e) {
            // Deliberately not acked - stays pending for inspection/retry
            // via XPENDING/XCLAIM, same philosophy as the ingestion/
            // nlp-triage services.
            log.error("Failed to process urgency_score message_id={}", message.getId(), e);
        }
    }
}
