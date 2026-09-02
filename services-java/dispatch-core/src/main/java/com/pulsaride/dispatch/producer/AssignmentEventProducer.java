package com.pulsaride.dispatch.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class AssignmentEventProducer {

    private static final Logger log = LoggerFactory.getLogger(AssignmentEventProducer.class);

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    @Value("${pulsaride.streams.request-accepted:requests:accepted}")
    private String acceptedStreamKey;

    @Value("${pulsaride.streams.request-refused:requests:refused}")
    private String refusedStreamKey;

    public AssignmentEventProducer(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    public void publishAccepted(String requestId, String doctorId) {
        publish(acceptedStreamKey, "request.accepted", requestId, doctorId);
    }

    public void publishRefused(String requestId, String doctorId) {
        publish(refusedStreamKey, "request.refused", requestId, doctorId);
    }

    private void publish(String streamKey, String eventType, String requestId, String doctorId) {
        try {
            Map<String, Object> event = Map.of(
                    "event_id", UUID.randomUUID().toString(),
                    "request_id", requestId,
                    "event_type", eventType,
                    "version", "1.0",
                    "produced_at", Instant.now().toString(),
                    "producer", "dispatch-core",
                    "payload", Map.of("pro_id", doctorId));

            String payload = objectMapper.writeValueAsString(event);

            MapRecord<String, String, String> record = MapRecord.create(
                    streamKey,
                    Map.of(
                            "event_type", eventType,
                            "request_id", requestId,
                            "payload", payload));

            redis.opsForStream().add(record);
            log.info("Published {} request_id={} doctor_id={}", eventType, requestId, doctorId);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to publish " + eventType + " for " + requestId, e);
        }
    }

    @Value("${pulsaride.streams.request-closed:requests:closed}")
    private String closedStreamKey;

    public void publishClosed(String requestId, String doctorId) {
        publish(closedStreamKey, "request.closed", requestId, doctorId);
    }
}