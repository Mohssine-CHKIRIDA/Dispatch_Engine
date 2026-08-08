package com.pulsaride.ingestion.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsaride.events.RequestSubmitted;
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

    public RecordId publish(RequestSubmitted event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            MapRecord<String, String, String> record = MapRecord.create(
                    streamKey,
                    Map.of(
                            // event.getEventType() is declared as Object by
                            // jsonschema2pojo (a known quirk when a schema
                            // field uses bare "const" instead of
                            // "type": "string" + "const") - it's always
                            // actually a String at runtime here, so
                            // .toString() is safe.
                            "event_type", event.getEventType().toString(),
                            "request_id", event.getRequestId().toString(),
                            "payload", payload));

            RecordId id = redisTemplate.opsForStream().add(record);
            log.info("Published request.submitted request_id={} stream_id={}", event.getRequestId(), id);
            return id;

        } catch (Exception e) {
            throw new IllegalStateException("Failed to publish request.submitted event", e);
        }
    }
}