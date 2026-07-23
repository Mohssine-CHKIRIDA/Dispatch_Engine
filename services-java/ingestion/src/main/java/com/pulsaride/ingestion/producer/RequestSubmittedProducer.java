package com.pulsaride.ingestion.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsaride.ingestion.event.RequestSubmittedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RequestSubmittedProducer {

    private static final Logger log = LoggerFactory.getLogger(RequestSubmittedProducer.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${pulsaride.streams.request-submitted:requests:submitted}")
    private String streamKey;

    public RequestSubmittedProducer(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    
    public RecordId publish(RequestSubmittedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            MapRecord<String, String, String> record = MapRecord.create(
                    streamKey,
                    Map.of(
                            "event_type", event.eventType(),
                            "request_id", event.requestId().toString(),
                            "payload", payload
                    )
            );

            RecordId id = redisTemplate.opsForStream().add(record);
            log.info("Published request.submitted request_id={} stream_id={}", event.requestId(), id);
            return id;

        } catch (Exception e) {
            throw new IllegalStateException("Failed to publish request.submitted event", e);
        }
    }
}
