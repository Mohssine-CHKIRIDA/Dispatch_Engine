package com.pulsaride.dispatch.controller;

import com.pulsaride.dispatch.domain.RequestContext;
import com.pulsaride.dispatch.producer.AssignmentEventProducer;
import com.pulsaride.dispatch.queue.PriorityQueueService;
import com.pulsaride.dispatch.registry.DoctorRegistry;
import com.pulsaride.dispatch.registry.RequestContextStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Handles Step 9's "Accepted? Yes/No" branch. In the target
 * architecture, a doctor's response arrives over WebSocket via
 * notification-gateway, which would call something equivalent to
 * this internally. For now these are plain REST endpoints so the
 * accept/refuse branch can be tested directly.
 */
@RestController
@RequestMapping("/assignments")
public class AssignmentController {

    private final DoctorRegistry doctorRegistry;
    private final RequestContextStore contextStore;
    private final PriorityQueueService priorityQueueService;
    private final AssignmentEventProducer eventProducer;

    public AssignmentController(DoctorRegistry doctorRegistry,
            RequestContextStore contextStore,
            PriorityQueueService priorityQueueService,
            AssignmentEventProducer eventProducer) {
        this.doctorRegistry = doctorRegistry;
        this.contextStore = contextStore;
        this.priorityQueueService = priorityQueueService;
        this.eventProducer = eventProducer;
    }

    @PostMapping("/{requestId}/accept")
    public ResponseEntity<Void> accept(@PathVariable String requestId, @RequestParam String doctorId) {
        doctorRegistry.confirmReservation(doctorId);
        eventProducer.publishAccepted(requestId, doctorId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{requestId}/refuse")
    public ResponseEntity<Void> refuse(@PathVariable String requestId, @RequestParam String doctorId) {
        doctorRegistry.release(doctorId);
        eventProducer.publishRefused(requestId, doctorId);

        Optional<RequestContext> ctxOpt = contextStore.get(requestId);
        ctxOpt.ifPresent(ctx -> priorityQueueService.requeue(requestId, ctx.urgency()));

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{requestId}/close")
    public ResponseEntity<Void> close(@PathVariable String requestId, @RequestParam String doctorId) {
        // Consultation finished (or was cancelled post-accept) - free the
        // doctor back into the available pool. Unlike accept/refuse, this
        // does NOT requeue the request - it's terminal, not a retry.
        doctorRegistry.release(doctorId);
        eventProducer.publishClosed(requestId, doctorId);
        return ResponseEntity.ok().build();
    }
}
