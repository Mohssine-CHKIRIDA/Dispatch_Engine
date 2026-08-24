CREATE TABLE IF NOT EXISTS pro_profiles (
    id UUID PRIMARY KEY,
    specialty VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    current_load INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);