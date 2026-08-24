"""
IA 3 - Embeddings, per the v2 architecture doc.

Wraps a Sentence-BERT model (paraphrase-multilingual-mpnet-base-v2,
768 dims, good multilingual/French support per the doc) behind a
small HTTP API so dispatch-core (Java) can call it without needing a
JVM embedding library. This is deliberately the *only* thing this
service does - encoding text into vectors. It does not know about
doctors, patients, or matching; it just turns text into numbers.

Used for BOTH:
  - "embedding_cas": the patient's request text (encoded per-request)
  - "embedding_pro": a doctor's profile text (encoded once, cached by
    dispatch-core in Redis / persisted in Postgres+pgvector)

Same model for both is required - cosine similarity is only
meaningful when both vectors come from the same embedding space.
"""

from __future__ import annotations

import logging

from fastapi import FastAPI
from pydantic import BaseModel, Field
from sentence_transformers import SentenceTransformer

log = logging.getLogger("embedding_service")
logging.basicConfig(level=logging.INFO)

MODEL_NAME = "paraphrase-multilingual-mpnet-base-v2"
EMBEDDING_DIMENSIONS = 768

app = FastAPI(title="Pulsaride Embedding Service", version="0.1.0")

# Loaded once at process startup - Sentence-BERT model loads are slow
# (seconds), so this must NOT happen per-request.
log.info("Loading model %s ...", MODEL_NAME)
_model = SentenceTransformer(MODEL_NAME)
log.info("Model loaded. Embedding dimension = %d", _model.get_sentence_embedding_dimension())


class EmbedRequest(BaseModel):
    text: str = Field(min_length=1, max_length=4000)


class EmbedResponse(BaseModel):
    embedding: list[float]
    dimensions: int
    model: str


class BatchEmbedRequest(BaseModel):
    texts: list[str] = Field(min_length=1, max_length=256)


class BatchEmbedResponse(BaseModel):
    embeddings: list[list[float]]
    dimensions: int
    model: str


@app.get("/health")
def health():
    return {"status": "ok", "model": MODEL_NAME, "dimensions": EMBEDDING_DIMENSIONS}


@app.post("/embed", response_model=EmbedResponse)
def embed(req: EmbedRequest) -> EmbedResponse:
    """
    Single-text embedding. Used for:
      - embedding_cas: one patient request at match time
      - embedding_pro: one doctor profile at seed/update time
    """
    vector = _model.encode(req.text, normalize_embeddings=True)
    return EmbedResponse(
        embedding=vector.tolist(),
        dimensions=len(vector),
        model=MODEL_NAME,
    )


@app.post("/embed/batch", response_model=BatchEmbedResponse)
def embed_batch(req: BatchEmbedRequest) -> BatchEmbedResponse:
    """
    Batch embedding - useful for the scenario-simulator seeding many
    doctor profiles at once, or re-embedding a corpus for evaluation,
    without one HTTP round-trip per profile.
    """
    vectors = _model.encode(req.texts, normalize_embeddings=True)
    return BatchEmbedResponse(
        embeddings=[v.tolist() for v in vectors],
        dimensions=vectors.shape[1],
        model=MODEL_NAME,
    )
