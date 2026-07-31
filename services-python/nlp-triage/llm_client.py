"""
Calls the Pulsaride LLM Gateway with a forced tool call so the response
is always a well-formed structured object, never free text we'd have to
hope is valid JSON. This is the "first LLM" in the pipeline: triage
extraction only. It does not diagnose, prescribe, or give medical
advice - it turns the patient's free text into structured routing
signals that urgency-scoring and matching consume next.

This used to call NVIDIA's integrate.api.nvidia.com endpoint directly
via the `openai` SDK. It now goes through the Gateway's /v1/complete
instead, so caching, provider fallback (NVIDIA -> Ollama), and any
future provider swap are centralized in one place rather than
duplicated in every service that needs an LLM. The Gateway is
responsible for actually talking to NVIDIA/Kimi (or whichever provider
is configured) - this client only knows about the Gateway's HTTP API.
"""

from __future__ import annotations

import uuid

import httpx

from .models import Extraction, RequestExtracted

SYSTEM_PROMPT = (
    "You are a triage information-extraction assistant for a patient "
    "dispatch system. Your only job is to extract structured signals "
    "from a patient's free-text request so it can be routed to an "
    "available healthcare professional. You do not diagnose, you do "
    "not suggest treatment, and you do not give medical advice. If the "
    "text is ambiguous or vague, reflect that with a lower confidence "
    "score rather than guessing specifics. Always respond by calling "
    "the extract_patient_request function."
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
            "required": [
                "summary",
                "symptoms",
                "red_flags",
                "specialty_guess",
                "patient_age_group",
                "urgency_signals",
                "language",
                "confidence",
            ],
            "properties": {
                "summary": {
                    "type": "string",
                    "description": "One neutral sentence summarizing the request",
                },
                "symptoms": {
                    "type": "array",
                    "items": {"type": "string"},
                    "description": "Symptoms/complaints as described by the patient",
                },
                "red_flags": {
                    "type": "array",
                    "items": {"type": "string"},
                    "description": "Phrases suggesting a potential emergency, if any",
                },
                "specialty_guess": {
                    "type": "string",
                    "description": "Best-guess relevant specialty, for routing only",
                },
                "patient_age_group": {
                    "type": "string",
                    "enum": ["infant", "child", "adult", "elderly", "unknown"],
                },
                "urgency_signals": {
                    "type": "array",
                    "items": {"type": "string"},
                    "description": "Textual cues used to gauge urgency",
                },
                "language": {
                    "type": "string",
                    "description": "ISO-ish language code of the raw text, e.g. 'fr', 'en'",
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
    """Raised when the Gateway can't produce a usable extraction."""


class ExtractionClient:
    def __init__(self, gateway_url: str, timeout: float = 30.0):
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

        extraction = Extraction.model_validate(tool_call["arguments"])

        return RequestExtracted.build(
            request_id=request_id,
            model=data["model"],
            extraction=extraction,
        )