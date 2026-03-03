-- Migration: Add ON DELETE CASCADE to post_likes foreign key
-- Run this to fix: "update or delete on table posts violates foreign key constraint on table post_likes"
--
-- If the DROP fails, run: \d post_likes (in psql) to get the actual FK constraint name.

ALTER TABLE post_likes DROP CONSTRAINT IF EXISTS fka5wxsgl4doibhbed9gm7ikie2;
ALTER TABLE post_likes
    ADD CONSTRAINT fk_post_likes_post
    FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE;
