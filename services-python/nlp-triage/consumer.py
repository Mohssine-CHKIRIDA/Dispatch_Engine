from __future__ import annotations

import json
import logging
import socket
import uuid

import redis

from .config import Settings
from .llm_client import ExtractionClient
from .models import RequestSubmitted

log = logging.getLogger("nlp_triage.consumer")


class TriageConsumer:
    def __init__(self, settings: Settings, extraction_client: ExtractionClient):
        self._settings = settings
        self._extraction_client = extraction_client
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
            # BUSYGROUP means it already exists - fine, not an error.
            if "BUSYGROUP" not in str(e):
                raise

    def run_forever(self) -> None:
        log.info(
            "nlp-triage consumer '%s' listening on '%s' (group=%s)",
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
            block=5000,  # ms
        )
        if not response:
            return  # timed out, nothing new - loop again

        for _stream_key, messages in response:
            for message_id, fields in messages:
                self._handle_message(message_id, fields)

    def _handle_message(self, message_id: str, fields: dict) -> None:
        try:
            payload = json.loads(fields["payload"])
            submitted = RequestSubmitted.model_validate(payload)

            extracted = self._extraction_client.extract(
                raw_text=submitted.raw_text,
                request_id=submitted.request_id,
            )

            self._publish(extracted)
            self._ack(message_id)

            log.info(
                "Processed request_id=%s -> specialty_guess=%s confidence=%.2f",
                submitted.request_id,
                extracted.extraction.specialty_guess,
                extracted.extraction.confidence,
            )

        except Exception:
            # Deliberately do NOT ack on failure. The message stays
            # "pending" in the consumer group (visible via XPENDING) and
            # can be retried/reclaimed later (XCLAIM) instead of being
            # silently lost.
            log.exception("Failed to process message_id=%s", message_id)

    def _publish(self, extracted) -> None:
        self._redis.xadd(
            self._settings.target_stream,
            {
                "event_type": extracted.event_type,
                "request_id": str(extracted.request_id),
                "payload": extracted.model_dump_json(),
            },
        )

    def _ack(self, message_id: str) -> None:
        self._redis.xack(
            self._settings.source_stream,
            self._settings.consumer_group,
            message_id,
        )
