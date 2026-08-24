package com.pulsaride.dispatch.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsaride.dispatch.domain.RequestContext;
import com.pulsaride.dispatch.embedding.EmbeddingClient;
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
import java.time.Instant;
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

    @Value("${pulsaride.streams.request-urgency-score:requests:urgency_scored}")
    private String streamKey;

    @Value("${pulsaride.streams.consumer-group:dispatch-core-group}")
    private String consumerGroup;

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final PriorityQueueService priorityQueueService;
    private final RequestContextStore contextStore;
    private final EmbeddingClient embeddingClient;

    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    public UrgencyScoredListener(StringRedisTemplate redis,
                                  ObjectMapper objectMapper,
                                  PriorityQueueService priorityQueueService,
                                  RequestContextStore contextStore,
                                  EmbeddingClient embeddingClient,
                                  RedisConnectionFactory connectionFactory) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.priorityQueueService = priorityQueueService;
        this.contextStore = contextStore;
        this.embeddingClient = embeddingClient;
        this.container = StreamMessageListenerContainer.create(
                connectionFactory,
                StreamMessageListenerContainerOptions.builder()
                        .pollTimeout(Duration.ofSeconds(2))
                        .build()
        );
    }

    @PostConstruct
    public void start() {
        ensureGroupExists();

        String consumerName = "dispatch-core-" + UUID.randomUUID().toString().substring(0, 8);

        container.receive(
                Consumer.from(consumerGroup, consumerName),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                this::handle
        );
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
            redis.opsForStream().createGroup(streamKey, consumerGroup);
        } catch (Exception e) {
            // BUSYGROUP: group already exists - fine on restart.
            if (!String.valueOf(e.getMessage()).contains("BUSYGROUP")) {
                throw e;
            }
        }
    }

    private void handle(MapRecord<String, String, String> message) {
        try {
            String payloadJson = message.getValue().get("payload");
            JsonNode payload = objectMapper.readTree(payloadJson);

            UUID requestId = UUID.fromString(payload.get("request_id").asText());
            int urgency = payload.get("urgency").asInt();
            String summary = payload.get("summary").asText();
            String specialtyGuess = payload.path("specialty_guess").asText("");

            double[] embedding = null;
            if (payload.hasNonNull("embedding")) {
                embedding = objectMapper.convertValue(payload.get("embedding"), double[].class);
            }
            if (embedding == null || embedding.length == 0) {
                embedding = embeddingClient.embed(summary);
            }

            RequestContext ctx = new RequestContext(
                    requestId, summary, specialtyGuess, urgency, embedding, Instant.now());

            contextStore.save(ctx);
            priorityQueueService.enqueue(requestId.toString(), urgency);

            redis.opsForStream().acknowledge(streamKey, consumerGroup, message.getId());

            log.info("Enqueued request_id={} urgency={}", requestId, urgency);

        } catch (Exception e) {
            // Deliberately not acked - stays pending for inspection/retry
            // via XPENDING/XCLAIM, same philosophy as the ingestion/
            // nlp-triage services.
            log.error("Failed to process urgency_score message_id={}", message.getId(), e);
        }
    }
}
