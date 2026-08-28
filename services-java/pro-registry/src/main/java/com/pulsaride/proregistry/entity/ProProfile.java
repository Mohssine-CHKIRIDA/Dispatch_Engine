package com.pulsaride.proregistry.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pro_profiles")
public class ProProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String specialty;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean available;

    @Column(name = "current_load")
    private Integer currentLoad;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ProProfile() {
        // JPA
    }

    public ProProfile(UUID id, String specialty, String description, boolean available, Integer currentLoad) {
        this.id = id;
        this.specialty = specialty;
        this.description = description;
        this.available = available;
        this.currentLoad = currentLoad;
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public String getSpecialty() {
        return specialty;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAvailable() {
        return available;
    }

    public Integer getCurrentLoad() {
        return currentLoad;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void updateProfile(String specialty, String description) {
        this.specialty = specialty;
        this.description = description;
        this.updatedAt = Instant.now();
    }

    public void updateAvailability(boolean available, Integer currentLoad) {
        this.available = available;
        this.currentLoad = currentLoad;
        this.updatedAt = Instant.now();
    }
}
