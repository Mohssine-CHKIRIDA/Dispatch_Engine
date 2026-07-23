package com.pulsaride.ingestion.controller;

import com.pulsaride.ingestion.dto.CreateRequestDto;
import com.pulsaride.ingestion.dto.RequestAcceptedResponse;
import com.pulsaride.ingestion.event.RequestSubmittedEvent;
import com.pulsaride.ingestion.producer.RequestSubmittedProducer;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
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

        RequestSubmittedEvent event = RequestSubmittedEvent.of(
                requestId,
                submittedAt,
                dto.rawText(),
                dto.requestedSpecialty(),
                dto.lat(),
                dto.lng(),
                dto.locationLabel(),
                dto.contactChannel(),
                dto.contactValue(),
                dto.source()
        );

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
