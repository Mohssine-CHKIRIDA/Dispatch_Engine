package com.pulsaride.dispatch.domain;

/**
 * Outcome of one matching attempt for a request. MatchingScheduler
 * decides what to do next based on which case it gets back.
 */
public sealed interface MatchResult {

    record Matched(String doctorId, double compositeScore) implements MatchResult {}

    /** No doctors were available at all (registry empty / all busy). */
    record NoDoctorsAvailable() implements MatchResult {}

    /**
     * There were candidates, but every one of them lost the atomic
     * reservation race (concurrently grabbed by another request)
     * between being scored and being reserved. Caller should requeue.
     */
    record AllCandidatesLost() implements MatchResult {}
}
