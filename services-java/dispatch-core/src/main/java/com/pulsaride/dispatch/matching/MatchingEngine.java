package com.pulsaride.dispatch.matching;

import com.pulsaride.dispatch.domain.*;
import com.pulsaride.dispatch.embedding.EmbeddingClient;
import com.pulsaride.dispatch.producer.ProposalProducer;
import com.pulsaride.dispatch.registry.DoctorRegistry;
import com.pulsaride.dispatch.scoring.CompositeScoreCalculator;
import com.pulsaride.dispatch.scoring.SimilarityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Implements steps 4-9 of the matching algorithm doc, in order:
 *   4. Retrieve available doctors
 *   5. Semantic similarity search
 *   6. Top-K selection
 *   7. Composite score calculation
 *   8. Atomic reservation (falls through to the next candidate on conflict)
 *   9. Notify doctor (publishes request.proposed; notification-gateway
 *      owns the actual WebSocket push)
 *
 * "Everything before the Composite Score is used to reduce the search
 * space. Everything after it is simply choosing the winner." - the doc.
 */
@Component
public class MatchingEngine {

    private static final Logger log = LoggerFactory.getLogger(MatchingEngine.class);

    private final DoctorRegistry doctorRegistry;
    private final EmbeddingClient embeddingClient;
    private final CompositeScoreCalculator scoreCalculator;
    private final ProposalProducer proposalProducer;

    @Value("${pulsaride.matching.top-k:3}")
    private int topK;

    public MatchingEngine(DoctorRegistry doctorRegistry,
                           EmbeddingClient embeddingClient,
                           CompositeScoreCalculator scoreCalculator,
                           ProposalProducer proposalProducer) {
        this.doctorRegistry = doctorRegistry;
        this.embeddingClient = embeddingClient;
        this.scoreCalculator = scoreCalculator;
        this.proposalProducer = proposalProducer;
    }

    public MatchResult attemptMatch(RequestContext ctx) {
        // Step 4
        List<Doctor> available = doctorRegistry.getAvailableDoctors();
        if (available.isEmpty()) {
            log.info("No doctors available for request_id={}", ctx.requestId());
            return new MatchResult.NoDoctorsAvailable();
        }

        double[] requestEmbedding = resolveEmbedding(ctx);

        // Step 5
        List<MatchCandidate> bySimilarity = available.stream()
                .map(doc -> new MatchCandidate(
                        doc,
                        SimilarityUtil.cosineSimilarity(requestEmbedding, doc.embedding()),
                        0.0 // composite score computed after Top-K, per the doc's ordering
                ))
                .sorted(Comparator.comparingDouble(MatchCandidate::similarity).reversed())
                .toList();

        // Step 6
        List<MatchCandidate> topCandidates = bySimilarity.stream().limit(topK).toList();

        // Step 7
        List<MatchCandidate> scored = topCandidates.stream()
                .map(c -> new MatchCandidate(
                        c.doctor(),
                        c.similarity(),
                        scoreCalculator.score(c.doctor(), c.similarity())))
                .sorted(Comparator.comparingDouble(MatchCandidate::compositeScore).reversed())
                .toList();

        log.debug("request_id={} scored candidates={}", ctx.requestId(), scored);

        // Steps 8-9: try candidates best-first, atomically reserving;
        // on a lost race (WATCH conflict), fall through to the next one.
        for (MatchCandidate candidate : scored) {
            boolean reserved = doctorRegistry.tryReserve(candidate.doctor().id(), ctx.requestId().toString());
            if (reserved) {
                proposalProducer.publish(ctx.requestId(), candidate.doctor().id(),
                        candidate.similarity(), candidate.compositeScore());
                log.info("Matched request_id={} -> doctor_id={} composite_score={}",
                        ctx.requestId(), candidate.doctor().id(), candidate.compositeScore());
                return new MatchResult.Matched(candidate.doctor().id(), candidate.compositeScore());
            }
            log.info("Lost reservation race for doctor_id={} on request_id={}, trying next candidate",
                    candidate.doctor().id(), ctx.requestId());
        }

        return new MatchResult.AllCandidatesLost();
    }

    private double[] resolveEmbedding(RequestContext ctx) {
        if (ctx.embedding() != null && ctx.embedding().length > 0) {
            return ctx.embedding();
        }
        // upstream (urgency-scoring) didn't precompute one - do it here
        return embeddingClient.embed(ctx.summary());
    }
}
