package com.pulsaride.proregistry.dto;

import com.pulsaride.proregistry.entity.ProProfile;

import java.time.Instant;
import java.util.UUID;

public record ProProfileResponse(
        UUID id,
        String specialty,
        String description,
        boolean available,
        Integer currentLoad,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProProfileResponse from(ProProfile profile) {
        return new ProProfileResponse(
                profile.getId(),
                profile.getSpecialty(),
                profile.getDescription(),
                profile.isAvailable(),
                profile.getCurrentLoad(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
