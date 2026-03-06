-- Migration: Refactor worked_muscles for per-muscle 48-hour expiry
-- Drops old table and creates new schema with one row per user per muscle group.
-- Run manually: psql -d musclehead -U jordanmolina21 -f src/main/resources/db/migration/worked-muscles-per-muscle-expiry.sql

DROP TABLE IF EXISTS worked_muscles;

CREATE TABLE worked_muscles (
  id          BIGSERIAL PRIMARY KEY,
  user_id     VARCHAR(255) NOT NULL REFERENCES users(sub_id) ON DELETE CASCADE,
  muscle_group VARCHAR(100) NOT NULL,
  expires_at  TIMESTAMP NOT NULL,
  UNIQUE(user_id, muscle_group)
);

CREATE INDEX idx_worked_muscles_user_expires ON worked_muscles(user_id, expires_at);
