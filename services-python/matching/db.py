"""
Postgres/pgvector access for IA3. Only responsibility: persist the
case embedding for traceability/evaluation. Per the real
request.case_embedded contract (embedding/dimension/embedding_model
only, no top_matches field), finding the best-matching professional is
NOT IA3's job - Dispatch Core does that pgvector nearest-neighbor
query itself, using the embedding value carried directly on the event.
So there's no pro_embeddings table or matching query here.
"""

from __future__ import annotations

import uuid
from typing import List

import psycopg2


def get_connection(dsn: str):
    return psycopg2.connect(dsn)


def ensure_schema(conn, embedding_dim: int) -> None:
    with conn.cursor() as cur:
        cur.execute("CREATE EXTENSION IF NOT EXISTS vector;")
        cur.execute(
            f"""
            CREATE TABLE IF NOT EXISTS case_embeddings (
                request_id UUID PRIMARY KEY,
                specialty_hint TEXT,
                embedding VECTOR({embedding_dim}) NOT NULL,
                created_at TIMESTAMPTZ NOT NULL DEFAULT now()
            );
            """
        )
    conn.commit()


def _vector_literal(embedding: List[float]) -> str:
    return "[" + ",".join(repr(float(x)) for x in embedding) + "]"


def save_case_embedding(
    conn, request_id: uuid.UUID, specialty_hint, embedding: List[float]
) -> None:
    with conn.cursor() as cur:
        cur.execute(
            """
            INSERT INTO case_embeddings (request_id, specialty_hint, embedding)
            VALUES (%s, %s, %s::vector)
            ON CONFLICT (request_id) DO UPDATE
                SET specialty_hint = EXCLUDED.specialty_hint,
                    embedding = EXCLUDED.embedding;
            """,
            (str(request_id), specialty_hint, _vector_literal(embedding)),
        )
    conn.commit()
