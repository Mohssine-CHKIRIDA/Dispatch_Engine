package com.pulsaride.dispatch.controller;

import com.pulsaride.dispatch.domain.Doctor;
import com.pulsaride.dispatch.dto.SeedDoctorRequest;
import com.pulsaride.dispatch.embedding.EmbeddingClient;
import com.pulsaride.dispatch.registry.DoctorRegistry;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Stands in for pro-registry (not built yet). In the real
 * architecture, pro-registry would own doctor profiles and publish
 * pro.availability_changed events for dispatch-core to consume - for
 * now dispatch-core owns this state directly so the matching pipeline
 * is testable end to end.
 */
@RestController
@RequestMapping("/doctors")
public class DoctorController {

    private final DoctorRegistry doctorRegistry;
    private final EmbeddingClient embeddingClient;

    public DoctorController(DoctorRegistry doctorRegistry, EmbeddingClient embeddingClient) {
        this.doctorRegistry = doctorRegistry;
        this.embeddingClient = embeddingClient;
    }

    @PostMapping
    public ResponseEntity<Void> seed(@Valid @RequestBody SeedDoctorRequest req) {
        double[] embedding = embeddingClient.embed(req.profileText());

        Doctor doctor = new Doctor(
                req.id(),
                req.specialty(),
                req.available(),
                req.load(),
                req.acceptanceRate(),
                req.experienceYears(),
                embedding
        );

        doctorRegistry.upsert(doctor);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/available")
    public ResponseEntity<List<Doctor>> listAvailable() {
        return ResponseEntity.ok(doctorRegistry.getAvailableDoctors());
    }

    @PostMapping("/{doctorId}/release")
    public ResponseEntity<Void> release(@PathVariable String doctorId) {
        doctorRegistry.release(doctorId);
        return ResponseEntity.ok().build();
    }
}
