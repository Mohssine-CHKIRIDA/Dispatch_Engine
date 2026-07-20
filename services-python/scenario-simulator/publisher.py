"""
Publishes request.submitted events to Redis Streams, using the shared
RequestSubmitted Pydantic model (single source of truth: contracts/events/).

Event serialization convention for this stream: a single field named
"data" holding the full event as a JSON string. Consumers (IA1) should
XREAD/XREADGROUP and json.loads() the "data" field to get the event back.
"""

import uuid
from datetime import datetime, timezone

import redis

from shared.events import RequestSubmitted, SubmittedPayload

STREAM_NAME = "request.submitted"


def build_event(patient_id: str, text: str, producer: str = "scenario-simulator") -> RequestSubmitted:
    """Wraps raw scenario data into a valid RequestSubmitted event."""
    return RequestSubmitted(
        event_id=uuid.uuid4(),
        request_id=uuid.uuid4(),
        event_type="request.submitted",
        version="1.0",
        produced_at=datetime.now(timezone.utc),
        producer=producer,
        payload=SubmittedPayload(patient_id=patient_id, text=text),
    )


def publish(client: redis.Redis, event: RequestSubmitted) -> str:
    """
    Publishes one event to the request.submitted stream.
    Returns the Redis Stream entry ID (e.g. '1721300000000-0').
    """
    serialized = event.model_dump_json()
    entry_id = client.xadd(STREAM_NAME, {"data": serialized})
    return entry_id
