"""
Calls the Pulsaride LLM Gateway with a forced tool call so the response
is always a well-formed structured object. This is the "first LLM" in
the pipeline: triage extraction only - it does not diagnose, prescribe,
or give medical advice, it turns the patient's free text into
structured routing signals that IA2 and IA3 consume next.

IMPORTANT: the tool schema below asks the LLM only for the fields it
actually needs to *infer* (symptoms, severity_keywords, duration_hint,
specialty_hint, location_hint, age_hint, extraction_confidence).
`raw_text` is NOT requested from the LLM - we already have it locally
from the caller, so it's set directly rather than asking the model to
reproduce the patient's exact original text verbatim (which risks
subtle transcription drift for no benefit).

Also note: the real request.extracted contract has no field to record
which LLM/model produced the extraction (neither the envelope nor
ExtractedPayload has one). That provenance only lives in this
service's logs now, not on the event itself.
"""

from __future__ import annotations

import logging
import uuid
from datetime import datetime, timezone

import httpx

from shared.events import RequestExtracted, ExtractedPayload

log = logging.getLogger("nlp_triage.llm_client")

PRODUCER_NAME = "nlp-triage"

SYSTEM_PROMPT = (
    "You are a triage information-extraction assistant for a patient "
    "dispatch system. Your only job is to extract structured signals "
    "from a patient's free-text request so it can be routed to an "
    "available healthcare professional. You do not diagnose, you do "
    "not suggest treatment, and you do not give medical advice. If the "
    "text is ambiguous or vague, reflect that with a lower "
    "extraction_confidence rather than guessing specifics. Leave a "
    "field out entirely if the text genuinely gives no basis for it - "
    "do not invent details. The one exception is specialty_hint: "
    "always give your best free-text guess at what kind of "
    "professional is needed (e.g. 'cardiologue', 'generaliste') based "
    "on the symptoms, even if uncertain - use 'generaliste' as a "
    "fallback only if truly nothing points to a specialty. Always "
    "respond by calling the extract_patient_request function."
)

EXTRACTION_FUNCTION = {
    "type": "function",
    "function": {
        "name": "extract_patient_request",
        "description": (
            "Extract structured triage signals from a raw patient request, "
            "for routing purposes only (not a medical diagnosis)."
        ),
        "parameters": {
            "type": "object",
            "required": ["symptoms", "specialty_hint", "extraction_confidence"],
            "properties": {
                "symptoms": {
                    "type": "array",
                    "items": {"type": "string"},
                    "description": "Discrete symptoms/complaints identified in the text",
                },
                "severity_keywords": {
                    "type": ["array", "null"],
                    "items": {"type": "string"},
                    "description": "Phrases signalling severity/urgency, used as input for urgency scoring",
                },
                "duration_hint": {
                    "type": ["string", "null"],
                    "description": "How long the patient reports the issue has been present, if stated",
                },
                "specialty_hint": {
                    "type": "string",
                    "description": "Free-text guess at the kind of professional needed, e.g. 'cardiologue' - not an exact tag, matched semantically downstream. Always give your best guess, use 'generaliste' if truly unclear.",
                },
                "location_hint": {
                    "type": ["string", "null"],
                    "description": "Location mentioned in the text, if any",
                },
                "age_hint": {
                    "type": ["integer", "null"],
                    "minimum": 0,
                    "description": "Patient age if mentioned or inferable from text",
                },
                "extraction_confidence": {
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
    """Raised when the Gateway can't produce a usable extraction."""


class ExtractionClient:
    def __init__(self, gateway_url: str, timeout: float = 200.0):
        # 200s, not 30s: the Gateway's own OllamaProvider can legitimately
        # take up to ~180s on a cold model load (see llm-gateway's
        # OLLAMA_TIMEOUT_SECONDS) if it falls back from Groq/NVIDIA. This
        # client's timeout must exceed the Gateway's worst-case internal
        # timeout, or we give up before the Gateway was actually done.
        self._gateway_url = gateway_url.rstrip("/")
        self._timeout = timeout

    def extract(self, raw_text: str, request_id: uuid.UUID) -> RequestExtracted:
        try:
            response = httpx.post(
                f"{self._gateway_url}/v1/complete",
                json={
                    "system_prompt": SYSTEM_PROMPT,
                    "user_prompt": f"Patient request:\n\n{raw_text}",
                    "temperature": 0.6,
                    "tools": [EXTRACTION_FUNCTION],
                    "tool_choice": {
                        "type": "function",
                        "function": {"name": "extract_patient_request"},
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

        symptoms = args.get("symptoms")
        if not symptoms:
            log.warning(
                "Gateway/LLM omitted 'symptoms' despite it being required "
                "in the tool schema; defaulting to empty list"
            )
            symptoms = []

        extraction_confidence = args.get("extraction_confidence")
        if extraction_confidence is None:
            log.warning(
                "Gateway/LLM omitted 'extraction_confidence' despite it "
                "being required in the tool schema; defaulting to 0.0 "
                "(lowest confidence, since we have no real signal)"
            )
            extraction_confidence = 0.0

        payload = ExtractedPayload(
            raw_text=raw_text,
            symptoms=symptoms,
            severity_keywords=args.get("severity_keywords"),
            duration_hint=args.get("duration_hint"),
            specialty_hint=args.get("specialty_hint"),
            location_hint=args.get("location_hint"),
            age_hint=args.get("age_hint"),
            extraction_confidence=extraction_confidence,
        )

        return RequestExtracted(
            event_id=uuid.uuid4(),
            request_id=request_id,
            event_type="request.extracted",
            version="1.0",
            produced_at=datetime.now(timezone.utc),
            producer=PRODUCER_NAME,
            payload=payload,
        )