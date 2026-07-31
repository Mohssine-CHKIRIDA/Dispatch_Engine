"""
Tests for the extended Gateway. Follows the project's established
pattern: fakeredis instead of real Redis, fake providers instead of
real HTTP calls to NVIDIA/Ollama, FastAPI TestClient for the HTTP
layer. No live network or Docker needed to run these.
"""

import sys
import os

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

import fakeredis
import pytest
from fastapi.testclient import TestClient

import app as gateway_app
from cache import CompletionCache
from providers import CompletionResult, LLMProvider, ProviderError, ToolCallResult


class FakeProvider(LLMProvider):
    """Test double: returns a canned result, or raises, on demand."""

    def __init__(self, name, model="fake-model", result=None, error=None):
        self.name = name
        self._model = model
        self._result = result
        self._error = error
        self.call_count = 0

    def complete(self, system_prompt, user_prompt, temperature, tools=None, tool_choice=None):
        self.call_count += 1
        if self._error is not None:
            raise self._error
        return self._result


@pytest.fixture
def client(monkeypatch):
    fake_redis = fakeredis.FakeRedis(decode_responses=True)
    test_cache = CompletionCache(fake_redis, ttl_seconds=3600)
    monkeypatch.setattr(gateway_app, "cache", test_cache)
    return TestClient(gateway_app.app), fake_redis


def _set_chain(monkeypatch, providers):
    monkeypatch.setattr(gateway_app, "PROVIDER_CHAIN", providers)


def test_health_check(client, monkeypatch):
    test_client, _ = client
    fake = FakeProvider("nvidia")
    _set_chain(monkeypatch, [fake])
    monkeypatch.setattr(gateway_app, "_all_providers", {"nvidia": fake})

    resp = test_client.get("/health")
    assert resp.status_code == 200
    assert resp.json()["providers_configured"] == ["nvidia"]


def test_cache_miss_then_hit(client, monkeypatch):
    test_client, _ = client
    result = CompletionResult(provider="nvidia", model="fake-model", text="hello")
    fake = FakeProvider("nvidia", result=result)
    _set_chain(monkeypatch, [fake])

    body = {"system_prompt": "sys", "user_prompt": "hi", "temperature": 0.5}

    resp1 = test_client.post("/v1/complete", json=body)
    assert resp1.status_code == 200
    assert resp1.json()["cached"] is False
    assert fake.call_count == 1

    resp2 = test_client.post("/v1/complete", json=body)
    assert resp2.status_code == 200
    assert resp2.json()["cached"] is True
    assert fake.call_count == 1  # not called again - served from cache


def test_skip_cache_bypasses_cache(client, monkeypatch):
    test_client, _ = client
    result = CompletionResult(provider="nvidia", model="fake-model", text="hello")
    fake = FakeProvider("nvidia", result=result)
    _set_chain(monkeypatch, [fake])

    body = {"system_prompt": "sys", "user_prompt": "hi", "temperature": 0.5, "skip_cache": True}
    test_client.post("/v1/complete", json=body)
    test_client.post("/v1/complete", json=body)
    assert fake.call_count == 2


def test_fallback_to_next_provider(client, monkeypatch):
    test_client, _ = client
    failing = FakeProvider("nvidia", error=ProviderError("nvidia down"))
    result = CompletionResult(provider="ollama", model="mistral", text="fallback text")
    working = FakeProvider("ollama", model="mistral", result=result)
    _set_chain(monkeypatch, [failing, working])

    body = {"system_prompt": "sys", "user_prompt": "hi", "temperature": 0.5}
    resp = test_client.post("/v1/complete", json=body)

    assert resp.status_code == 200
    assert resp.json()["provider"] == "ollama"
    assert failing.call_count == 1
    assert working.call_count == 1


def test_all_providers_fail_returns_502(client, monkeypatch):
    test_client, _ = client
    p1 = FakeProvider("nvidia", error=ProviderError("nvidia down"))
    p2 = FakeProvider("ollama", error=ProviderError("ollama down"))
    _set_chain(monkeypatch, [p1, p2])

    body = {"system_prompt": "sys", "user_prompt": "hi", "temperature": 0.5}
    resp = test_client.post("/v1/complete", json=body)

    assert resp.status_code == 502
    assert "nvidia down" in resp.json()["detail"]
    assert "ollama down" in resp.json()["detail"]


def test_tool_call_passthrough(client, monkeypatch):
    test_client, _ = client
    tool_result = CompletionResult(
        provider="nvidia",
        model="moonshotai/kimi-k2.6",
        tool_call=ToolCallResult(
            name="extract_patient_request",
            arguments={"summary": "patient reports chest pain", "confidence": 0.8},
        ),
    )
    fake = FakeProvider("nvidia", result=tool_result)
    _set_chain(monkeypatch, [fake])

    body = {
        "system_prompt": "sys",
        "user_prompt": "j'ai mal à la poitrine",
        "temperature": 0.6,
        "tools": [{"type": "function", "function": {"name": "extract_patient_request"}}],
        "tool_choice": {"type": "function", "function": {"name": "extract_patient_request"}},
    }
    resp = test_client.post("/v1/complete", json=body)

    assert resp.status_code == 200
    data = resp.json()
    assert data["tool_call"]["name"] == "extract_patient_request"
    assert data["tool_call"]["arguments"]["confidence"] == 0.8
    assert data["text"] is None


def test_tool_choice_without_tools_is_422(client, monkeypatch):
    test_client, _ = client
    _set_chain(monkeypatch, [FakeProvider("nvidia")])

    body = {
        "system_prompt": "sys",
        "user_prompt": "hi",
        "temperature": 0.5,
        "tool_choice": {"type": "function", "function": {"name": "x"}},
    }
    resp = test_client.post("/v1/complete", json=body)
    assert resp.status_code == 422


def test_no_providers_configured_returns_502(client, monkeypatch):
    test_client, _ = client
    _set_chain(monkeypatch, [])

    body = {"system_prompt": "sys", "user_prompt": "hi", "temperature": 0.5}
    resp = test_client.post("/v1/complete", json=body)
    assert resp.status_code == 502
