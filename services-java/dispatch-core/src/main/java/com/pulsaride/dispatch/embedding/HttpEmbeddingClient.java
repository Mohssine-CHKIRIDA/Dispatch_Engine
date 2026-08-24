package com.pulsaride.dispatch.embedding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Calls an OpenAI-compatible embeddings endpoint, e.g. an NVIDIA NIM
 * embedding model - same "OpenAI-compatible everywhere" pattern used
 * for Kimi K2.6 in nlp-triage. Active only when
 * pulsaride.embedding.provider=http (see application.yml); otherwise
 * HashEmbeddingClient is used, which needs no external API and is
 * meant for local development/demos only.
 */
@Component
@ConditionalOnProperty(name = "pulsaride.embedding.provider", havingValue = "http")
public class HttpEmbeddingClient implements EmbeddingClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${pulsaride.embedding.base-url}")
    private String baseUrl;

    @Value("${pulsaride.embedding.api-key}")
    private String apiKey;

    @Value("${pulsaride.embedding.model}")
    private String model;

    public HttpEmbeddingClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public double[] embed(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "model", model,
                "input", List.of(text)
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        JsonNode response = restTemplate.postForObject(
                baseUrl + "/embeddings", request, JsonNode.class);

        if (response == null || !response.has("data") || response.get("data").isEmpty()) {
            throw new IllegalStateException("Embedding provider returned no data for input text");
        }

        JsonNode vector = response.get("data").get(0).get("embedding");
        double[] result = new double[vector.size()];
        for (int i = 0; i < vector.size(); i++) {
            result[i] = vector.get(i).asDouble();
        }
        return result;
    }
}
