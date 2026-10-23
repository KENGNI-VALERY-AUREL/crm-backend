-- Pipeline module (cahier des charges, sections 7.3 and 7.5)
-- pipelines, pipeline_stages, stage_history + prospects.current_stage_id
--
-- V1 scope: one pipeline per organization, customizable by HOST / SUPER_ADMIN
-- (multiple simultaneous pipelines are V2, but the schema already allows them).
--
-- pipeline_stages.outcome is an addition to the cahier des charges' ETAPE_PIPELINE:
-- it marks terminal stages (WON / LOST) so the lifecycle of section 7.5 and the
-- future conversion-rate reporting do not depend on stage *names*, which are
-- user-defined.

CREATE TABLE pipelines (
    id                 UUID PRIMARY KEY,
    organization_id    UUID NOT NULL REFERENCES organizations (id),
    created_by_user_id UUID REFERENCES users (id),
    name               VARCHAR(255) NOT NULL,
    is_deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    created_date       TIMESTAMPTZ NOT NULL,
    updated_date       TIMESTAMPTZ,
    deleted_date       TIMESTAMPTZ
);

CREATE INDEX idx_pipelines_organization_id ON pipelines (organization_id);

CREATE TABLE pipeline_stages (
    id           UUID PRIMARY KEY,
    pipeline_id  UUID NOT NULL REFERENCES pipelines (id),
    stage_name   VARCHAR(100) NOT NULL,
    sort_order   INTEGER NOT NULL,
    outcome      VARCHAR(10) NOT NULL DEFAULT 'OPEN' CHECK (outcome IN ('OPEN', 'WON', 'LOST')),
    is_deleted   BOOLEAN NOT NULL DEFAULT FALSE,
    created_date TIMESTAMPTZ NOT NULL,
    updated_date TIMESTAMPTZ,
    deleted_date TIMESTAMPTZ
);

CREATE INDEX idx_pipeline_stages_pipeline_id ON pipeline_stages (pipeline_id);

-- Append-only audit trail of stage moves (HISTORIQUE_ETAPE): never updated or deleted.
CREATE TABLE stage_history (
    id                 UUID PRIMARY KEY,
    prospect_id        UUID NOT NULL REFERENCES prospects (id),
    from_stage_id      UUID REFERENCES pipeline_stages (id),
    to_stage_id        UUID NOT NULL REFERENCES pipeline_stages (id),
    changed_by_user_id UUID NOT NULL REFERENCES users (id),
    reason             VARCHAR(500),
    changed_date       TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_stage_history_prospect_id ON stage_history (prospect_id);

ALTER TABLE prospects ADD COLUMN current_stage_id UUID REFERENCES pipeline_stages (id);
CREATE INDEX idx_prospects_current_stage_id ON prospects (current_stage_id);

-- Backfill for data created before this migration: every existing organization gets
-- the default pipeline (same stages as PipelineService.DEFAULT_STAGES) and every
-- existing prospect starts on its first stage.
INSERT INTO pipelines (id, organization_id, name, is_deleted, created_date)
SELECT gen_random_uuid(), o.id, 'Default pipeline', FALSE, NOW()
FROM organizations o;

INSERT INTO pipeline_stages (id, pipeline_id, stage_name, sort_order, outcome, is_deleted, created_date)
SELECT gen_random_uuid(), p.id, s.stage_name, s.sort_order, s.outcome, FALSE, NOW()
FROM pipelines p
CROSS JOIN (VALUES
    ('New',        0, 'OPEN'),
    ('Contacted',  1, 'OPEN'),
    ('Offer sent', 2, 'OPEN'),
    ('Won',        3, 'WON'),
    ('Lost',       4, 'LOST')
) AS s (stage_name, sort_order, outcome);

UPDATE prospects pr
SET current_stage_id = st.id
FROM groups g
JOIN pipelines pl ON pl.organization_id = g.organization_id
JOIN pipeline_stages st ON st.pipeline_id = pl.id AND st.sort_order = 0
WHERE pr.group_id = g.id;

ALTER TABLE prospects ALTER COLUMN current_stage_id SET NOT NULL;
