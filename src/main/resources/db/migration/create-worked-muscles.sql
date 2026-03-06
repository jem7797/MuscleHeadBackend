-- Migration: Create worked_muscles table
-- Stores canonical muscle groups per user (Chest, Arms, Shoulders, Back, Legs, Glutes, Calves, Abs, Core, Traps)

CREATE TABLE IF NOT EXISTS worked_muscles (
    user_id VARCHAR(255) PRIMARY KEY REFERENCES users(sub_id) ON DELETE CASCADE,
    muscle_groups TEXT[] NOT NULL DEFAULT '{}',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
