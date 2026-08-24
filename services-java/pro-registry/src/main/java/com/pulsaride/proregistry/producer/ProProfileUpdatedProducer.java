package com.pulsaride.proregistry.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsaride.events.ProProfileUpdated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ProProfileUpdatedProducer {

    private static final Logger log = LoggerFactory.getLogger(ProProfileUpdatedProducer.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${pulsaride.streams.pro-profile-updated:pro:profile_updated}")
    private String streamKey;

    public ProProfileUpdatedProducer(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public RecordId publish(ProProfileUpdated event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            MapRecord<String, String, String> record = MapRecord.create(
                    streamKey,
                    Map.of(
                            // .getEventType() is typed Object by jsonschema2pojo
                            // (bare "const" in the schema) - always a String at
                            // runtime, same quirk as RequestSubmitted.
                            "event_type", event.getEventType().toString(),
                            "pro_id", event.getPayload().getProId(),
                            "payload", payload
                    )
            );

            RecordId id = redisTemplate.opsForStream().add(record);
            log.info("Published pro.profile_updated pro_id={} stream_id={}",
                    event.getPayload().getProId(), id);
            return id;

        } catch (Exception e) {
            throw new IllegalStateException("Failed to publish pro.profile_updated event", e);
        }
    }
}
