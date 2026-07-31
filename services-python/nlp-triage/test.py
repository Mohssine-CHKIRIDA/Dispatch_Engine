"""
Tests for ExtractionClient against the real models.py (Extraction,
RequestExtracted, AgeGroup) - httpx.post is still mocked (no real
network / Gateway needed), but the Pydantic validation, enum handling,
and RequestExtracted.build() wiring are all exercised for real.
"""

import sys
import os
import uuid

sys.path.insert(0, os.path.join(os.path.dirname(__file__), "..", ".."))

import httpx
import pytest

import llm_client as llm_client_module
from llm_client import ExtractionClient, GatewayError, EXTRACTION_FUNCTION
from llm_client import AgeGroup, RequestExtracted


class FakeResponse:
    def __init__(self, status_code, json_data=None, text=""):
        self.status_code = status_code
        self._json_data = json_data or {}
        self.text = text

    def json(self):
        return self._json_data


VALID_ARGS = {
    "summary": "patient reports chest pain",
    "symptoms": ["chest pain", "shortness of breath"],
    "red_flags": ["chest pain"],
    "specialty_guess": "cardiology",
    "patient_age_group": "adult",
    "urgency_signals": ["chest pain"],
    "language": "fr",
    "confidence": 0.85,
}


def test_extract_success_produces_real_pydantic_objects(monkeypatch):
    def fake_post(url, json, timeout):
        assert url == "http://gateway:8000/v1/complete"
        assert json["tools"] == [EXTRACTION_FUNCTION]
        assert json["tool_choice"] == {
            "type": "function",
            "function": {"name": "extract_patient_request"},
        }
        assert "poitrine" in json["user_prompt"]
        return FakeResponse(
            200,
            {
                "provider": "nvidia",
                "model": "moonshotai/kimi-k2.6",
                "cached": False,
                "text": None,
                "tool_call": {"name": "extract_patient_request", "arguments": VALID_ARGS},
            },
        )

    monkeypatch.setattr(llm_client_module.httpx, "post", fake_post)

    gw_client = ExtractionClient(gateway_url="http://gateway:8000")
    request_id = uuid.uuid4()
    result = gw_client.extract("j'ai mal à la poitrine", request_id)

    # Real Pydantic model this time, not a dict stand-in.
    assert isinstance(result, RequestExtracted)
    assert result.request_id == request_id
    assert result.model == "moonshotai/kimi-k2.6"
    assert result.event_type == "request.extracted"
    assert result.extraction.patient_age_group == AgeGroup.adult
    assert result.extraction.confidence == 0.85
    assert result.extraction.symptoms == ["chest pain", "shortness of breath"]


def test_extract_raises_on_invalid_extraction_shape(monkeypatch):
    """If the Gateway/model returns arguments that don't satisfy the
    Extraction schema (e.g. bad enum value), Pydantic validation should
    raise - and since ExtractionClient doesn't catch ValidationError,
    it should propagate up rather than being silently swallowed."""

    bad_args = dict(VALID_ARGS)
    bad_args["patient_age_group"] = "toddler"  # not a valid AgeGroup

    def fake_post(url, json, timeout):
        return FakeResponse(
            200,
            {
                "provider": "nvidia",
                "model": "moonshotai/kimi-k2.6",
                "tool_call": {"name": "extract_patient_request", "arguments": bad_args},
            },
        )

    monkeypatch.setattr(llm_client_module.httpx, "post", fake_post)

    gw_client = ExtractionClient(gateway_url="http://gateway:8000")
    with pytest.raises(Exception):  # pydantic.ValidationError
        gw_client.extract("x", uuid.uuid4())


def test_extract_raises_on_missing_tool_call(monkeypatch):
    def fake_post(url, json, timeout):
        return FakeResponse(
            200,
            {"provider": "ollama", "model": "mistral", "tool_call": None, "text": "oops"},
        )

    monkeypatch.setattr(llm_client_module.httpx, "post", fake_post)

    gw_client = ExtractionClient(gateway_url="http://gateway:8000")
    with pytest.raises(GatewayError, match="did not return a tool call"):
        gw_client.extract("x", uuid.uuid4())


def test_extract_raises_on_non_200(monkeypatch):
    def fake_post(url, json, timeout):
        return FakeResponse(502, {}, text="All providers failed: nvidia down; ollama down")

    monkeypatch.setattr(llm_client_module.httpx, "post", fake_post)

    gw_client = ExtractionClient(gateway_url="http://gateway:8000")
    with pytest.raises(GatewayError, match="502"):
        gw_client.extract("x", uuid.uuid4())


def test_extract_raises_on_connection_error(monkeypatch):
    def fake_post(url, json, timeout):
        raise httpx.ConnectError("connection refused")

    monkeypatch.setattr(llm_client_module.httpx, "post", fake_post)

    gw_client = ExtractionClient(gateway_url="http://gateway:8000")
    with pytest.raises(GatewayError, match="Could not reach LLM Gateway"):
        gw_client.extract("x", uuid.uuid4())