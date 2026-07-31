"""
Redis-backed completion cache. Cache key is a sha256 hash of every input
that affects the output: provider, model, system_prompt, user_prompt,
temperature, and now also tools + tool_choice (a request with tools
passed is a materially different request from one without, so it must
not collide in the cache with a plain-text completion for the same
prompts).
"""

from __future__ import annotations

import hashlib
import json
from dataclasses import asdict
from typing import Any, Dict, List, Optional

import redis

from providers import CompletionResult, ToolCallResult


def _cache_key(
    provider: str,
    model: str,
    system_prompt: str,
    user_prompt: str,
    temperature: float,
    tools: Optional[List[Dict[str, Any]]],
    tool_choice: Optional[Dict[str, Any]],
) -> str:
    payload = json.dumps(
        {
            "provider": provider,
            "model": model,
            "system_prompt": system_prompt,
            "user_prompt": user_prompt,
            "temperature": temperature,
            "tools": tools,
            "tool_choice": tool_choice,
        },
        sort_keys=True,
    )
    digest = hashlib.sha256(payload.encode("utf-8")).hexdigest()
    return f"llm-gateway:completion:{digest}"


class CompletionCache:
    def __init__(self, redis_client: redis.Redis, ttl_seconds: int):
        self._redis = redis_client
        self._ttl = ttl_seconds

    def get(
        self,
        provider: str,
        model: str,
        system_prompt: str,
        user_prompt: str,
        temperature: float,
        tools: Optional[List[Dict[str, Any]]] = None,
        tool_choice: Optional[Dict[str, Any]] = None,
    ) -> Optional[CompletionResult]:
        key = _cache_key(
            provider, model, system_prompt, user_prompt, temperature, tools, tool_choice
        )
        raw = self._redis.get(key)
        if raw is None:
            return None
        data = json.loads(raw)
        tool_call = None
        if data.get("tool_call"):
            tool_call = ToolCallResult(**data["tool_call"])
        return CompletionResult(
            provider=data["provider"],
            model=data["model"],
            text=data.get("text"),
            tool_call=tool_call,
        )

    def set(
        self,
        provider: str,
        model: str,
        system_prompt: str,
        user_prompt: str,
        temperature: float,
        result: CompletionResult,
        tools: Optional[List[Dict[str, Any]]] = None,
        tool_choice: Optional[Dict[str, Any]] = None,
    ) -> None:
        key = _cache_key(
            provider, model, system_prompt, user_prompt, temperature, tools, tool_choice
        )
        serializable = asdict(result)
        self._redis.set(key, json.dumps(serializable), ex=self._ttl)
