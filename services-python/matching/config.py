import os
from dataclasses import dataclass

from dotenv import load_dotenv

load_dotenv()


@dataclass(frozen=True)
class Settings:
    redis_host: str
    redis_port: int
    source_stream: str
    target_stream: str
    consumer_group: str

    postgres_dsn: str
    embedding_model_name: str


def load_settings() -> Settings:
    return Settings(
        redis_host=os.getenv("REDIS_HOST", "localhost"),
        redis_port=int(os.getenv("REDIS_PORT", "6379")),
        source_stream=os.getenv("SOURCE_STREAM", "requests:extracted"),
        target_stream=os.getenv("TARGET_STREAM", "requests:case_embedded"),
        consumer_group=os.getenv("CONSUMER_GROUP", "matching-group"),
        postgres_dsn=os.getenv(
            "MATCHING_DB_DSN",
            "postgresql://pulsaride:pulsaride@localhost:5433/matching_db",
        ),
        # Multilingual since patient texts are French; 384-dim.
        embedding_model_name=os.getenv(
            "EMBEDDING_MODEL", "paraphrase-multilingual-MiniLM-L12-v2"
        ),
    )
