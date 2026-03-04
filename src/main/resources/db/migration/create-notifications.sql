-- Migration: Recreate notifications table with new schema
-- WARNING: This drops existing notifications. Run only if you need the new schema.
--
-- New schema: id, sub_id, type, message, is_read, created_at
-- Types: NEMESIS_POST, LEVEL_UP, MEDAL_EARNED, FOLLOW

DROP TABLE IF EXISTS notifications CASCADE;

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    sub_id VARCHAR(255) NOT NULL REFERENCES users(sub_id),
    type VARCHAR(50) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_sub_id ON notifications(sub_id);
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);
