import logging

from config import load_settings
from consumer import UrgencyConsumer
from llm_client import UrgencyClient


def main() -> None:
    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s %(levelname)s %(name)s - %(message)s",
    )

    settings = load_settings()
    urgency_client = UrgencyClient(gateway_url=settings.gateway_url)
    consumer = UrgencyConsumer(settings, urgency_client)
    consumer.run_forever()


if __name__ == "__main__":
    main()
