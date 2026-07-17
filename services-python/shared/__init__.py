"""
Pulsaride shared event models (Python side).

Generated from contracts/events/*.schema.json via datamodel-code-generator.
Do NOT hand-edit the individual *_schema.py files or common/envelope_schema.py —
regenerate them from the JSON Schemas instead (see scripts/generate-models.sh).

Usage:
    from shared.events import RequestSubmitted, RequestExtracted, EventEnvelope
"""

from events.common.envelope_schema import EventEnvelope
from events.request_submitted_schema import RequestSubmitted
from events.request_extracted_schema import RequestExtracted
from events.request_urgency_score_schema import RequestUrgencyScore
from events.request_case_embedded_schema import RequestCaseEmbedded
from events.request_proposed_schema import RequestProposed, Strategy as ProposalStrategy
from events.request_accepted_schema import RequestAccepted
from events.request_refused_schema import RequestRefused, Reason as RefusalReason
from events.request_closed_schema import RequestClosed, Outcome as ClosureOutcome
from events.pro_profile_updated_schema import ProProfileUpdated
from events.pro_availability_changed_schema import ProAvailabilityChanged

__all__ = [
    "EventEnvelope",
    "RequestSubmitted",
    "RequestExtracted",
    "RequestUrgencyScore",
    "RequestCaseEmbedded",
    "RequestProposed",
    "ProposalStrategy",
    "RequestAccepted",
    "RequestRefused",
    "RefusalReason",
    "RequestClosed",
    "ClosureOutcome",
    "ProProfileUpdated",
    "ProAvailabilityChanged",
]
