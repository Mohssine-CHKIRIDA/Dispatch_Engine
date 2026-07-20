"""
Pulsaride shared event models (Python side).

Generated from contracts/events/*.schema.json via datamodel-code-generator.
Do NOT hand-edit the individual *_schema.py files or common/envelope_schema.py —
regenerate them from the JSON Schemas instead (see scripts/generate-models.sh).

Usage:
    from shared.events import RequestSubmitted, SubmittedPayload, EventEnvelope
"""

from .common.envelope_schema import EventEnvelope
from .request_submitted_schema import RequestSubmitted, SubmittedPayload
from .request_extracted_schema import RequestExtracted, ExtractedPayload
from .request_urgency_score_schema import RequestUrgencyScore, UrgencyScorePayload
from .request_case_embedded_schema import RequestCaseEmbedded, CaseEmbeddedPayload
from .request_proposed_schema import RequestProposed, ProposedPayload, Strategy as ProposalStrategy
from .request_accepted_schema import RequestAccepted, AcceptedPayload
from .request_refused_schema import RequestRefused, RefusedPayload, Reason as RefusalReason
from .request_closed_schema import RequestClosed, ClosedPayload, Outcome as ClosureOutcome
from .pro_profile_updated_schema import ProProfileUpdated, ProfileUpdatedPayload
from .pro_availability_changed_schema import ProAvailabilityChanged, AvailabilityChangedPayload

__all__ = [
    "EventEnvelope",
    "RequestSubmitted",
    "SubmittedPayload",
    "RequestExtracted",
    "ExtractedPayload",
    "RequestUrgencyScore",
    "UrgencyScorePayload",
    "RequestCaseEmbedded",
    "CaseEmbeddedPayload",
    "RequestProposed",
    "ProposedPayload",
    "ProposalStrategy",
    "RequestAccepted",
    "AcceptedPayload",
    "RequestRefused",
    "RefusedPayload",
    "RefusalReason",
    "RequestClosed",
    "ClosedPayload",
    "ClosureOutcome",
    "ProProfileUpdated",
    "ProfileUpdatedPayload",
    "ProAvailabilityChanged",
    "AvailabilityChangedPayload",
]
