"""
Calls Kimi K2.6, hosted as an NVIDIA NIM endpoint, with a forced
function call so the response is always a well-formed structured
object, never free text we'd have to hope is valid JSON. This is the
"first LLM" in the pipeline: triage extraction only. It does not
diagnose, prescribe, or give medical advice - it turns the patient's
free text into structured routing signals that urgency-scoring and
matching consume next.

NVIDIA's integrate.api.nvidia.com endpoint is OpenAI-compatible, so we
use the `openai` SDK pointed at NVIDIA's base_url rather than raw
`requests` calls or a Moonshot-specific SDK. If you ever move off
NVIDIA (straight to Moonshot, or a self-hosted vLLM deployment), only
the base_url/model name/api key change - this client code stays as is.
"""

from __future__ import annotations

import json
import uuid

from openai import OpenAI

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


class ExtractionClient:
    def __init__(self, api_key: str, base_url: str, model: str):
        self._client = OpenAI(api_key=api_key, base_url=base_url)
        self._model = model

    def extract(self, raw_text: str, request_id: uuid.UUID) -> RequestExtracted:
        response = self._client.chat.completions.create(
            model=self._model,
            extra_body={"chat_template_kwargs": {"thinking": False}},
            temperature=0.6,
            tools=[EXTRACTION_FUNCTION],
            tool_choice={
                "type": "function",
                "function": {"name": "extract_patient_request"},
            },
            messages=[
                {"role": "system", "content": SYSTEM_PROMPT},
                {"role": "user", "content": f"Patient request:\n\n{raw_text}"},
            ],
        )

        message = response.choices[0].message
        if not message.tool_calls:
            raise RuntimeError(
                "Kimi did not return a tool call despite tool_choice being forced "
                f"(finish_reason={response.choices[0].finish_reason})"
            )

        tool_call = message.tool_calls[0]
        arguments = json.loads(tool_call.function.arguments)
        extraction = Extraction.model_validate(arguments)

        return RequestExtracted.build(
            request_id=request_id,
            model=self._model,
            extraction=extraction,
        )
