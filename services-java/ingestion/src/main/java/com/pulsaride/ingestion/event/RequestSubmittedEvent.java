package com.pulsaride.ingestion.event;

import java.time.Instant;
import java.util.UUID;


public record RequestSubmittedEvent(
        UUID eventId,
        String eventType,   
        int eventVersion,   
        Instant occurredAt,

        UUID requestId,
        Instant submittedAt,
        String rawText,
        String requestedSpecialty,

        Double lat,
        Double lng,
        String locationLabel,

        String contactChannel,
        String contactValue,

        String source
) {

    public static RequestSubmittedEvent of(UUID requestId,
                                            Instant submittedAt,
                                            String rawText,
                                            String requestedSpecialty,
                                            Double lat,
                                            Double lng,
                                            String locationLabel,
                                            String contactChannel,
                                            String contactValue,
                                            String source) {
        return new RequestSubmittedEvent(
                UUID.randomUUID(),
                "request.submitted",
                1,
                Instant.now(),
                requestId,
                submittedAt,
                rawText,
                requestedSpecialty,
                lat,
                lng,
                locationLabel,
                contactChannel,
                contactValue,
                source == null ? "web" : source
        );
    }
}
