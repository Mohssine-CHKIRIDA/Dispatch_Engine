package com.pulsaride.dispatch.matching;

import com.pulsaride.dispatch.domain.MatchResult;
import com.pulsaride.dispatch.domain.RequestContext;
import com.pulsaride.dispatch.queue.PriorityQueueService;
import com.pulsaride.dispatch.registry.RequestContextStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Drains the priority queue one request at a time and runs it through
 * the MatchingEngine. Kept as a simple fixed-delay poll rather than a
 * tight busy loop - fine for a prototype; a production version would
 * likely use a blocking pop or a dedicated worker thread pool for
 * higher throughput.
 */
@Component
public class MatchingScheduler {

    private static final Logger log = LoggerFactory.getLogger(MatchingScheduler.class);

    private final PriorityQueueService priorityQueueService;
    private final RequestContextStore contextStore;
    private final MatchingEngine matchingEngine;

    public MatchingScheduler(PriorityQueueService priorityQueueService,
                              RequestContextStore contextStore,
                              MatchingEngine matchingEngine) {
        this.priorityQueueService = priorityQueueService;
        this.contextStore = contextStore;
        this.matchingEngine = matchingEngine;
    }

    @Scheduled(fixedDelayString = "${pulsaride.matching.poll-interval-ms:1000}")
    public void pollAndMatch() {
        Optional<String> nextRequestId = priorityQueueService.pollNext();
        if (nextRequestId.isEmpty()) {
            return; // queue empty, nothing to do this tick
        }

        String requestId = nextRequestId.get();
        Optional<RequestContext> ctxOpt = contextStore.get(requestId);

        if (ctxOpt.isEmpty()) {
            log.warn("request_id={} was in the queue but its context expired/was missing - dropping", requestId);
            return;
        }

        RequestContext ctx = ctxOpt.get();
        MatchResult result = matchingEngine.attemptMatch(ctx);

        switch (result) {
            case MatchResult.Matched matched -> log.info(
                    "request_id={} matched to doctor_id={}", requestId, matched.doctorId());

            case MatchResult.NoDoctorsAvailable ignored -> {
                log.info("request_id={} requeued - no doctors available right now", requestId);
                priorityQueueService.requeue(requestId, ctx.urgency());
            }

            case MatchResult.AllCandidatesLost ignored -> {
                log.info("request_id={} requeued - all top candidates lost the reservation race", requestId);
                priorityQueueService.requeue(requestId, ctx.urgency());
            }
        }
    }
}
