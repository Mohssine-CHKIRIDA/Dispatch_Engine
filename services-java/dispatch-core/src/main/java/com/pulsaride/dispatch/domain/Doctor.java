package com.pulsaride.dispatch.domain;

/**
 * A doctor's current state as stored in Redis (key: "doctor:{id}", a hash).
 * load/acceptanceRate/experienceYears feed directly into the composite
 * score formula from the matching algorithm doc.
 */
public record Doctor(
        String id,
        String specialty,
        boolean available,
        double load,             // 0.0 (idle) .. 1.0 (fully loaded)
        double acceptanceRate,   // 0.0 .. 1.0
        int experienceYears,
        double[] embedding
) {
    /** Normalizes years of experience into a 0..1 score, capped at 20 years (per the doc's example). */
    public double experienceScore() {
        return Math.min(experienceYears, 20) / 20.0;
    }
}
