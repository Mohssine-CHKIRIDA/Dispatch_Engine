"""
LLM Gateway. Single entry point for any service that needs an LLM
completion - plain text (IA2 urgency scoring) or a forced tool call
(IA1 triage, via the `tools` + `tool_choice` fields). Providers are
tried in the order configured by PROVIDER_ORDER, with the same
provider+model+prompt(+tools) combination cached in Redis so repeated
or near-identical requests don't re-hit a paid API.
"""

from __future__ import annotations

from typing import Any, Dict, List, Optional

import redis
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

from cache import CompletionCache
from config import load_settings
from providers import LLMProvider, NvidiaProvider, GroqProvider, AnthropicProvider, OllamaProvider, ProviderError

settings = load_settings()

redis_client = redis.Redis(
    host=settings.redis_host, port=settings.redis_port, decode_responses=True
)
cache = CompletionCache(redis_client, ttl_seconds=settings.cache_ttl_seconds)

app = FastAPI(title="Pulsaride LLM Gateway")


def _build_providers() -> Dict[str, LLMProvider]:
    """
    Only instantiate providers whose config is actually present. A
    provider missing its key/config is skipped (not a crash) so the
    Gateway still boots in environments that only have e.g. Ollama.
    """
    available: Dict[str, LLMProvider] = {}

    if settings.nvidia_api_key:
        available["nvidia"] = NvidiaProvider(
            api_key=settings.nvidia_api_key,
            base_url=settings.nvidia_base_url,
            model=settings.nvidia_model,
        )
    if settings.groq_api_key:
        available["groq"] = GroqProvider(
            api_key=settings.groq_api_key,
            base_url=settings.groq_base_url,
            model=settings.groq_model,
        )
    if settings.anthropic_api_key:
        available["anthropic"] = AnthropicProvider(
            api_key=settings.anthropic_api_key, model=settings.anthropic_model
        )
    # Ollama has no required key - always available if configured in order.
    available["ollama"] = OllamaProvider(
        base_url=settings.ollama_base_url,
        model=settings.ollama_model,
        timeout_seconds=settings.ollama_timeout_seconds,
    )

    return available


_all_providers = _build_providers()
PROVIDER_CHAIN: List[LLMProvider] = [
    _all_providers[name] for name in settings.provider_order if name in _all_providers
]


class CompleteRequest(BaseModel):
    system_prompt: str
    user_prompt: str
    temperature: float = 0.7
    skip_cache: bool = False
    tools: Optional[List[Dict[str, Any]]] = None
    tool_choice: Optional[Dict[str, Any]] = None


class ToolCallResponse(BaseModel):
    name: str
    arguments: Dict[str, Any]


class CompleteResponse(BaseModel):
    provider: str
    model: str
    cached: bool
    text: Optional[str] = None
    tool_call: Optional[ToolCallResponse] = None


@app.get("/health")
def health() -> Dict[str, Any]:
    return {
        "status": "ok",
        "providers_configured": list(_all_providers.keys()),
        "provider_order": [p.name for p in PROVIDER_CHAIN],
    }


@app.post("/v1/complete", response_model=CompleteResponse)
def complete(request: CompleteRequest) -> CompleteResponse:
    if not PROVIDER_CHAIN:
        raise HTTPException(
            status_code=502, detail="No LLM providers configured"
        )

    if request.tool_choice and not request.tools:
        raise HTTPException(
            status_code=422, detail="tool_choice requires tools to be set"
        )

    errors: List[str] = []

    for provider in PROVIDER_CHAIN:
        model = _model_for(provider)

        if not request.skip_cache:
            cached_result = cache.get(
                provider.name,
                model,
                request.system_prompt,
                request.user_prompt,
                request.temperature,
                request.tools,
                request.tool_choice,
            )
            if cached_result is not None:
                return _to_response(cached_result, cached=True)

        try:
            result = provider.complete(
                request.system_prompt,
                request.user_prompt,
                request.temperature,
                tools=request.tools,
                tool_choice=request.tool_choice,
            )
        except ProviderError as exc:
            errors.append(f"{provider.name}: {exc}")
            continue

        if not request.skip_cache:
            cache.set(
                provider.name,
                model,
                request.system_prompt,
                request.user_prompt,
                request.temperature,
                result,
                request.tools,
                request.tool_choice,
            )

        return _to_response(result, cached=False)

    raise HTTPException(
        status_code=502,
        detail=f"All providers failed: {'; '.join(errors)}",
    )


def _model_for(provider: LLMProvider) -> str:
    # Small helper so the cache key always reflects the model actually
    # configured for that provider instance.
    return getattr(provider, "_model", "unknown")


def _to_response(result, cached: bool) -> CompleteResponse:
    tool_call = None
    if result.tool_call is not None:
        tool_call = ToolCallResponse(
            name=result.tool_call.name, arguments=result.tool_call.arguments
        )
    return CompleteResponse(
        provider=result.provider,
        model=result.model,
        cached=cached,
        text=result.text,
        tool_call=tool_call,
    )