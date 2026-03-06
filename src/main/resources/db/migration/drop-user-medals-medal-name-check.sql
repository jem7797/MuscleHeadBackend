-- Drop the check constraint on medal_name so new enum values are allowed.
-- Hibernate may have added this when the table was created; it only allows
-- the enum values that existed at that time.
ALTER TABLE user_medals DROP CONSTRAINT IF EXISTS user_medals_medal_name_check;
