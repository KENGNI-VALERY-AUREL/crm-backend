-- Users can now be (soft-)deleted through the API (UC-201). The plain UNIQUE
-- constraint on users.email also counts soft-deleted rows, so the email of a
-- removed user could never be reused (and the insert would fail with a 500, since
-- the application-level check ignores deleted users). Uniqueness is enforced on
-- active users only.

ALTER TABLE users DROP CONSTRAINT IF EXISTS users_email_key;

CREATE UNIQUE INDEX uq_users_email_active ON users (email) WHERE is_deleted = FALSE;
