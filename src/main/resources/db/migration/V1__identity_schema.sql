-- Identity module (cahier des charges, section 7.3 and section 12.1)
-- organizations, groups, users, and supervisions (host <-> group, many-to-many, arbitrage 8.1)
--
-- Table names are plural ("groups", "users") because "GROUP" and "USER" are
-- reserved SQL keywords on their own; the plural form avoids any quoting.
--
-- Audit columns (is_deleted, created_date, updated_date, deleted_date) are
-- populated by Spring Data JPA Auditing — see BaseEntity / JpaAuditingConfig.

CREATE TABLE organizations (
    id            UUID PRIMARY KEY,
    company_name  VARCHAR(255) NOT NULL,
    is_deleted    BOOLEAN NOT NULL DEFAULT FALSE,
    created_date  TIMESTAMPTZ NOT NULL,
    updated_date  TIMESTAMPTZ,
    deleted_date  TIMESTAMPTZ
);

CREATE TABLE groups (
    id              UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    group_name      VARCHAR(255) NOT NULL,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    created_date    TIMESTAMPTZ NOT NULL,
    updated_date    TIMESTAMPTZ,
    deleted_date    TIMESTAMPTZ
);

CREATE INDEX idx_groups_organization_id ON groups (organization_id);

CREATE TABLE users (
    id             UUID PRIMARY KEY,
    group_id       UUID NOT NULL REFERENCES groups (id),
    first_name     VARCHAR(100) NOT NULL,
    last_name      VARCHAR(100) NOT NULL,
    email          VARCHAR(255) NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    role           VARCHAR(20) NOT NULL CHECK (role IN ('SUPER_ADMIN', 'HOST', 'MEMBER')),
    is_deleted     BOOLEAN NOT NULL DEFAULT FALSE,
    created_date   TIMESTAMPTZ NOT NULL,
    updated_date   TIMESTAMPTZ,
    deleted_date   TIMESTAMPTZ
);

CREATE INDEX idx_users_group_id ON users (group_id);

-- A host (user) can supervise several groups.
-- Distinct from users.group_id, which remains the primary membership (relevant for members).
CREATE TABLE supervisions (
    user_id  UUID NOT NULL REFERENCES users (id),
    group_id UUID NOT NULL REFERENCES groups (id),
    PRIMARY KEY (user_id, group_id)
);

CREATE INDEX idx_supervisions_group_id ON supervisions (group_id);
