-- Wipe all achievements (medals) from all users.
-- Run manually: psql -d musclehead -U jordanmolina21 -f src/main/resources/db/migration/wipe-user-medals.sql

-- Delete medal-earned notifications first (they reference medal ids)
DELETE FROM notifications WHERE type = 'MEDAL_EARNED';

-- Delete all user medals
DELETE FROM user_medals;
