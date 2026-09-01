package com.pulsaride.notification.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Doctors connect to ws://host:port/ws/doctor/{doctorId}. No auth in
 * this v0 - anyone who knows a doctor's id can open their socket.
 * Fine for a prototype; a real deployment needs a token exchanged at
 * handshake time instead of a bare id in the URL.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private static final Pattern DOCTOR_ID_PATTERN = Pattern.compile("/ws/doctor/([^/]+)");

    private final DoctorSocketHandler doctorSocketHandler;

    public WebSocketConfig(DoctorSocketHandler doctorSocketHandler) {
        this.doctorSocketHandler = doctorSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(doctorSocketHandler, "/ws/doctor/{doctorId}")
                .addInterceptors(new DoctorIdHandshakeInterceptor())
                .setAllowedOrigins("*"); // prototype only - tighten before any real deployment
    }

    static class DoctorIdHandshakeInterceptor implements HandshakeInterceptor {

        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                        WebSocketHandler wsHandler, Map<String, Object> attributes) {
            Matcher matcher = DOCTOR_ID_PATTERN.matcher(request.getURI().getPath());
            if (matcher.find()) {
                attributes.put("doctorId", matcher.group(1));
                return true;
            }
            // No doctorId in the path - reject the handshake outright
            // rather than accepting an unattributable connection.
            return false;
        }

        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Exception exception) {
            // no-op
        }
    }
}
