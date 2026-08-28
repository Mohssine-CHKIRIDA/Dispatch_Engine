package com.pulsaride.dispatch.controller;

import com.pulsaride.dispatch.domain.RequestContext;
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

    public AssignmentController(DoctorRegistry doctorRegistry,
            RequestContextStore contextStore,
            PriorityQueueService priorityQueueService) {
        this.doctorRegistry = doctorRegistry;
        this.contextStore = contextStore;
        this.priorityQueueService = priorityQueueService;
    }

    @PostMapping("/{requestId}/accept")
    public ResponseEntity<Void> accept(@PathVariable String requestId, @RequestParam String doctorId) {
        // Doctor stays reserved (available=0). A real version would
        // move the request to a CLOSED/IN_PROGRESS state in Postgres
        // here for Mission 3 evaluation purposes.

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{requestId}/refuse")
    public ResponseEntity<Void> refuse(@PathVariable String requestId, @RequestParam String doctorId) {
        // "No -> Next Candidate": free the doctor, then requeue the
        // request so MatchingScheduler runs the pipeline again - this
        // time that doctor won't be in the available set.
        doctorRegistry.release(doctorId);

        Optional<RequestContext> ctxOpt = contextStore.get(requestId);
        ctxOpt.ifPresent(ctx -> priorityQueueService.requeue(requestId, ctx.urgency()));

        return ResponseEntity.ok().build();
    }
}
