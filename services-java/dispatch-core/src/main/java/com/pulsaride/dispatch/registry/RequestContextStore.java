package com.pulsaride.dispatch.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsaride.dispatch.domain.RequestContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * The priority queue (a Redis ZSET) only stores request ids - it has
 * nowhere to put the summary/embedding/specialty a request needs to
 * actually be matched. This stores that side data separately, keyed
 * by request id, with a TTL so abandoned entries don't linger forever.
 */
@Component
public class RequestContextStore {

    private static final String KEY_PREFIX = "request:ctx:";
    private static final Duration TTL = Duration.ofHours(6);

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public RequestContextStore(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    public void save(RequestContext ctx) {
        try {
            String json = objectMapper.writeValueAsString(ctx);
            redis.opsForValue().set(KEY_PREFIX + ctx.requestId(), json, TTL);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize RequestContext " + ctx.requestId(), e);
        }
    }

    public Optional<RequestContext> get(String requestId) {
        String json = redis.opsForValue().get(KEY_PREFIX + requestId);
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, RequestContext.class));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize RequestContext " + requestId, e);
        }
    }
}
