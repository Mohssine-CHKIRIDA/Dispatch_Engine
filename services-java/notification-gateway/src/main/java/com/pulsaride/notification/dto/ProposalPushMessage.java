package com.pulsaride.notification.dto;

/** Mirrors request.proposed's payload - sent verbatim over the WebSocket. */
public record ProposalPushMessage(
        String type,   // always "PROPOSAL"
        String requestId,
        String doctorId,
        Double similarity,
        Double compositeScore,
        String summary  // optional, filled in later once dispatch-core includes it in the event
) {
    public static ProposalPushMessage of(String requestId, String doctorId, Double similarity, Double compositeScore) {
        return new ProposalPushMessage("PROPOSAL", requestId, doctorId, similarity, compositeScore, null);
    }
}
