-- pgvector is needed by matching_db (IA3 — Matching: embeddings + cosine
-- similarity, Phase 4 of the plan). Enabling it now costs nothing and
-- saves you a migration later.
--
-- \connect switches this script's target DB — required because
-- CREATE EXTENSION only affects the database you're currently in.

\connect matching_db
CREATE EXTENSION IF NOT EXISTS vector;