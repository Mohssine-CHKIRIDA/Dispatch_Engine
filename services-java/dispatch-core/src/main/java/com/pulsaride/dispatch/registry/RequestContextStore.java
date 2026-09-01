package com.pulsaride.dispatch.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsaride.dispatch.domain.RequestContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * The priority queue (a Redis ZSET) only stores request ids - it has
 * nowhere to put the summary/embedding/specialty a request needs to
 * actually be matched. This stores that side data separately, keyed
 * by request id, with a TTL so abandoned entries don't linger forever.
 */
@Component
public class RequestContextStore {

    private static final String KEY_PREFIX = "request:ctx:";
    private static final String PENDING_KEY_PREFIX = "request:pending:";
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

    public Optional<RequestContext> saveExtracted(UUID requestId, double[] embedding, String summary,
            String specialtyGuess) {
        String key = PENDING_KEY_PREFIX + requestId;
        try {
            redis.opsForHash().put(key, "embedding", objectMapper.writeValueAsString(embedding));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize embedding for " + requestId, e);
        }
        redis.opsForHash().put(key, "summary", summary == null ? "" : summary);
        redis.opsForHash().put(key, "specialtyGuess", specialtyGuess == null ? "" : specialtyGuess);
        redis.expire(key, TTL);
        return assemble(requestId, key);
    }

    public Optional<RequestContext> saveUrgency(UUID requestId, int urgency) {
        String key = PENDING_KEY_PREFIX + requestId;
        redis.opsForHash().put(key, "urgency", String.valueOf(urgency));
        redis.expire(key, TTL);
        return assemble(requestId, key);
    }

    private Optional<RequestContext> assemble(UUID requestId, String pendingKey) {
        Map<Object, Object> fields = redis.opsForHash().entries(pendingKey);
        if (!fields.containsKey("embedding") || !fields.containsKey("urgency")) {
            return Optional.empty();
        }

        double[] embedding;
        try {
            embedding = objectMapper.readValue(String.valueOf(fields.get("embedding")), double[].class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize embedding for " + requestId, e);
        }

        RequestContext context = new RequestContext(
                requestId,
                String.valueOf(fields.getOrDefault("summary", "")),
                String.valueOf(fields.getOrDefault("specialtyGuess", "")),
                Integer.parseInt(String.valueOf(fields.get("urgency"))),
                embedding,
                Instant.now());
        save(context);
        redis.delete(pendingKey);
        return Optional.of(context);
    }
}
