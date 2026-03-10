-- Migration: Create live workout session tables
-- Run this in Supabase SQL editor if tables don't exist yet.
--
-- Tables: live_workout_sessions, session_invites, live_session_exercises

CREATE TABLE IF NOT EXISTS live_workout_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    host_user_id VARCHAR(255) NOT NULL REFERENCES users(sub_id),
    guest_user_id VARCHAR(255) REFERENCES users(sub_id),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'in_progress', 'ENDED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS session_invites (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL REFERENCES live_workout_sessions(id) ON DELETE CASCADE,
    from_user_id VARCHAR(255) NOT NULL REFERENCES users(sub_id),
    to_user_id VARCHAR(255) NOT NULL REFERENCES users(sub_id),
    message VARCHAR(1000),
    status VARCHAR(50) NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'accepted', 'declined')),
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS live_session_exercises (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL REFERENCES live_workout_sessions(id) ON DELETE CASCADE,
    user_id VARCHAR(255) NOT NULL REFERENCES users(sub_id),
    exercise_name VARCHAR(255) NOT NULL,
    sets INTEGER,
    reps INTEGER,
    weight DECIMAL(10, 2),
    logged_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_live_workout_sessions_host_user_id ON live_workout_sessions(host_user_id);
CREATE INDEX IF NOT EXISTS idx_live_workout_sessions_guest_user_id ON live_workout_sessions(guest_user_id);
CREATE INDEX IF NOT EXISTS idx_live_workout_sessions_status ON live_workout_sessions(status);

CREATE INDEX IF NOT EXISTS idx_session_invites_session_id ON session_invites(session_id);
CREATE INDEX IF NOT EXISTS idx_session_invites_to_user_id ON session_invites(to_user_id);
CREATE INDEX IF NOT EXISTS idx_session_invites_status ON session_invites(status);

CREATE INDEX IF NOT EXISTS idx_live_session_exercises_session_id ON live_session_exercises(session_id);
CREATE INDEX IF NOT EXISTS idx_live_session_exercises_user_id ON live_session_exercises(user_id);
