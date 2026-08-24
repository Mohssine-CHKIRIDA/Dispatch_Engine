package com.pulsaride.dispatch.queue;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

/**
 * Implements Step 3 of the matching algorithm doc:
 *   redisScore = timestamp - urgency * 3600
 * Higher urgency => smaller score => nearer the front, since ZPOPMIN
 * always returns the member with the lowest score first.
 */
@Component
public class PriorityQueueService {

    private static final String QUEUE_KEY = "dispatch:queue";
    private static final long URGENCY_WEIGHT_SECONDS = 3600;

    private final StringRedisTemplate redis;

    public PriorityQueueService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void enqueue(String requestId, int urgency) {
        redis.opsForZSet().add(QUEUE_KEY, requestId, score(urgency));
    }

    /** Same scoring, exposed separately so callers can requeue with the original urgency. */
    public void requeue(String requestId, int urgency) {
        enqueue(requestId, urgency);
    }

    /** Pops the single highest-priority request id, or empty if the queue is idle. */
    public Optional<String> pollNext() {
        Set<ZSetOperations.TypedTuple<String>> popped =
                redis.opsForZSet().popMin(QUEUE_KEY, 1);

        if (popped == null || popped.isEmpty()) {
            return Optional.empty();
        }
        return popped.stream().findFirst().map(ZSetOperations.TypedTuple::getValue);
    }

    public Long size() {
        return redis.opsForZSet().zCard(QUEUE_KEY);
    }

    private double score(int urgency) {
        return Instant.now().getEpochSecond() - (double) urgency * URGENCY_WEIGHT_SECONDS;
    }
}
