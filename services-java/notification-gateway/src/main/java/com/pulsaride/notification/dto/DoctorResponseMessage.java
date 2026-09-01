package com.pulsaride.notification.dto;

/**
 * What the doctor's app sends back over the WebSocket, e.g.:
 *   {"type": "RESPONSE", "requestId": "...", "decision": "ACCEPT"}
 */
public record DoctorResponseMessage(
        String type,       // "RESPONSE"
        String requestId,
        String decision    // "ACCEPT" | "REFUSE"
) {
    public boolean isAccept() {
        return "ACCEPT".equalsIgnoreCase(decision);
    }
}
