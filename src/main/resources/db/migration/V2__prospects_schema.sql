-- Prospects module (cahier des charges, section 7.3)
-- prospects, actions
--
-- Note: prospects.current_stage_id (pipeline stage, id_etape_actuelle in the
-- cahier des charges) is intentionally NOT included yet — it will be added
-- via a later migration once the pipeline module exists.

CREATE TABLE prospects (
    id               UUID PRIMARY KEY,
    group_id         UUID NOT NULL REFERENCES groups (id),
    assigned_user_id UUID NOT NULL REFERENCES users (id),
    name             VARCHAR(255) NOT NULL,
    phone            VARCHAR(50),
    email            VARCHAR(255),
    company_name     VARCHAR(255),
    source           VARCHAR(20) NOT NULL CHECK (source IN ('MANUAL', 'VOICE', 'OCR')),
    is_deleted       BOOLEAN NOT NULL DEFAULT FALSE,
    created_date     TIMESTAMPTZ NOT NULL,
    updated_date     TIMESTAMPTZ,
    deleted_date     TIMESTAMPTZ
);

CREATE INDEX idx_prospects_group_id ON prospects (group_id);
CREATE INDEX idx_prospects_assigned_user_id ON prospects (assigned_user_id);

CREATE TABLE actions (
    id             UUID PRIMARY KEY,
    prospect_id    UUID NOT NULL REFERENCES prospects (id),
    type           VARCHAR(20) NOT NULL CHECK (type IN ('CALL', 'SMS', 'FIELD_VISIT', 'VOICE_NOTE')),
    description    TEXT,
    voice_note_url VARCHAR(500),
    due_date       TIMESTAMPTZ,
    status         VARCHAR(20) NOT NULL CHECK (status IN ('TO_DO', 'DONE')),
    is_deleted     BOOLEAN NOT NULL DEFAULT FALSE,
    created_date   TIMESTAMPTZ NOT NULL,
    updated_date   TIMESTAMPTZ,
    deleted_date   TIMESTAMPTZ
);

CREATE INDEX idx_actions_prospect_id ON actions (prospect_id);
