import os
from dataclasses import dataclass

from dotenv import load_dotenv

load_dotenv()


@dataclass(frozen=True)
class Settings:
    gateway_url: str
    redis_host: str
    redis_port: int
    source_stream: str
    target_stream: str
    consumer_group: str


def load_settings() -> Settings:
    return Settings(
        gateway_url=os.getenv("GATEWAY_URL", "http://localhost:8000"),
        redis_host=os.getenv("REDIS_HOST", "localhost"),
        redis_port=int(os.getenv("REDIS_PORT", "6379")),
        source_stream=os.getenv("SOURCE_STREAM", "requests:submitted"),
        target_stream=os.getenv("TARGET_STREAM", "requests:extracted"),
        consumer_group=os.getenv("CONSUMER_GROUP", "nlp-triage-group"),
    )