package com.pulsaride.dispatch.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Snapshot of a request's triage data, as needed by the matching engine.
 * Built from a request.urgency_score event and cached in Redis
 * (key: "request:ctx:{id}") so the scheduler can re-fetch it after
 * popping the id off the priority queue.
 */
public record RequestContext(
        UUID requestId,
        String summary,
        String specialtyGuess,
        int urgency,
        double[] embedding,
        Instant enqueuedAt
) {
}
