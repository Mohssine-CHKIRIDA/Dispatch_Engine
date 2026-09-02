package com.pulsaride.proregistry.controller;

import com.pulsaride.events.AvailabilityChangedPayload;
import com.pulsaride.events.ProAvailabilityChanged;
import com.pulsaride.events.ProProfileUpdated;
import com.pulsaride.events.ProfileUpdatedPayload;
import com.pulsaride.proregistry.dto.CreateOrUpdateProDto;
import com.pulsaride.proregistry.dto.ProProfileResponse;
import com.pulsaride.proregistry.dto.UpdateAvailabilityDto;
import com.pulsaride.proregistry.entity.ProProfile;
import com.pulsaride.proregistry.producer.ProAvailabilityChangedProducer;
import com.pulsaride.proregistry.producer.ProProfileUpdatedProducer;
import com.pulsaride.proregistry.repository.ProProfileRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/pros")
public class ProController {

    private final ProProfileRepository repository;
    private final ProProfileUpdatedProducer profileUpdatedProducer;
    private final ProAvailabilityChangedProducer availabilityChangedProducer;

    public ProController(
            ProProfileRepository repository,
            ProProfileUpdatedProducer profileUpdatedProducer,
            ProAvailabilityChangedProducer availabilityChangedProducer) {
        this.repository = repository;
        this.profileUpdatedProducer = profileUpdatedProducer;
        this.availabilityChangedProducer = availabilityChangedProducer;
    }

    @PostMapping
    public ResponseEntity<ProProfileResponse> createPro(@Valid @RequestBody CreateOrUpdateProDto dto) {
        UUID id = UUID.randomUUID();
        // Default a new pro to available with no tracked load, until
        // Dispatch Core starts actually assigning cases.
        ProProfile profile = new ProProfile(id, dto.specialty(), dto.description(), true, null);
        repository.save(profile);

        publishProfileUpdated(profile);
        publishAvailabilityChanged(profile);

        return ResponseEntity.status(HttpStatus.CREATED).body(ProProfileResponse.from(profile));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProProfileResponse> getPro(@PathVariable UUID id) {
        ProProfile profile = findOrThrow(id);
        return ResponseEntity.ok(ProProfileResponse.from(profile));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProProfileResponse> updateProfile(
            @PathVariable UUID id, @Valid @RequestBody CreateOrUpdateProDto dto) {
        ProProfile profile = findOrThrow(id);
        profile.updateProfile(dto.specialty(), dto.description());
        repository.save(profile);

        publishProfileUpdated(profile);

        return ResponseEntity.ok(ProProfileResponse.from(profile));
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<ProProfileResponse> updateAvailability(
            @PathVariable UUID id, @Valid @RequestBody UpdateAvailabilityDto dto) {
        ProProfile profile = findOrThrow(id);
        profile.updateAvailability(dto.available(), dto.currentLoad());
        repository.save(profile);

        publishAvailabilityChanged(profile);

        return ResponseEntity.ok(ProProfileResponse.from(profile));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("pro-registry-ok");
    }

    private ProProfile findOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pro not found: " + id));
    }

    // NOTE: request_id on these two event types is a fresh random UUID
    // with no real correlation meaning - see the flag raised alongside
    // this code. The shared envelope requires request_id on every
    // event, but pro-side events aren't tied to any patient request.
    // This satisfies schema validity without pretending it means
    // anything. Revisit if the envelope schema itself changes.

    private void publishProfileUpdated(ProProfile profile) {
        ProfileUpdatedPayload payload = new ProfileUpdatedPayload(
                profile.getId().toString(), profile.getSpecialty(), profile.getDescription());
        ProProfileUpdated event = new ProProfileUpdated(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "pro.profile_updated",
                "1.0",
                OffsetDateTime.now(),
                "pro-registry",
                payload);
        profileUpdatedProducer.publish(event);
    }

    private void publishAvailabilityChanged(ProProfile profile) {
        AvailabilityChangedPayload payload = new AvailabilityChangedPayload(
                profile.getId().toString(), profile.isAvailable(), profile.getCurrentLoad());
        ProAvailabilityChanged event = new ProAvailabilityChanged(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "pro.availability_changed",
                "1.0",
                OffsetDateTime.now(),
                "pro-registry",
                payload);
        availabilityChangedProducer.publish(event);
    }
}
