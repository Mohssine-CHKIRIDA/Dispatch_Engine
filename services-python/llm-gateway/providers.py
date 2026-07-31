"""
Provider abstraction for the LLM Gateway.

Every provider implements .complete(), which either:
  - returns a CompletionResult with `text` set (plain completion), or
  - returns a CompletionResult with `tool_call` set (forced function-call
    result), when the caller passed `tools` + `tool_choice`.

Exception handling stays broad on purpose (bare `except Exception`), not
narrowed to specific HTTP/SDK error types. The Gateway's whole reason to
exist is graceful fallback - a provider can fail in ways we didn't
anticipate (DNS failure, timeout, auth error, model not pulled locally,
malformed tool-call response, etc.) and all of those should trigger
fallback to the next provider, not bubble up as an unhandled 500.
"""

from __future__ import annotations

from abc import ABC, abstractmethod
from dataclasses import dataclass
from typing import Any, Dict, List, Optional

import httpx
from openai import OpenAI


class ProviderError(Exception):
    """Raised when a provider fails for any reason. Triggers fallback."""


@dataclass
class ToolCallResult:
    name: str
    arguments: Dict[str, Any]


@dataclass
class CompletionResult:
    provider: str
    model: str
    text: Optional[str] = None
    tool_call: Optional[ToolCallResult] = None


class LLMProvider(ABC):
    name: str

    @abstractmethod
    def complete(
        self,
        system_prompt: str,
        user_prompt: str,
        temperature: float,
        tools: Optional[List[Dict[str, Any]]] = None,
        tool_choice: Optional[Dict[str, Any]] = None,
    ) -> CompletionResult:
        ...


class OpenAICompatibleProvider(LLMProvider):
    """
    Generic provider for any OpenAI-compatible chat completions endpoint
    that supports forced function-calling via `tool_choice`. NVIDIA NIM
    and Groq both fit this shape - only api_key/base_url/model differ,
    so both are just instances of this class with a different `name`.
    """

    def __init__(self, name: str, api_key: str, base_url: str, model: str):
        self.name = name
        self._client = OpenAI(api_key=api_key, base_url=base_url)
        self._model = model

    def complete(
        self,
        system_prompt: str,
        user_prompt: str,
        temperature: float,
        tools: Optional[List[Dict[str, Any]]] = None,
        tool_choice: Optional[Dict[str, Any]] = None,
    ) -> CompletionResult:
        try:
            kwargs: Dict[str, Any] = {
                "model": self._model,
                "temperature": temperature,
                "messages": [
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": user_prompt},
                ],
            }
            if tools:
                kwargs["tools"] = tools
            if tool_choice:
                kwargs["tool_choice"] = tool_choice

            response = self._client.chat.completions.create(**kwargs)
            message = response.choices[0].message

            if tool_choice and not message.tool_calls:
                # We forced a tool call and didn't get one back - treat as
                # a provider failure so the Gateway falls back, rather than
                # silently handing the caller an empty result.
                raise ProviderError(
                    f"{self.name} did not return a tool call despite "
                    f"tool_choice being forced (finish_reason="
                    f"{response.choices[0].finish_reason})"
                )

            if message.tool_calls:
                import json

                call = message.tool_calls[0]
                return CompletionResult(
                    provider=self.name,
                    model=self._model,
                    tool_call=ToolCallResult(
                        name=call.function.name,
                        arguments=json.loads(call.function.arguments),
                    ),
                )

            return CompletionResult(
                provider=self.name, model=self._model, text=message.content
            )
        except ProviderError:
            raise
        except Exception as exc:  # noqa: BLE001 - intentional, see module docstring
            raise ProviderError(f"{self.name} provider failed: {exc}") from exc


# Backwards-compatible alias - NvidiaProvider is just this, named "nvidia".
def NvidiaProvider(api_key: str, base_url: str, model: str) -> OpenAICompatibleProvider:
    return OpenAICompatibleProvider(name="nvidia", api_key=api_key, base_url=base_url, model=model)


def GroqProvider(api_key: str, base_url: str, model: str) -> OpenAICompatibleProvider:
    return OpenAICompatibleProvider(name="groq", api_key=api_key, base_url=base_url, model=model)


class AnthropicProvider(LLMProvider):
    """
    Stub - inert without an API key. Kept provider-agnostic per the
    original design so a cloud key can be added later with zero code
    changes elsewhere. Not wired into tool-calling yet since it isn't in
    active use; extend when there's a real key to test against.
    """

    name = "anthropic"

    def __init__(self, api_key: str, model: str):
        self._api_key = api_key
        self._model = model

    def complete(
        self,
        system_prompt: str,
        user_prompt: str,
        temperature: float,
        tools: Optional[List[Dict[str, Any]]] = None,
        tool_choice: Optional[Dict[str, Any]] = None,
    ) -> CompletionResult:
        raise ProviderError("AnthropicProvider is a stub - no API key wired up yet")


class OllamaProvider(LLMProvider):
    """
    Local fallback via Ollama's /api/chat. Note: small local models
    (mistral 7B) are much less reliable at strict forced-function-calling
    than a hosted model like Kimi K2.6. If `tool_choice` is passed and
    Ollama doesn't honor it or returns unparseable arguments, this raises
    ProviderError like any other failure - but be aware that for
    tool-calling requests specifically, this fallback is weaker than it
    is for plain-text completions. Worth revisiting once IA1/IA2 are
    actually running against it.
    """

    name = "ollama"

    def __init__(self, base_url: str, model: str, timeout_seconds: float = 180.0):
        self._base_url = base_url.rstrip("/")
        self._model = model
        self._timeout = timeout_seconds

    def complete(
        self,
        system_prompt: str,
        user_prompt: str,
        temperature: float,
        tools: Optional[List[Dict[str, Any]]] = None,
        tool_choice: Optional[Dict[str, Any]] = None,
    ) -> CompletionResult:
        try:
            payload: Dict[str, Any] = {
                "model": self._model,
                "messages": [
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": user_prompt},
                ],
                "options": {"temperature": temperature},
                "stream": False,
            }
            if tools:
                payload["tools"] = tools

            response = httpx.post(
                f"{self._base_url}/api/chat", json=payload, timeout=self._timeout
            )
            response.raise_for_status()
            data = response.json()
            message = data["message"]

            tool_calls = message.get("tool_calls")
            if tool_choice and not tool_calls:
                raise ProviderError(
                    "Ollama did not return a tool call despite tool_choice "
                    "being requested"
                )

            if tool_calls:
                call = tool_calls[0]["function"]
                arguments = call["arguments"]
                if isinstance(arguments, str):
                    import json

                    arguments = json.loads(arguments)
                return CompletionResult(
                    provider=self.name,
                    model=self._model,
                    tool_call=ToolCallResult(name=call["name"], arguments=arguments),
                )

            return CompletionResult(
                provider=self.name, model=self._model, text=message.get("content", "")
            )
        except ProviderError:
            raise
        except Exception as exc:  # noqa: BLE001 - intentional, see module docstring
            raise ProviderError(f"Ollama provider failed: {exc}") from exc