-- Migration: Add actor_sub_id to notifications for FOLLOW (and other actor-based) notifications
-- Run in Supabase SQL editor if needed. Hibernate ddl-auto=update will also add the column.

ALTER TABLE notifications ADD COLUMN IF NOT EXISTS actor_sub_id VARCHAR(255);
