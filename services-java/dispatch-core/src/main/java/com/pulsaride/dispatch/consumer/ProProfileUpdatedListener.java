package com.pulsaride.dispatch.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsaride.dispatch.embedding.EmbeddingClient;
import com.pulsaride.dispatch.registry.DoctorRegistry;
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
import java.util.UUID;

/**
 * Bridges pro.profile_updated (published by Pro Registry) into the
 * Redis doctor:{id} hashes that DoctorRegistry/matching actually read.
 * Computes the doctor's embedding from their profile description via
 * the embedding service - same model as patient text (see
 * UrgencyScoredListener), which is required for cosine similarity to
 * mean anything.
 *
 * Deliberately does NOT touch availability/load - those come from the
 * separate availability event.
 */
@Component
public class ProProfileUpdatedListener {

    private static final Logger log = LoggerFactory.getLogger(ProProfileUpdatedListener.class);

    @Value("${pulsaride.streams.pro-profile-updated:pro:profile_updated}")
    private String streamKey;

    @Value("${pulsaride.streams.consumer-group:dispatch-core-group}")
    private String consumerGroup;

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final DoctorRegistry doctorRegistry;
    private final EmbeddingClient embeddingClient;

    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    public ProProfileUpdatedListener(StringRedisTemplate redis,
            ObjectMapper objectMapper,
            DoctorRegistry doctorRegistry,
            EmbeddingClient embeddingClient,
            RedisConnectionFactory connectionFactory) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.doctorRegistry = doctorRegistry;
        this.embeddingClient = embeddingClient;
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
        log.info("ProProfileUpdatedListener started, group={} consumer={} stream={}",
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
            JsonNode payload = envelope.get("payload");

            String proId = payload.get("pro_id").asText();
            String specialty = payload.get("specialty").asText();
            String description = payload.get("description").asText();

            // Same text-combination principle as IA1/IA3 elsewhere in
            // this project: give the embedding model more than just the
            // bare description, so the vector actually encodes specialty
            // too, not just prose style.
            String textToEmbed = specialty + ". " + description;
            double[] embedding = embeddingClient.embed(textToEmbed);

            doctorRegistry.updateProfile(proId, specialty, description, embedding);

            redis.opsForStream().acknowledge(streamKey, consumerGroup, message.getId());

            log.info("Updated embedding for pro_id={} specialty={}", proId, specialty);

        } catch (Exception e) {
            log.error("Failed to process pro.profile_updated message_id={}", message.getId(), e);
        }
    }
}
