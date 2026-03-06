-- Wipe users and posts for fresh testing.
-- Run manually: psql -d musclehead -U jordanmolina21 -f src/main/resources/db/migration/wipe-users-and-posts.sql
-- Order matters for FK constraints.

-- Post-related
DELETE FROM post_likes;
DELETE FROM comments;
DELETE FROM posts;

-- User-related (order by dependency)
DELETE FROM notifications;
DELETE FROM user_medals;
DELETE FROM workout_exercises;
DELETE FROM workout_sessions;
DELETE FROM follows;
DELETE FROM workout_schedule;
DELETE FROM routine_exercises;
DELETE FROM routine;
DELETE FROM user_workout_schedule;

-- Nemesis join table (Hibernate default: users_nemesis)
DELETE FROM users_nemesis;

-- Users last
DELETE FROM users;

-- Under-13 blocked emails
DELETE FROM blocked_emails;
