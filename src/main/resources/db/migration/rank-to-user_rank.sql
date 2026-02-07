-- Migration: Rename rank table to user_rank to avoid PostgreSQL keyword conflicts
-- Run this BEFORE restarting the app if you have existing data to preserve.
--
-- If you're okay with a fresh start, you can instead:
--   DROP TABLE IF EXISTS rank CASCADE;
--   (Hibernate will create user_rank and the seeder will populate it)

-- 1. Rename the rank table to user_rank
ALTER TABLE IF EXISTS rank RENAME TO user_rank;

-- 2. Rename the foreign key column in users table
ALTER TABLE users RENAME COLUMN rank_id TO user_rank_id;
