import logging

from .config import load_settings
from .consumer import TriageConsumer
from .llm_client import ExtractionClient


def main() -> None:
    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s %(levelname)s %(name)s - %(message)s",
    )

    settings = load_settings()
    extraction_client = ExtractionClient(
        api_key=settings.nvidia_api_key,
        base_url=settings.nvidia_base_url,
        model=settings.llm_model,
    )
    consumer = TriageConsumer(settings, extraction_client)
    consumer.run_forever()


if __name__ == "__main__":
    main()
