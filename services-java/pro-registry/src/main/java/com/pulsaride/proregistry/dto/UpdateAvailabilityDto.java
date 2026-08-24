package com.pulsaride.proregistry.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateAvailabilityDto(

        @NotNull(message = "available must be specified")
        Boolean available,

        @PositiveOrZero(message = "current_load must be zero or positive")
        Integer currentLoad
) {
}
