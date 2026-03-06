-- Migration: Add achievement post support to posts table
-- achievement_id references user_medals (achievements). One post per achievement per user.
-- Run manually: psql -d musclehead -U jordanmolina21 -f src/main/resources/db/migration/posts-achievement-support.sql

ALTER TABLE posts
ADD COLUMN IF NOT EXISTS is_trophy BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS achievement_id BIGINT REFERENCES user_medals(id);

-- Enforce one post per achievement per user (achievement_id NULL allowed for regular posts)
ALTER TABLE posts
ADD CONSTRAINT unique_user_achievement UNIQUE (user_sub_id, achievement_id);
