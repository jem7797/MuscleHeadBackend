-- Add profile_pic_version for cache-busting (CloudFront/browser).
-- When profilePicUrl changes, backend sets this to current timestamp.
-- Frontend appends ?v={profilePicVersion} to image URL to bypass cache.
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_pic_version BIGINT;
