import json
import logging
import time
from datetime import datetime, timezone

import redis

from config import REDIS_HOST, REDIS_PORT, CONSUMER_GROUP, CONSUMER_NAME, STREAMS
import db

log = logging.getLogger("evaluation.consumer")


def _parse_ts(iso_str):
    return datetime.fromisoformat(iso_str.replace("Z", "+00:00")) if iso_str else None


class EvaluationConsumer:
    def __init__(self):
        self.redis = redis.Redis(host=REDIS_HOST, port=REDIS_PORT, decode_responses=True)
        self.conn = db.get_conn()
        self._ensure_groups()

    def _ensure_groups(self):
        for stream in STREAMS:
            try:
                self.redis.xgroup_create(stream, CONSUMER_GROUP, id="0", mkstream=True)
            except redis.exceptions.ResponseError as e:
                if "BUSYGROUP" not in str(e):
                    raise
                log.info("Consumer group already exists on %s - continuing", stream)

    def run(self):
        stream_ids = {s: ">" for s in STREAMS}
        log.info("EvaluationConsumer started, group=%s streams=%s", CONSUMER_GROUP, STREAMS)
        while True:
            entries = self.redis.xreadgroup(
                CONSUMER_GROUP, CONSUMER_NAME, stream_ids, count=50, block=2000
            )
            if not entries:
                continue
            for stream_name, messages in entries:
                for message_id, fields in messages:
                    try:
                        self._handle(stream_name, fields)
                        self.redis.xack(stream_name, CONSUMER_GROUP, message_id)
                    except Exception:
                        log.exception(
                            "Failed to process message_id=%s on stream=%s - left pending",
                            message_id, stream_name,
                        )

    def _handle(self, stream_name, fields):
        envelope = json.loads(fields["payload"])
        request_id = envelope["request_id"]
        payload = envelope["payload"]
        produced_at = _parse_ts(envelope.get("produced_at"))

        if stream_name == "requests:submitted":
            db.upsert_submitted(self.conn, request_id, payload.get("patient_id"), produced_at)

        elif stream_name == "requests:extracted":
            db.update_lifecycle(self.conn, request_id, extracted_at=produced_at)

        elif stream_name == "requests:urgency_score":
            db.update_lifecycle(
                self.conn, request_id,
                urgency_score=payload.get("score"),
                urgency_scored_at=produced_at,
            )

        elif stream_name == "requests:proposed":
            db.insert_proposal(
                self.conn, request_id,
                doctor_id=payload["pro_id"],
                match_score=payload.get("match_score"),
                strategy=payload.get("strategy"),
                proposed_at=produced_at,
            )

        elif stream_name == "requests:accepted":
            db.resolve_proposal(self.conn, request_id, payload["pro_id"], "accepted", produced_at)

        elif stream_name == "requests:refused":
            reason = payload.get("reason", "doctor_declined")
            outcome = "timeout" if reason == "timeout" else "refused"
            db.resolve_proposal(self.conn, request_id, payload["pro_id"], outcome, produced_at)

        elif stream_name == "requests:closed":
            db.update_lifecycle(self.conn, request_id, closed_at=produced_at)