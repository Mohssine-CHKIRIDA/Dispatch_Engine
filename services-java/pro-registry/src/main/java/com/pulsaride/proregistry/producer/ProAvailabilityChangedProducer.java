package com.pulsaride.proregistry.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsaride.events.ProAvailabilityChanged;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ProAvailabilityChangedProducer {

    private static final Logger log = LoggerFactory.getLogger(ProAvailabilityChangedProducer.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${pulsaride.streams.pro-availability-changed:pro:availability_changed}")
    private String streamKey;

    public ProAvailabilityChangedProducer(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public RecordId publish(ProAvailabilityChanged event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            MapRecord<String, String, String> record = MapRecord.create(
                    streamKey,
                    Map.of(
                            "event_type", event.getEventType().toString(),
                            "pro_id", event.getPayload().getProId(),
                            "payload", payload
                    )
            );

            RecordId id = redisTemplate.opsForStream().add(record);
            log.info("Published pro.availability_changed pro_id={} available={} stream_id={}",
                    event.getPayload().getProId(), event.getPayload().getAvailable(), id);
            return id;

        } catch (Exception e) {
            throw new IllegalStateException("Failed to publish pro.availability_changed event", e);
        }
    }
}
