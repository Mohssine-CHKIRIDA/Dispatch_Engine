package com.pulsaride.ingestion.dto;

import java.time.Instant;
import java.util.UUID;

public record RequestAcceptedResponse(
        UUID requestId,
        Instant submittedAt,
        String status
) {
    public static RequestAcceptedResponse accepted(UUID requestId, Instant submittedAt) {
        return new RequestAcceptedResponse(requestId, submittedAt, "ACCEPTED");
    }
}
