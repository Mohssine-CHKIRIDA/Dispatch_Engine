import json
import psycopg2
import psycopg2.extras
from config import POSTGRES_DSN


def get_conn():
    return psycopg2.connect(POSTGRES_DSN)


def upsert_submitted(conn, request_id, patient_id, submitted_at):
    with conn.cursor() as cur:
        cur.execute(
            """
            INSERT INTO request_lifecycle (request_id, patient_id, submitted_at)
            VALUES (%s, %s, %s)
            ON CONFLICT (request_id) DO UPDATE SET
                patient_id = EXCLUDED.patient_id,
                submitted_at = EXCLUDED.submitted_at
            """,
            (request_id, patient_id, submitted_at),
        )
    conn.commit()


def update_lifecycle(conn, request_id, **fields):
    if not fields:
        return
    # Ensure a row exists even if request.submitted was somehow missed -
    # a partial lifecycle is still useful data, not something to drop.
    with conn.cursor() as cur:
        cur.execute(
            "INSERT INTO request_lifecycle (request_id) VALUES (%s) ON CONFLICT DO NOTHING",
            (request_id,),
        )
        set_clause = ", ".join(f"{k} = %s" for k in fields)
        cur.execute(
            f"UPDATE request_lifecycle SET {set_clause} WHERE request_id = %s",
            (*fields.values(), request_id),
        )
    conn.commit()


def insert_proposal(conn, request_id, doctor_id, match_score, strategy, proposed_at):
    with conn.cursor() as cur:
        cur.execute(
            """
            INSERT INTO proposals (request_id, doctor_id, match_score, strategy, proposed_at, outcome)
            VALUES (%s, %s, %s, %s, %s, 'pending')
            """,
            (request_id, doctor_id, match_score, strategy, proposed_at),
        )
    conn.commit()


def resolve_proposal(conn, request_id, doctor_id, outcome, outcome_at):
    with conn.cursor() as cur:
        cur.execute(
            """
            UPDATE proposals SET outcome = %s, outcome_at = %s
            WHERE id = (
                SELECT id FROM proposals
                WHERE request_id = %s AND doctor_id = %s AND outcome = 'pending'
                ORDER BY proposed_at DESC LIMIT 1
            )
            """,
            (outcome, outcome_at, request_id, doctor_id),
        )
    conn.commit()