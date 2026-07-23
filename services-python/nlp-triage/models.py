from __future__ import annotations

import uuid
from datetime import datetime, timezone
from enum import Enum
from typing import Optional

from pydantic import BaseModel, Field


# ---------- incoming: request.submitted ----------

class RequestSubmitted(BaseModel):
    event_id: uuid.UUID
    event_type: str
    event_version: int
    occurred_at: datetime

    request_id: uuid.UUID
    submitted_at: datetime
    raw_text: str
    requested_specialty: Optional[str] = None

    lat: Optional[float] = None
    lng: Optional[float] = None
    location_label: Optional[str] = Field(default=None, alias="locationLabel")

    contact_channel: Optional[str] = None
    contact_value: Optional[str] = None
    source: Optional[str] = "web"

    class Config:
        populate_by_name = True


# ---------- outgoing: request.extracted ----------

class AgeGroup(str, Enum):
    infant = "infant"
    child = "child"
    adult = "adult"
    elderly = "elderly"
    unknown = "unknown"


class Extraction(BaseModel):
    summary: str
    symptoms: list[str]
    red_flags: list[str]
    specialty_guess: str
    patient_age_group: AgeGroup
    urgency_signals: list[str]
    language: str
    confidence: float = Field(ge=0.0, le=1.0)


class RequestExtracted(BaseModel):
    event_id: uuid.UUID
    event_type: str = "request.extracted"
    event_version: int = 1
    occurred_at: datetime
    request_id: uuid.UUID
    model: str
    extraction: Extraction

    @staticmethod
    def build(request_id: uuid.UUID, model: str, extraction: Extraction) -> "RequestExtracted":
        return RequestExtracted(
            event_id=uuid.uuid4(),
            occurred_at=datetime.now(timezone.utc),
            request_id=request_id,
            model=model,
            extraction=extraction,
        )
