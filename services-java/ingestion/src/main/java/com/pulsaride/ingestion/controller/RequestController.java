package com.pulsaride.ingestion.controller;

import com.pulsaride.events.RequestSubmitted;
import com.pulsaride.events.SubmittedPayload;
import com.pulsaride.ingestion.dto.CreateRequestDto;
import com.pulsaride.ingestion.dto.RequestAcceptedResponse;
import com.pulsaride.ingestion.producer.RequestSubmittedProducer;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/requests")
public class RequestController {

    private final RequestSubmittedProducer producer;

    public RequestController(RequestSubmittedProducer producer) {
        this.producer = producer;
    }

    @PostMapping
    public ResponseEntity<RequestAcceptedResponse> submitRequest(@Valid @RequestBody CreateRequestDto dto) {

        UUID requestId = UUID.randomUUID();
        Instant submittedAt = Instant.now();

        // NOTE: the current request.submitted contract (contracts/events/
        // request.submitted.schema.json) only defines `patient_id` and
        // `text` on the payload - it has no fields yet for location,
        // contact info, or requested specialty, even though the DTO
        // collects them. Those fields are intentionally dropped here
        // rather than silently smuggled into extra JSON keys the schema
        // doesn't allow (payload has additionalProperties: false).
        // If/when Dispatch Core or Notification Gateway need lat/lng or
        // contact info, extend SubmittedPayload's schema first, regenerate
        // both the Java and Python models, then wire it through here.
        //
        // patient_id: the DTO has no real patient identity to use (no
        // auth, no health data per the conception doc's scope) - a fresh
        // UUID is generated per request, matching the schema's own
        // description of this being a "synthetic/simulated ID".
        String syntheticPatientId = UUID.randomUUID().toString();

        SubmittedPayload payload = new SubmittedPayload(syntheticPatientId, dto.rawText());

        RequestSubmitted event = new RequestSubmitted(
                UUID.randomUUID(), // event_id
                requestId, // request_id
                "request.submitted", // event_type
                "1.0", // version
                OffsetDateTime.now(), // produced_at
                "ingestion", // producer
                payload);

        producer.publish(event);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(RequestAcceptedResponse.accepted(requestId, submittedAt));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ingestion-ok");
    }
}