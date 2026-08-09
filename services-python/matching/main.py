import logging

import db
from config import load_settings
from consumer import MatchingConsumer
from embeddings import EmbeddingModel


def main() -> None:
    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s %(levelname)s %(name)s - %(message)s",
    )

    settings = load_settings()
    embedding_model = EmbeddingModel(settings.embedding_model_name)

    pg_conn = db.get_connection(settings.postgres_dsn)
    # NOTE: embedding_dim isn't known until the model actually loads.
    # sentence-transformers exposes this without a full forward pass
    # via get_sentence_embedding_dimension() - call that once at
    # startup so ensure_schema can declare the pgvector column width.
    from sentence_transformers import SentenceTransformer

    probe_model = SentenceTransformer(settings.embedding_model_name)
    embedding_dim = probe_model.get_sentence_embedding_dimension()

    db.ensure_schema(pg_conn, embedding_dim)

    consumer = MatchingConsumer(settings, embedding_model, pg_conn)
    consumer.run_forever()


if __name__ == "__main__":
    main()
