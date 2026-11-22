import os

REDIS_HOST = os.getenv("REDIS_HOST", "localhost")
REDIS_PORT = int(os.getenv("REDIS_PORT", "6379"))

POSTGRES_DSN = os.getenv(
    "EVALUATION_DB_DSN",
    "postgresql://pulsaride:pulsaride@localhost:5433/evaluation_db",
)

CONSUMER_GROUP = os.getenv("EVALUATION_CONSUMER_GROUP", "evaluation-group")
CONSUMER_NAME = os.getenv("EVALUATION_CONSUMER_NAME", "evaluation-1")

STREAMS = [
    "requests:submitted",
    "requests:extracted",
    "requests:urgency_score",
    "requests:proposed",
    "requests:accepted",
    "requests:refused",
    "requests:closed",
]