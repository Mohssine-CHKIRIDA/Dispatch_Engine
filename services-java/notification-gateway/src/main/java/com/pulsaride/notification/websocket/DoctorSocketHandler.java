package com.pulsaride.notification.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsaride.notification.client.DispatchCoreClient;
import com.pulsaride.notification.dto.DoctorResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * One connection per doctor, at /ws/doctor/{doctorId} (see
 * WebSocketConfig). Two responsibilities:
 *   1. On connect/disconnect: register/unregister in DoctorSessionRegistry
 *      so ProposalListener knows who's currently reachable.
 *   2. On incoming message: parse the doctor's ACCEPT/REFUSE decision
 *      and relay it to dispatch-core via DispatchCoreClient - this is
 *      the "Accepted? Yes/No" branch from the matching algorithm doc,
 *      arriving over the wire instead of a REST call from a test client.
 */
@Component
public class DoctorSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(DoctorSocketHandler.class);
    private static final String DOCTOR_ID_ATTR = "doctorId";

    private final DoctorSessionRegistry sessionRegistry;
    private final DispatchCoreClient dispatchCoreClient;
    private final ObjectMapper objectMapper;

    public DoctorSocketHandler(DoctorSessionRegistry sessionRegistry,
                                DispatchCoreClient dispatchCoreClient,
                                ObjectMapper objectMapper) {
        this.sessionRegistry = sessionRegistry;
        this.dispatchCoreClient = dispatchCoreClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String doctorId = doctorIdOf(session);
        sessionRegistry.register(doctorId, session);
        log.info("doctor_id={} connected (session={}), {} total connected",
                doctorId, session.getId(), sessionRegistry.connectedCount());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String doctorId = doctorIdOf(session);
        sessionRegistry.unregister(doctorId, session);
        log.info("doctor_id={} disconnected (session={}, status={})", doctorId, session.getId(), status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String doctorId = doctorIdOf(session);
        try {
            DoctorResponseMessage response = objectMapper.readValue(message.getPayload(), DoctorResponseMessage.class);

            if (response.isAccept()) {
                dispatchCoreClient.accept(response.requestId(), doctorId);
            } else {
                dispatchCoreClient.refuse(response.requestId(), doctorId);
            }

            log.info("doctor_id={} decision={} request_id={}", doctorId, response.decision(), response.requestId());

        } catch (Exception e) {
            log.error("Failed to parse/handle message from doctor_id={}: {}", doctorId, message.getPayload(), e);
        }
    }

    /** doctorId is stuffed into session attributes by WebSocketConfig's HandshakeInterceptor. */
    private String doctorIdOf(WebSocketSession session) {
        return String.valueOf(session.getAttributes().get(DOCTOR_ID_ATTR));
    }
}
