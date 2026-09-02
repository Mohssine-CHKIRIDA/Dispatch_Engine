package com.pulsaride.dispatch.matching;

import com.pulsaride.dispatch.domain.RequestContext;
import com.pulsaride.dispatch.queue.PriorityQueueService;
import com.pulsaride.dispatch.registry.DoctorRegistry;
import com.pulsaride.dispatch.registry.RequestContextStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * The missing half of Step 9's "Accepted? Yes/No" branch: if a doctor
 * never responds within the timeout, free them and requeue the
 * request so MatchingEngine tries the next candidate. Without this,
 * a silently-dropped WebSocket proposal (doctor offline) or an
 * ignored one leaves that doctor permanently reserved for nothing.
 */
@Component
public class ReservationTimeoutScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReservationTimeoutScheduler.class);

    private final DoctorRegistry doctorRegistry;
    private final RequestContextStore contextStore;
    private final PriorityQueueService priorityQueueService;

    @Value("${pulsaride.matching.reservation-timeout-seconds:30}")
    private long timeoutSeconds;

    public ReservationTimeoutScheduler(DoctorRegistry doctorRegistry,
            RequestContextStore contextStore,
            PriorityQueueService priorityQueueService) {
        this.doctorRegistry = doctorRegistry;
        this.contextStore = contextStore;
        this.priorityQueueService = priorityQueueService;
    }

    @Scheduled(fixedDelayString = "${pulsaride.matching.timeout-sweep-interval-ms:5000}")
    public void sweep() {
        List<DoctorRegistry.ReservedDoctor> expired = doctorRegistry.getExpiredReservations(timeoutSeconds * 1000);

        for (DoctorRegistry.ReservedDoctor reservation : expired) {
            log.info("Reservation timed out doctor_id={} request_id={} (>{}s), releasing and requeueing",
                    reservation.doctorId(), reservation.requestId(), timeoutSeconds);

            doctorRegistry.release(reservation.doctorId());

            contextStore.get(reservation.requestId()).ifPresentOrElse(
                    ctx -> priorityQueueService.requeue(reservation.requestId(), ctx.urgency()),
                    () -> log.warn("No RequestContext found for timed-out request_id={}, cannot requeue",
                            reservation.requestId()));
        }
    }
}