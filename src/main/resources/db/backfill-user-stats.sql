-- Backfill user stats from workout_sessions for users whose aggregates are out of sync.
-- Run when users have workout_sessions but lifetime_weight_lifted, etc. are 0.

UPDATE users u SET
  lifetime_weight_lifted = COALESCE(agg.total_weight, 0),
  lifetime_gym_time = COALESCE(agg.total_gym_time, 0),
  highest_weight_lifted = GREATEST(COALESCE(u.highest_weight_lifted, 0), COALESCE(agg.max_lift, 0)),
  xp = COALESCE(agg.session_count, 0)
FROM (
  SELECT
    sub_id,
    SUM(total_weight_lifted) AS total_weight,
    SUM(time_spent_in_gym) AS total_gym_time,
    MAX(session_highest_lift) AS max_lift,
    COUNT(*) AS session_count
  FROM workout_sessions
  GROUP BY sub_id
) agg
WHERE u.sub_id = agg.sub_id;
