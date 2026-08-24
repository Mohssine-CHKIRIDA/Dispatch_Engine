package com.pulsaride.dispatch.domain;

/** A doctor scored against one specific request, at some stage of the pipeline. */
public record MatchCandidate(Doctor doctor, double similarity, double compositeScore) {
}
