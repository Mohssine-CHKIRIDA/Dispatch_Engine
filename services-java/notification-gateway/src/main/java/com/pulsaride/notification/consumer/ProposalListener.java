package com.pulsaride.notification.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsaride.notification.dto.ProposalPushMessage;
import com.pulsaride.notification.websocket.DoctorSessionRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Step 9 of the matching algorithm doc ("Notify Doctor - WebSocket"),
 * the receiving end of dispatch-core's ProposalProducer. Same
 * consumer-group pattern as dispatch-core's UrgencyScoredListener.
 */
@Component
public class ProposalListener {

    private static final Logger log = LoggerFactory.getLogger(ProposalListener.class);

    @Value("${pulsaride.streams.request-proposed:requests:proposed}")
    private String streamKey;

    @Value("${pulsaride.streams.consumer-group:notification-gateway-group}")
    private String consumerGroup;

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final DoctorSessionRegistry sessionRegistry;

    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    public ProposalListener(StringRedisTemplate redis,
                             ObjectMapper objectMapper,
                             DoctorSessionRegistry sessionRegistry,
                             RedisConnectionFactory connectionFactory) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.sessionRegistry = sessionRegistry;
        this.container = StreamMessageListenerContainer.create(
                connectionFactory,
                StreamMessageListenerContainerOptions.builder()
                        .pollTimeout(Duration.ofSeconds(2))
                        .build()
        );
    }

    @PostConstruct
    public void start() {
        ensureGroupExists();

        String consumerName = "notification-gateway-" + UUID.randomUUID().toString().substring(0, 8);

        container.receive(
                Consumer.from(consumerGroup, consumerName),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                this::handle
        );
        container.start();
        log.info("ProposalListener started, group={} consumer={} stream={}",
                consumerGroup, consumerName, streamKey);
    }

    @PreDestroy
    public void stop() {
        if (container != null) {
            container.stop();
        }
    }

    private void ensureGroupExists() {
        try {
            redis.opsForStream().createGroup(streamKey, consumerGroup);
        } catch (Exception e) {
            if (!String.valueOf(e.getMessage()).contains("BUSYGROUP")) {
                throw e;
            }
        }
    }

    private void handle(MapRecord<String, String, String> message) {
        try {
            JsonNode payload = objectMapper.readTree(message.getValue().get("payload"));

            String requestId = payload.get("request_id").asText();
            String doctorId = payload.get("doctor_id").asText();
            Double similarity = payload.has("similarity") ? payload.get("similarity").asDouble() : null;
            Double compositeScore = payload.get("composite_score").asDouble();

            ProposalPushMessage pushMessage = ProposalPushMessage.of(requestId, doctorId, similarity, compositeScore);

            Optional<WebSocketSession> sessionOpt = sessionRegistry.get(doctorId);
            if (sessionOpt.isPresent()) {
                sessionOpt.get().sendMessage(new TextMessage(objectMapper.writeValueAsString(pushMessage)));
                log.info("Pushed proposal request_id={} to doctor_id={}", requestId, doctorId);
            } else {
                // Doctor isn't connected right now. No queued/offline
                // delivery in this v0 - see README. dispatch-core's
                // own 30s timeout (once built) is what recovers this,
                // not a retry here.
                log.warn("doctor_id={} not connected, dropping proposal for request_id={}", doctorId, requestId);
            }

            redis.opsForStream().acknowledge(streamKey, consumerGroup, message.getId());

        } catch (Exception e) {
            // Not acked - stays pending for inspection/retry via XPENDING/XCLAIM.
            log.error("Failed to process proposal message_id={}", message.getId(), e);
        }
    }
}
