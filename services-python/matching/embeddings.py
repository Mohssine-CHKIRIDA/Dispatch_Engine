"""
Thin wrapper around sentence-transformers. The model is loaded lazily
(on first .embed() call) so importing this module never triggers a
real model download. Not verified against a real download in this
environment - test that for real on your machine.
"""

from __future__ import annotations

from typing import List


class EmbeddingModel:
    def __init__(self, model_name: str):
        self._model_name = model_name
        self._model = None

    def _ensure_loaded(self) -> None:
        if self._model is None:
            from sentence_transformers import SentenceTransformer

            self._model = SentenceTransformer(self._model_name)

    def embed(self, text: str) -> List[float]:
        self._ensure_loaded()
        vector = self._model.encode(text, normalize_embeddings=True)
        return vector.tolist()
