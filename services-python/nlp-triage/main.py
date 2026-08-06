import logging

from config import load_settings
from consumer import TriageConsumer
from llm_client import ExtractionClient
import os
import sys

sys.path.insert(
    0,
    os.path.join(os.path.dirname(os.path.abspath(__file__)), ".."),
)

def main() -> None:
    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s %(levelname)s %(name)s - %(message)s",
    )

    settings = load_settings()
    extraction_client = ExtractionClient(gateway_url=settings.gateway_url)
    consumer = TriageConsumer(settings, extraction_client)
    consumer.run_forever()


if __name__ == "__main__":
    main()