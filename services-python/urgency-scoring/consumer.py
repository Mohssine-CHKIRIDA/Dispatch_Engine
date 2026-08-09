from __future__ import annotations

import json
import logging
import socket
import uuid

import redis

from config import Settings
from llm_client import UrgencyClient
from shared.events import RequestExtracted, RequestUrgencyScore

log = logging.getLogger("urgency_scoring.consumer")


class UrgencyConsumer:
    def __init__(self, settings: Settings, urgency_client: UrgencyClient):
        self._settings = settings
        self._urgency_client = urgency_client
        self._redis = redis.Redis(
            host=settings.redis_host,
            port=settings.redis_port,
            decode_responses=True,
        )
        self._consumer_name = f"{socket.gethostname()}-{uuid.uuid4().hex[:8]}"
        self._ensure_group_exists()

    def _ensure_group_exists(self) -> None:
        try:
            self._redis.xgroup_create(
                name=self._settings.source_stream,
                groupname=self._settings.consumer_group,
                id="0",
                mkstream=True,
            )
            log.info(
                "Created consumer group %s on stream %s",
                self._settings.consumer_group,
                self._settings.source_stream,
            )
        except redis.exceptions.ResponseError as e:
            if "BUSYGROUP" not in str(e):
                raise

    def run_forever(self) -> None:
        log.info(
            "urgency-scoring consumer '%s' listening on '%s' (group=%s)",
            self._consumer_name,
            self._settings.source_stream,
            self._settings.consumer_group,
        )
        while True:
            self._poll_once()

    def _poll_once(self) -> None:
        response = self._redis.xreadgroup(
            groupname=self._settings.consumer_group,
            consumername=self._consumer_name,
            streams={self._settings.source_stream: ">"},
            count=10,
            block=5000,
        )
        if not response:
            return

        for _stream_key, messages in response:
            for message_id, fields in messages:
                self._handle_message(message_id, fields)

    def _handle_message(self, message_id: str, fields: dict) -> None:
        try:
            payload = json.loads(fields["payload"])
            extracted = RequestExtracted.model_validate(payload)

            scored = self._urgency_client.score(
                extracted_payload=extracted.payload,
                request_id=extracted.request_id,
            )

            self._publish(scored)
            self._ack(message_id)

            log.info(
                "Processed request_id=%s -> urgency_score=%s confidence=%.2f",
                extracted.request_id,
                scored.payload.score,
                scored.payload.confidence,
            )

        except Exception:
            log.exception("Failed to process message_id=%s", message_id)

    def _publish(self, scored: RequestUrgencyScore) -> None:
        self._redis.xadd(
            self._settings.target_stream,
            {
                "event_type": scored.event_type,
                "request_id": str(scored.request_id),
                "payload": scored.model_dump_json(),
            },
        )

    def _ack(self, message_id: str) -> None:
        self._redis.xack(
            self._settings.source_stream,
            self._settings.consumer_group,
            message_id,
        )
