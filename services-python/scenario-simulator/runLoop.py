"""
Scenario Simulator

Continuously publishes random request.submitted events to Redis Streams
and records their ground truth in the evaluation database.

Usage:
    python run.py

Optional environment variables:

    REDIS_HOST
    REDIS_PORT

    EVALUATION_DB_HOST
    EVALUATION_DB_PORT
    EVALUATION_DB_NAME
    POSTGRES_USER
    POSTGRES_PASSWORD

    MIN_DELAY_SECONDS (default: 2)
    MAX_DELAY_SECONDS (default: 10)

Example:

    MIN_DELAY_SECONDS=1 MAX_DELAY_SECONDS=5 python run.py
"""

import os
import sys
import random
import time

# Make services-python/ importable so `from shared.events import ...` works.
sys.path.insert(
    0,
    os.path.join(
        os.path.dirname(os.path.abspath(__file__)),
        "..",
    ),
)

import redis

import db
from publisher import build_event, publish
from scenarios import SCENARIOS


def get_redis_client() -> redis.Redis:
    return redis.Redis(
        host=os.environ.get("REDIS_HOST", "localhost"),
        port=int(os.environ.get("REDIS_PORT", "6379")),
        decode_responses=True,
    )


def publish_scenario(redis_client, pg_conn, scenario):
    """
    Publish one scenario and store its ground truth.
    """

    event = build_event(
        patient_id=scenario.patient_id,
        text=scenario.text,
    )

    entry_id = publish(redis_client, event)

    db.insert_ground_truth(
        pg_conn,
        request_id=event.request_id,
        scenario_id=scenario.scenario_id,
        patient_id=scenario.patient_id,
        patient_text=scenario.text,
        expected_urgency_score=scenario.expected_urgency_score,
        expected_specialty=scenario.expected_specialty,
    )

    print(
        f"[{scenario.scenario_id}] "
        f"request_id={event.request_id} "
        f"-> stream entry {entry_id}"
    )


def main():
    redis_client = get_redis_client()

    pg_conn = db.get_connection()
    db.ensure_schema(pg_conn)

    min_delay = float(os.environ.get("MIN_DELAY_SECONDS", 2))
    max_delay = float(os.environ.get("MAX_DELAY_SECONDS", 5))

    if min_delay > max_delay:
        raise ValueError("MIN_DELAY_SECONDS cannot be greater than MAX_DELAY_SECONDS")

    print("=" * 60)
    print("Scenario Simulator started")
    print(f"Loaded scenarios : {len(SCENARIOS)}")
    print(f"Delay range      : {min_delay:.1f}s - {max_delay:.1f}s")
    print("Press Ctrl+C to stop.")
    print("=" * 60)

    try:
        while True:
            scenario = random.choice(SCENARIOS)

            publish_scenario(
                redis_client=redis_client,
                pg_conn=pg_conn,
                scenario=scenario,
            )

            delay = random.uniform(min_delay, max_delay)

            print(f"Sleeping {delay:.2f} seconds...\n")

            time.sleep(delay)

    except KeyboardInterrupt:
        print("\nStopping simulator...")

    finally:
        pg_conn.close()
        print("Database connection closed.")
        print("Simulator stopped.")


if __name__ == "__main__":
    try:
        main()

    except redis.exceptions.ConnectionError as e:
        print(
            f"ERROR: Could not connect to Redis.\n{e}",
            file=sys.stderr,
        )
        sys.exit(1)

    except Exception as e:
        print(f"ERROR: {e}", file=sys.stderr)
        sys.exit(1)