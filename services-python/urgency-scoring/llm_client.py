"""
Calls the Gateway with a forced tool call to produce a 0-3 urgency
score from IA1's real ExtractedPayload fields. Like IA1, this does not
diagnose or prescribe - it only estimates how quickly a request needs
a human professional.

Note: the real request.urgency_score contract only has score/rationale/
confidence on the payload - there's no 'escalate' flag or similar in
the actual schema, so none is invented here. If you want an escalation
signal later, that needs a schema change (add it to
UrgencyScorePayload, regenerate models), not just app-level logic.
"""

from __future__ import annotations

import json
import uuid
from datetime import datetime, timezone


import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
import httpx


from shared.events import ExtractedPayload, RequestUrgencyScore, UrgencyScorePayload

PRODUCER_NAME = "urgency-scoring"

SYSTEM_PROMPT = (
    "You are an urgency-scoring assistant for a patient dispatch system. "
    "You receive structured triage signals (already extracted from a "
    "patient's free text) and must assign an urgency score from 0 to 3:\n"
    "0 = routine, can be scheduled with no time pressure\n"
    "1 = moderate, should be seen the same day\n"
    "2 = urgent, should be seen within the hour\n"
    "3 = critical, emergency requiring immediate dispatch\n\n"
    "You do not diagnose or suggest treatment. Base your score only on "
    "the structured signals provided (symptoms, severity_keywords, "
    "duration_hint, age_hint). If information is sparse or ambiguous, "
    "reflect that with lower confidence rather than guessing a higher "
    "score than the evidence supports. Always respond by calling the "
    "score_urgency function."
)

SCORE_URGENCY_FUNCTION = {
    "type": "function",
    "function": {
        "name": "score_urgency",
        "description": (
            "Assign a 0-3 urgency score to a patient request based on "
            "structured triage signals, for routing/prioritization only "
            "(not a medical diagnosis)."
        ),
        "parameters": {
            "type": "object",
            "required": ["score", "rationale", "confidence"],
            "properties": {
                "score": {
                    "type": "integer",
                    "enum": [0, 1, 2, 3],
                    "description": "0=routine, 1=moderate, 2=urgent, 3=critical",
                },
                "rationale": {
                    "type": "string",
                    "description": "One or two neutral sentences explaining the score",
                },
                "confidence": {
                    "type": "number",
                    "minimum": 0,
                    "maximum": 1,
                },
            },
            "additionalProperties": False,
        },
    },
}


class GatewayError(Exception):
    """Raised when the Gateway can't produce a usable urgency score."""


class UrgencyClient:
    def __init__(self, gateway_url: str, timeout: float = 200.0):
        self._gateway_url = gateway_url.rstrip("/")
        self._timeout = timeout

    def score(
        self, extracted_payload: ExtractedPayload, request_id: uuid.UUID
    ) -> RequestUrgencyScore:
        user_payload = {
            "raw_text": extracted_payload.raw_text,
            "symptoms": extracted_payload.symptoms,
            "severity_keywords": extracted_payload.severity_keywords,
            "duration_hint": extracted_payload.duration_hint,
            "specialty_hint": extracted_payload.specialty_hint,
            "age_hint": extracted_payload.age_hint,
            "extraction_confidence": extracted_payload.extraction_confidence,
        }

        try:
            response = httpx.post(
                f"{self._gateway_url}/v1/complete",
                json={
                    "system_prompt": SYSTEM_PROMPT,
                    "user_prompt": f"Structured triage signals:\n\n{json.dumps(user_payload, ensure_ascii=False)}",
                    "temperature": 0.3,
                    "tools": [SCORE_URGENCY_FUNCTION],
                    "tool_choice": {
                        "type": "function",
                        "function": {"name": "score_urgency"},
                    },
                },
                timeout=self._timeout,
            )
        except httpx.HTTPError as exc:
            raise GatewayError(f"Could not reach LLM Gateway: {exc}") from exc

        if response.status_code != 200:
            raise GatewayError(
                f"LLM Gateway returned {response.status_code}: {response.text}"
            )

        data = response.json()
        tool_call = data.get("tool_call")
        if not tool_call:
            raise GatewayError(
                "LLM Gateway did not return a tool call despite tool_choice "
                "being forced"
            )

        args = tool_call["arguments"]
        payload = UrgencyScorePayload(
            score=args["score"],
            rationale=args.get("rationale"),
            confidence=args["confidence"],
        )

        return RequestUrgencyScore(
            event_id=uuid.uuid4(),
            request_id=request_id,
            event_type="request.urgency_score",
            version="1.0",
            produced_at=datetime.now(timezone.utc),
            producer=PRODUCER_NAME,
            payload=payload,
        )
