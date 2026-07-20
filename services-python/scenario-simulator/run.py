"""
Scenario Simulator — publishes 6 fixed request.submitted events to Redis,
recording ground truth in evaluation_db for later comparison (Phase 5).

Usage (run from inside this directory):
    cd services-python/scenario-simulator
    python3 run.py

Env vars (all optional, defaults match docker-compose.yml):
    REDIS_HOST, REDIS_PORT
    EVALUATION_DB_HOST, EVALUATION_DB_PORT, EVALUATION_DB_NAME
    POSTGRES_USER, POSTGRES_PASSWORD

NOTE on imports: this folder is named "scenario-simulator" (hyphen), which
is not a valid Python package name, so it's run as a standalone script
rather than imported as a package (`import scenario_simulator` would not
work). The sys.path insert below makes the sibling `shared` package
(services-python/shared/) importable regardless of where this script is
run from.
"""

import os
import sys

# Make services-python/ importable so `from shared.events import ...` works,
# regardless of the current working directory this script is run from.
sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), ".."))

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


def main():
    redis_client = get_redis_client()
    pg_conn = db.get_connection()
    db.ensure_schema(pg_conn)

    print(f"Publishing {len(SCENARIOS)} scenarios to Redis Stream 'request.submitted'...\n")

    for scenario in SCENARIOS:
        event = build_event(patient_id=scenario.patient_id, text=scenario.text)
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

        print(f"  [{scenario.scenario_id}] request_id={event.request_id} -> stream entry {entry_id}")

    pg_conn.close()
    print(f"\nDone. {len(SCENARIOS)} events published, {len(SCENARIOS)} ground truth rows recorded.")


if __name__ == "__main__":
    try:
        main()
    except redis.exceptions.ConnectionError as e:
        print(f"ERROR: could not connect to Redis — is docker-compose up? ({e})", file=sys.stderr)
        sys.exit(1)
    except Exception as e:
        print(f"ERROR: {e}", file=sys.stderr)
        sys.exit(1)
