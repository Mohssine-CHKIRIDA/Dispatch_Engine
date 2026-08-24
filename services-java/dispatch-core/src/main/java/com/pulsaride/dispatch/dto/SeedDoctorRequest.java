package com.pulsaride.dispatch.dto;

import jakarta.validation.constraints.NotBlank;

public record SeedDoctorRequest(
        @NotBlank String id,
        @NotBlank String specialty,
        @NotBlank String profileText, // fed into the embedding client, e.g. "Pediatrician with 15 years experience..."
        boolean available,
        double load,
        double acceptanceRate,
        int experienceYears
) {
}
