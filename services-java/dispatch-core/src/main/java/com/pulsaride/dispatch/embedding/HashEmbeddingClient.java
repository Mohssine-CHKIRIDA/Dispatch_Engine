package com.pulsaride.dispatch.embedding;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * NOT a real semantic embedding - just a deterministic pseudo-vector
 * derived from word hashes, so you can run and demo the full matching
 * pipeline (Top-K, cosine similarity, composite score, reservation)
 * end to end without needing an embedding API key yet. It gives
 * "same words -> similar vector", nothing more. Swap
 * pulsaride.embedding.provider to "http" and configure
 * HttpEmbeddingClient once you're ready for real semantic search.
 */
@Component
@ConditionalOnProperty(name = "pulsaride.embedding.provider", havingValue = "hash", matchIfMissing = true)
public class HashEmbeddingClient implements EmbeddingClient {

    private static final int DIMENSIONS = 64;

    @Override
    public double[] embed(String text) {
        double[] vector = new double[DIMENSIONS];
        if (text == null || text.isBlank()) {
            return vector;
        }

        String[] words = text.toLowerCase().split("\\W+");
        for (String word : words) {
            if (word.isBlank()) continue;
            int bucket = Math.floorMod(sha256Hash(word), DIMENSIONS);
            vector[bucket] += 1.0;
        }

        normalize(vector);
        return vector;
    }

    private int sha256Hash(String word) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(word.getBytes(StandardCharsets.UTF_8));
            int result = 0;
            for (int i = 0; i < 4; i++) {
                result = (result << 8) | (hash[i] & 0xFF);
            }
            return result;
        } catch (Exception e) {
            return word.hashCode();
        }
    }

    private void normalize(double[] vector) {
        double norm = 0;
        for (double v : vector) norm += v * v;
        norm = Math.sqrt(norm);
        if (norm == 0) return;
        for (int i = 0; i < vector.length; i++) {
            vector[i] /= norm;
        }
    }
}
