package com.pulsaride.ingestion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record CreateRequestDto(

        @NotBlank(message = "raw_text must not be empty")
        @Size(max = 4000, message = "raw_text must be at most 4000 characters")
        String rawText,

        String requestedSpecialty,

        Double lat,
        Double lng,
        String locationLabel,

        @NotBlank(message = "contact channel is required")
        String contactChannel,

        @NotBlank(message = "contact value is required")
        String contactValue,

        String source
) {
}
