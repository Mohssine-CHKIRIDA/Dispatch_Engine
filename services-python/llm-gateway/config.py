"""
Gateway configuration. Mirrors the pattern already used in nlp-triage
(frozen dataclass + load_settings() + dotenv) so both services read the
same way. Provider-specific settings are Optional: a provider that's
missing its config is simply left out of the provider chain at startup
(logged as a warning), rather than crashing the whole Gateway. This
matters because not every environment will have every provider
configured (e.g. local dev with only Ollama running).
"""

from __future__ import annotations

import os
from dataclasses import dataclass, field
from typing import List, Optional

from dotenv import load_dotenv

load_dotenv()


@dataclass(frozen=True)
class Settings:
    redis_host: str
    redis_port: int
    cache_ttl_seconds: int

    provider_order: List[str]  # e.g. ["nvidia", "anthropic", "ollama"]

    # NVIDIA NIM (OpenAI-compatible) - same env var names as nlp-triage,
    # so a single .env can serve both services.
    nvidia_api_key: Optional[str] = None
    nvidia_base_url: str = "https://integrate.api.nvidia.com/v1"
    nvidia_model: str = "moonshotai/kimi-k2.6"

    # Groq (OpenAI-compatible) - fast inference, generous free tier.
    # GROQ_MODEL is configurable since Groq deprecates/swaps models on
    # short notice. moonshotai/kimi-k2-instruct-0905 was deprecated
    # (March 2026) in favor of openai/gpt-oss-120b, which is Groq's
    # current recommended flagship model and supports tool-calling.
    # Check https://console.groq.com/docs/models for what's live.
    groq_api_key: Optional[str] = None
    groq_base_url: str = "https://api.groq.com/openai/v1"
    groq_model: str = "openai/gpt-oss-120b"

    # Anthropic - stub/inert until a key exists.
    anthropic_api_key: Optional[str] = None
    anthropic_model: str = "claude-haiku-4-5-20251001"

    # Ollama - local fallback, no key needed. Timeout is generous on
    # purpose: CPU-only hardware can take 60-90+ seconds to load a 7B
    # model into memory on the first request after the container starts
    # (subsequent requests are fast while the model stays warm via
    # Ollama's keep_alive).
    ollama_base_url: str = "http://localhost:11434"
    ollama_model: str = "mistral"
    ollama_timeout_seconds: float = 180.0


def load_settings() -> Settings:
    order = os.getenv("PROVIDER_ORDER", "groq,nvidia,ollama")
    return Settings(
        redis_host=os.getenv("REDIS_HOST", "localhost"),
        redis_port=int(os.getenv("REDIS_PORT", "6379")),
        cache_ttl_seconds=int(os.getenv("CACHE_TTL_SECONDS", "3600")),
        provider_order=[p.strip() for p in order.split(",") if p.strip()],
        nvidia_api_key=os.getenv("NVIDIA_API_KEY"),
        nvidia_base_url=os.getenv("NVIDIA_BASE_URL", "https://integrate.api.nvidia.com/v1"),
        nvidia_model=os.getenv("NVIDIA_MODEL", "moonshotai/kimi-k2.6"),
        groq_api_key=os.getenv("GROQ_API_KEY"),
        groq_base_url=os.getenv("GROQ_BASE_URL", "https://api.groq.com/openai/v1"),
        groq_model=os.getenv("GROQ_MODEL", "openai/gpt-oss-120b"),
        anthropic_api_key=os.getenv("ANTHROPIC_API_KEY"),
        anthropic_model=os.getenv("ANTHROPIC_MODEL", "claude-haiku-4-5-20251001"),
        ollama_base_url=os.getenv("OLLAMA_BASE_URL", "http://localhost:11434"),
        ollama_model=os.getenv("OLLAMA_MODEL", "mistral"),
        ollama_timeout_seconds=float(os.getenv("OLLAMA_TIMEOUT_SECONDS", "180")),
    )