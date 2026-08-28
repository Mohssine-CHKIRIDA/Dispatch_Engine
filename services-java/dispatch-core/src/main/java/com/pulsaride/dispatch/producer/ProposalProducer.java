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

/**
 * Step 9: "Notify Doctor (WebSocket)". dispatch-core doesn't own the
 * WebSocket connection itself - that's notification-gateway's job.
 * This just publishes the fact that a proposal was made; the gateway
 * service (not built yet) would consume this stream and push to the
 * doctor's live connection.
 */
@Component
public class ProposalProducer {

    private static final Logger log = LoggerFactory.getLogger(ProposalProducer.class);

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    @Value("${pulsaride.streams.request-proposed:requests:proposed}")
    private String streamKey;

    public ProposalProducer(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    public void publish(UUID requestId, String doctorId, double compositeScore) {
        try {
            Map<String, Object> event = Map.of(
                    "event_id", UUID.randomUUID().toString(),
                    "request_id", requestId.toString(),
                    "event_type", "request.proposed",
                    "version", "1.0",
                    "produced_at", Instant.now().toString(),
                    "producer", "dispatch-core",
                    "payload", Map.of(
                            "pro_id", doctorId,
                            "strategy", "S3",
                            "match_score", compositeScore));

            String payload = objectMapper.writeValueAsString(event);

            MapRecord<String, String, String> record = MapRecord.create(
                    streamKey,
                    Map.of(
                            "event_type", "request.proposed",
                            "request_id", requestId.toString(),
                            "pro_id", doctorId,
                            "payload", payload));

            redis.opsForStream().add(record);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to publish request.proposed for " + requestId, e);
        }
    }
}
