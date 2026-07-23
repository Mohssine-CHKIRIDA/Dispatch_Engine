import os
from dataclasses import dataclass

from dotenv import load_dotenv

load_dotenv()


@dataclass(frozen=True)
class Settings:
    nvidia_api_key: str
    nvidia_base_url: str
    redis_host: str
    redis_port: int
    source_stream: str
    target_stream: str
    consumer_group: str
    llm_model: str


def load_settings() -> Settings:
    return Settings(
        nvidia_api_key=_require("NVIDIA_API_KEY"),
        nvidia_base_url=os.getenv("NVIDIA_BASE_URL", "https://integrate.api.nvidia.com/v1"),
        redis_host=os.getenv("REDIS_HOST", "localhost"),
        redis_port=int(os.getenv("REDIS_PORT", "6379")),
        source_stream=os.getenv("SOURCE_STREAM", "requests:submitted"),
        target_stream=os.getenv("TARGET_STREAM", "requests:extracted"),
        consumer_group=os.getenv("CONSUMER_GROUP", "nlp-triage-group"),
        llm_model=os.getenv("LLM_MODEL", "moonshotai/kimi-k2.6"),
    )


def _require(name: str) -> str:
    value = os.getenv(name)
    if not value:
        raise RuntimeError(f"Missing required environment variable: {name}")
    return value
