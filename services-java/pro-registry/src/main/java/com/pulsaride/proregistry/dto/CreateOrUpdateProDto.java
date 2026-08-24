package com.pulsaride.proregistry.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateOrUpdateProDto(

        @NotBlank(message = "specialty must not be empty")
        String specialty,

        @NotBlank(message = "description must not be empty")
        String description
) {
}
