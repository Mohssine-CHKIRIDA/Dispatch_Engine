from __future__ import annotations

import json
import logging
import socket
import uuid
from datetime import datetime, timezone

import redis

import db
from config import Settings
from embeddings import EmbeddingModel
from shared.events import CaseEmbeddedPayload, RequestCaseEmbedded, RequestExtracted

log = logging.getLogger("matching.consumer")

PRODUCER_NAME = "matching"


def _text_to_embed(payload) -> str:
    parts = [payload.raw_text]
    if payload.symptoms:
        parts.append("Symptoms: " + ", ".join(payload.symptoms))
    if payload.specialty_hint:
        parts.append("Suspected specialty: " + payload.specialty_hint)
    return ". ".join(parts)


class MatchingConsumer:
    def __init__(self, settings: Settings, embedding_model: EmbeddingModel, pg_conn):
        self._settings = settings
        self._embedding_model = embedding_model
        self._pg_conn = pg_conn
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
            "matching consumer '%s' listening on '%s' (group=%s)",
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

            text = _text_to_embed(extracted.payload)
            embedding = self._embedding_model.embed(text)

            db.save_case_embedding(
                self._pg_conn,
                extracted.request_id,
                extracted.payload.specialty_hint,
                embedding,
            )

            case_payload = CaseEmbeddedPayload(
                embedding=embedding,
                dimension=len(embedding),
                embedding_model=self._settings.embedding_model_name,
            )
            event = RequestCaseEmbedded(
                event_id=uuid.uuid4(),
                request_id=extracted.request_id,
                event_type="request.case_embedded",
                version="1.0",
                produced_at=datetime.now(timezone.utc),
                producer=PRODUCER_NAME,
                payload=case_payload,
            )

            self._publish(event)
            self._ack(message_id)

            log.info(
                "Processed request_id=%s -> embedding dim=%d",
                extracted.request_id,
                len(embedding),
            )

        except Exception:
            log.exception("Failed to process message_id=%s", message_id)

    def _publish(self, event: RequestCaseEmbedded) -> None:
        self._redis.xadd(
            self._settings.target_stream,
            {
                "event_type": event.event_type,
                "request_id": str(event.request_id),
                "payload": event.model_dump_json(),
            },
        )

    def _ack(self, message_id: str) -> None:
        self._redis.xack(
            self._settings.source_stream,
            self._settings.consumer_group,
            message_id,
        )
