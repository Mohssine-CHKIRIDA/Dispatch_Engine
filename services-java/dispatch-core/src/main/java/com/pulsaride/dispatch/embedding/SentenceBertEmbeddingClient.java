package com.pulsaride.dispatch.embedding;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Calls the dedicated embedding-service (Sentence-BERT,
 * paraphrase-multilingual-mpnet-base-v2, 768 dims), per IA 3 in the
 * v2 architecture doc. Used for BOTH:
 *   - embedding_cas: the patient request text, computed fresh at
 *     match time (see MatchingEngine.resolveEmbedding)
 *   - embedding_pro: a doctor's profile text, computed once when the
 *     doctor is seeded/updated (see DoctorController) and cached in
 *     the doctor's Redis hash from then on
 *
 * Both MUST go through this same client/model - cosine similarity is
 * only meaningful when both vectors come from the same embedding
 * space, which is why EmbeddingClient stays a single shared interface
 * rather than having separate "case" and "pro" embedding paths.
 */
@Component
@ConditionalOnProperty(name = "pulsaride.embedding.provider", havingValue = "sentence-bert")
public class SentenceBertEmbeddingClient implements EmbeddingClient {

    private final RestTemplate restTemplate;

    @Value("${pulsaride.embedding.base-url}")
    private String baseUrl;

    public SentenceBertEmbeddingClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public double[] embed(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(Map.of("text", text), headers);

        JsonNode response = restTemplate.postForObject(baseUrl + "/embed", request, JsonNode.class);

        if (response == null || !response.has("embedding")) {
            throw new IllegalStateException(
                    "embedding-service returned no embedding for input text (is it running on " + baseUrl + "?)");
        }

        JsonNode vectorNode = response.get("embedding");
        double[] vector = new double[vectorNode.size()];
        for (int i = 0; i < vectorNode.size(); i++) {
            vector[i] = vectorNode.get(i).asDouble();
        }
        return vector;
    }
}
