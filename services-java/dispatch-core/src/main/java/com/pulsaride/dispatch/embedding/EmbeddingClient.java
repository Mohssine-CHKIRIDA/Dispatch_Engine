package com.pulsaride.dispatch.embedding;

/**
 * Step 5 of the matching algorithm doc needs embeddings for both the
 * patient request text and each doctor's profile text. Kept as an
 * interface so the real provider (e.g. an NVIDIA NIM embedding model,
 * same pattern as the Kimi K2.6 integration in nlp-triage) is a
 * pluggable implementation rather than baked into the matching logic.
 */
public interface EmbeddingClient {
    double[] embed(String text);
}
