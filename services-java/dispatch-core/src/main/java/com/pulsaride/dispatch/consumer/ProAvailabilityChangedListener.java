package com.pulsaride.dispatch.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Bridges pro.availability_changed (published by Pro Registry) into
 * Redis - keeps doctor:{id}'s available/load fields and the
 * doctors:available set (what DoctorRegistry.getAvailableDoctors()
 * actually reads) in sync, without needing Dispatch Core to poll Pro
 * Registry synchronously.
 */
@Component
public class ProAvailabilityChangedListener {

    private static final Logger log = LoggerFactory.getLogger(ProAvailabilityChangedListener.class);

    @Value("${pulsaride.streams.pro-availability-changed:pro:availability_changed}")
    private String streamKey;

    @Value("${pulsaride.streams.consumer-group:dispatch-core-group}")
    private String consumerGroup;

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final DoctorRegistry doctorRegistry;

    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    public ProAvailabilityChangedListener(StringRedisTemplate redis,
                                           ObjectMapper objectMapper,
                                           DoctorRegistry doctorRegistry,
                                           RedisConnectionFactory connectionFactory) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.doctorRegistry = doctorRegistry;
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
        log.info("ProAvailabilityChangedListener started, group={} consumer={} stream={}",
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
            boolean available = payload.get("available").asBoolean();
            Integer currentLoad = payload.hasNonNull("current_load")
                    ? payload.get("current_load").asInt()
                    : null;

            doctorRegistry.updateAvailability(proId, available, currentLoad);

            redis.opsForStream().acknowledge(streamKey, consumerGroup, message.getId());

            log.info("Updated availability for pro_id={} available={} load={}", proId, available, currentLoad);

        } catch (Exception e) {
            log.error("Failed to process pro.availability_changed message_id={}", message.getId(), e);
        }
    }
}
