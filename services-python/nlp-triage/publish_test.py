"""
publish_test.py

Publishes a single well-formed test event directly to Redis Streams via
redis-py, bypassing the shell entirely. This avoids the PowerShell
quote-stripping bug that corrupted every message sent via
`docker exec -it pulsaride-redis redis-cli XADD ...` from PowerShell.

Usage (from project root or wherever this file lives):
    python publish_test.py
    python publish_test.py --text "some other patient text"
    python publish_test.py --stream requests:submitted
"""

import argparse
import json
import uuid
from datetime import datetime, timezone

import redis


def build_event(raw_text: str) -> dict:
    """Build a request.submitted event matching the SubmittedPayload contract."""
    now = datetime.now(timezone.utc).isoformat()
    return {
        "event_id": str(uuid.uuid4()),
        "event_type": "request.submitted",
        "event_version": 1,
        "occurred_at": now,
        "request_id": str(uuid.uuid4()),
        "submitted_at": now,
        "raw_text": raw_text,
    }


def main():
    parser = argparse.ArgumentParser(description="Publish a test request.submitted event to Redis.")
    parser.add_argument(
        "--text",
        default="jai mal a la poitrine depuis ce matin, difficulte a respirer",
        help="raw_text value for the simulated patient message",
    )
    parser.add_argument(
        "--stream",
        default="requests:submitted",
        help="Redis stream name to publish to (default: requests:submitted)",
    )
    parser.add_argument("--host", default="localhost", help="Redis host (default: localhost)")
    parser.add_argument("--port", type=int, default=6379, help="Redis port (default: 6379)")
    args = parser.parse_args()

    r = redis.Redis(host=args.host, port=args.port, decode_responses=True)

    # Fail fast and clearly if this is hitting the wrong Redis instance
    # (relevant given the earlier wslrelay.exe shadowing bug).
    pong = r.ping()
    print(f"[publish_test] PING -> {pong}")

    event = build_event(args.text)
    payload_json = json.dumps(event)

    message_id = r.xadd(args.stream, {"payload": payload_json})

    print(f"[publish_test] Published to stream '{args.stream}'")
    print(f"[publish_test] message_id = {message_id}")
    print(f"[publish_test] payload    = {payload_json}")


if __name__ == "__main__":
    main()