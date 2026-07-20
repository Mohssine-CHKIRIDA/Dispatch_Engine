"""
Ground truth storage in evaluation_db (PostgreSQL) — see infra/postgres/init/
for how this database gets created (one-per-service, per the doc).

For now this uses plain psycopg2 with idempotent DDL (CREATE TABLE IF NOT
EXISTS) rather than a real migration tool. When you move to Alembic for
the Python services (per the earlier ORM discussion), replace
ensure_schema() with a proper Alembic migration and remove this function.
"""

import os

import psycopg

DDL = """
CREATE TABLE IF NOT EXISTS ground_truth (
    request_id UUID PRIMARY KEY,
    scenario_id TEXT NOT NULL,
    patient_id TEXT NOT NULL,
    patient_text TEXT NOT NULL,
    expected_urgency_score INTEGER NOT NULL CHECK (expected_urgency_score BETWEEN 0 AND 3),
    expected_specialty TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
"""

INSERT = """
INSERT INTO ground_truth
    (request_id, scenario_id, patient_id, patient_text, expected_urgency_score, expected_specialty)
VALUES (%s, %s, %s, %s, %s, %s)
ON CONFLICT (request_id) DO NOTHING;
"""


def get_connection():
    """
    Reads connection info from env vars, falling back to the defaults set
    in docker-compose.yml / .env.example.
    """
   
    return psycopg.connect(
        host=os.environ.get("EVALUATION_DB_HOST", "localhost"),
        port=os.environ.get("EVALUATION_DB_PORT", "5433"),
        dbname=os.environ.get("EVALUATION_DB_NAME", "pulsaride"),
        user=os.environ.get("POSTGRES_USER", "pulsaride"),
        password=os.environ.get("POSTGRES_PASSWORD", "pulsaride"),
    )


def ensure_schema(conn) -> None:
    with conn.cursor() as cur:
        cur.execute(DDL)
    conn.commit()


def insert_ground_truth(conn, request_id, scenario_id, patient_id, patient_text,
                         expected_urgency_score, expected_specialty) -> None:
    with conn.cursor() as cur:
        cur.execute(
            INSERT,
            (str(request_id), scenario_id, patient_id, patient_text,
             expected_urgency_score, expected_specialty),
        )
    conn.commit()
