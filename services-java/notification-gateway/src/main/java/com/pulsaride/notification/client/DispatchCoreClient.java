package com.pulsaride.notification.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Relays a doctor's WebSocket response to dispatch-core's
 * AssignmentController (POST /assignments/{requestId}/accept|refuse).
 * notification-gateway never touches Redis state directly for
 * assignments - dispatch-core stays the single owner of the
 * doctor/request FSM, this service is purely a transport layer.
 */
@Component
public class DispatchCoreClient {

    private static final Logger log = LoggerFactory.getLogger(DispatchCoreClient.class);

    private final RestTemplate restTemplate;

    @Value("${pulsaride.dispatch-core.base-url:http://localhost:9092}")
    private String baseUrl;

    public DispatchCoreClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void accept(String requestId, String doctorId) {
        post("/assignments/" + requestId + "/accept", doctorId);
    }

    public void refuse(String requestId, String doctorId) {
        post("/assignments/" + requestId + "/refuse", doctorId);
    }

    private void post(String path, String doctorId) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + path)
                .queryParam("doctorId", doctorId)
                .toUriString();
        try {
            restTemplate.postForEntity(url, null, Void.class);
        } catch (Exception e) {
            // Doesn't crash the WebSocket connection - the doctor still
            // sees their own click succeed client-side. A production
            // version would retry or alert on repeated failures here.
            log.error("Failed to relay decision to dispatch-core at {}", url, e);
        }
    }
}
