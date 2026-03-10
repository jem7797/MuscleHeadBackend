-- Add unique constraint for ON CONFLICT upsert in worked_muscles
-- Run in Supabase SQL editor or: psql -d postgres -U postgres -f src/main/resources/db/migration/add-worked-muscles-unique-constraint.sql

ALTER TABLE worked_muscles
ADD CONSTRAINT worked_muscles_user_muscle_unique
UNIQUE (user_id, muscle_group);
