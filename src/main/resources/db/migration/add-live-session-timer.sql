ALTER TABLE live_workout_sessions
    ADD COLUMN IF NOT EXISTS timer_state VARCHAR(20) NOT NULL DEFAULT 'STOPPED',
    ADD COLUMN IF NOT EXISTS timer_started_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS timer_elapsed_seconds BIGINT NOT NULL DEFAULT 0;

ALTER TABLE live_workout_sessions
    DROP CONSTRAINT IF EXISTS live_workout_sessions_timer_state_check;

ALTER TABLE live_workout_sessions
    ADD CONSTRAINT live_workout_sessions_timer_state_check
    CHECK (timer_state IN ('STOPPED', 'RUNNING', 'PAUSED'));
