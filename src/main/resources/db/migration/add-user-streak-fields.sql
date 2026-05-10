ALTER TABLE users
    ADD COLUMN IF NOT EXISTS current_streak INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS longest_streak INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS last_workout_date DATE,
    ADD COLUMN IF NOT EXISTS streak_status VARCHAR(20) NOT NULL DEFAULT 'BROKEN',
    ADD COLUMN IF NOT EXISTS grace_period_start DATE;

ALTER TABLE users
    DROP CONSTRAINT IF EXISTS users_streak_status_check;

ALTER TABLE users
    ADD CONSTRAINT users_streak_status_check
    CHECK (streak_status IN ('ACTIVE', 'AT_RISK', 'BROKEN'));
