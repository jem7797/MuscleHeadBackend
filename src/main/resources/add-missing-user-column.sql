-- Add missing column expected by User entity (e.g. after schema drift).
-- Run once against your musclehead DB: psql -U jordanmolina21 -d musclehead -f src/main/resources/add-missing-user-column.sql
-- Or in psql: \i path/to/add-missing-user-column.sql

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS highest_weight_lifted DOUBLE PRECISION DEFAULT 0;
