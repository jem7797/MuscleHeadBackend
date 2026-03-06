-- Migration: Split Arms into Triceps and Biceps in worked_muscles
-- The backend no longer uses the coarse "Arms" canonical; it now stores Triceps, Biceps, Forearms separately.
-- Run manually: psql -d musclehead -U jordanmolina21 -f src/main/resources/db/migration/worked-muscles-split-arms.sql

UPDATE worked_muscles
SET muscle_groups = ARRAY(
    SELECT DISTINCT unnest(array_remove(muscle_groups, 'Arms') || ARRAY['Triceps', 'Biceps'])
    ORDER BY 1
)
WHERE 'Arms' = ANY(muscle_groups);
