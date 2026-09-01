package com.pulsaride.notification.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A doctor may have at most one active connection (their phone/app).
 * If dispatch-core proposes a request to a doctor with no live
 * session here (offline), ProposalListener just logs it - there's no
 * queued/retry delivery in this v0 (see README "What's NOT here yet").
 */
@Component
public class DoctorSessionRegistry {

    private static final Logger log = LoggerFactory.getLogger(DoctorSessionRegistry.class);

    private final Map<String, WebSocketSession> sessionsByDoctorId = new ConcurrentHashMap<>();

    public void register(String doctorId, WebSocketSession session) {
        WebSocketSession previous = sessionsByDoctorId.put(doctorId, session);
        if (previous != null && previous.isOpen() && !previous.getId().equals(session.getId())) {
            log.info("doctor_id={} reconnected, replacing previous session", doctorId);
        }
    }

    public void unregister(String doctorId, WebSocketSession session) {
        // Only remove if it's still this exact session - avoids a race
        // where an old session's close event evicts a newer connection.
        sessionsByDoctorId.computeIfPresent(doctorId, (id, current) ->
                current.getId().equals(session.getId()) ? null : current);
    }

    public Optional<WebSocketSession> get(String doctorId) {
        WebSocketSession session = sessionsByDoctorId.get(doctorId);
        return (session != null && session.isOpen()) ? Optional.of(session) : Optional.empty();
    }

    public int connectedCount() {
        return sessionsByDoctorId.size();
    }
}
