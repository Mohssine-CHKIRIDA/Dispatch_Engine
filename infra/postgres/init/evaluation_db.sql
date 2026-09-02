CREATE TABLE IF NOT EXISTS request_lifecycle (
    request_id UUID PRIMARY KEY,
    patient_id TEXT,
    submitted_at TIMESTAMPTZ,
    extracted_at TIMESTAMPTZ,
    urgency_score INT,
    urgency_scored_at TIMESTAMPTZ,
    closed_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS proposals (
    id SERIAL PRIMARY KEY,
    request_id UUID NOT NULL,
    doctor_id TEXT NOT NULL,
    match_score DOUBLE PRECISION,
    strategy TEXT,
    proposed_at TIMESTAMPTZ NOT NULL,
    outcome TEXT,               -- 'pending' | 'accepted' | 'refused' | 'timeout'
    outcome_at TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_proposals_request_id ON proposals(request_id);