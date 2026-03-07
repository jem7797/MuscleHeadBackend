-- Delete user blaster7797 (case insensitive) and all related data.
-- Run: psql -d musclehead -U jordanmolina21 -f src/main/resources/db/delete-user-blaster7797.sql

DO $$
DECLARE
  target_sub_id VARCHAR(255);
BEGIN
  SELECT sub_id INTO target_sub_id FROM users WHERE LOWER(username) = 'blaster7797';
  IF target_sub_id IS NULL THEN
    RAISE NOTICE 'User blaster7797 not found';
    RETURN;
  END IF;

  RAISE NOTICE 'Deleting user sub_id: %', target_sub_id;

  -- Comments on this user's posts, and comments by this user
  DELETE FROM comments WHERE user_sub_id = target_sub_id
    OR post_id IN (SELECT post_id FROM posts WHERE user_sub_id = target_sub_id);
  -- Likes by this user, and likes on this user's posts
  DELETE FROM post_likes WHERE user_sub_id = target_sub_id
    OR post_id IN (SELECT post_id FROM posts WHERE user_sub_id = target_sub_id);
  DELETE FROM posts WHERE user_sub_id = target_sub_id;

  DELETE FROM notifications WHERE sub_id = target_sub_id;
  DELETE FROM user_medals WHERE user_id = target_sub_id;
  DELETE FROM workout_exercises WHERE sub_id = target_sub_id;
  DELETE FROM workout_sessions WHERE sub_id = target_sub_id;
  DELETE FROM follows WHERE follower_sub_id = target_sub_id OR followee_sub_id = target_sub_id;
  DELETE FROM workout_schedule WHERE user_sub_id = target_sub_id;
  DELETE FROM routine_exercises WHERE routine_id IN (SELECT id FROM routine WHERE user_sub_id = target_sub_id);
  DELETE FROM routine WHERE user_sub_id = target_sub_id;
  DELETE FROM user_workout_schedule WHERE user_sub_id = target_sub_id;
  DELETE FROM worked_muscles WHERE user_id = target_sub_id;

  DELETE FROM users_nemesis WHERE user_sub_id = target_sub_id OR nemesis_sub_id = target_sub_id;

  DELETE FROM users WHERE sub_id = target_sub_id;

  RAISE NOTICE 'User blaster7797 deleted successfully';
END $$;
