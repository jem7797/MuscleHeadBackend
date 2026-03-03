-- Migration: Create workout_schedule table
-- Hibernate ddl-auto=update will create this automatically.
-- Run manually only if needed (e.g. production with ddl-auto=validate).

CREATE TABLE IF NOT EXISTS workout_schedule (
    id BIGSERIAL PRIMARY KEY,
    user_sub_id VARCHAR(255) NOT NULL REFERENCES users(sub_id),
    day_of_the_week INTEGER NOT NULL CHECK (day_of_the_week >= 1 AND day_of_the_week <= 7)
);

CREATE INDEX IF NOT EXISTS idx_workout_schedule_user_sub_id ON workout_schedule(user_sub_id);
