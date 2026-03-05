-- Add medal info to notifications for MEDAL_EARNED type
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS medal_id BIGINT;
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS medal_name VARCHAR(100);
