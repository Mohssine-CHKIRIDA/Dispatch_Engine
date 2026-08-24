package com.pulsaride.dispatch.controller;

import com.pulsaride.dispatch.domain.RequestContext;
import com.pulsaride.dispatch.dto.EnqueueTestRequestDto;
import com.pulsaride.dispatch.embedding.EmbeddingClient;
import com.pulsaride.dispatch.queue.PriorityQueueService;
import com.pulsaride.dispatch.registry.RequestContextStore;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Bypasses UrgencyScoredListener so you can exercise the whole
 * matching pipeline (queue -> Top-K -> composite score -> reservation)
 * before urgency-scoring exists. Remove or gate behind a profile once
 * the real event flow (ingestion -> nlp-triage -> urgency-scoring) is
 * wired end to end.
 */
@RestController
@RequestMapping("/test")
public class TestQueueController {

    private final PriorityQueueService priorityQueueService;
    private final RequestContextStore contextStore;
    private final EmbeddingClient embeddingClient;

    public TestQueueController(PriorityQueueService priorityQueueService,
                                RequestContextStore contextStore,
                                EmbeddingClient embeddingClient) {
        this.priorityQueueService = priorityQueueService;
        this.contextStore = contextStore;
        this.embeddingClient = embeddingClient;
    }

    @PostMapping("/enqueue")
    public ResponseEntity<Map<String, String>> enqueue(@Valid @RequestBody EnqueueTestRequestDto dto) {
        UUID requestId = UUID.randomUUID();
        double[] embedding = embeddingClient.embed(dto.summary());

        RequestContext ctx = new RequestContext(
                requestId,
                dto.summary(),
                dto.specialtyGuess() == null ? "" : dto.specialtyGuess(),
                dto.urgency(),
                embedding,
                Instant.now()
        );

        contextStore.save(ctx);
        priorityQueueService.enqueue(requestId.toString(), dto.urgency());

        return ResponseEntity.ok(Map.of("requestId", requestId.toString()));
    }
}
