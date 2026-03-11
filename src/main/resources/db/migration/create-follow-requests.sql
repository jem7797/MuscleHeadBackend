-- Migration: Create follow_requests table for private-account follow requests
-- Run in Supabase SQL editor if needed. Hibernate ddl-auto=update will also create it.

CREATE TABLE IF NOT EXISTS follow_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requester_sub_id VARCHAR(255) NOT NULL REFERENCES users(sub_id),
    followee_sub_id VARCHAR(255) NOT NULL REFERENCES users(sub_id),
    status VARCHAR(50) NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'accepted', 'declined')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_follow_requests_followee ON follow_requests(followee_sub_id);
CREATE INDEX IF NOT EXISTS idx_follow_requests_requester_followee ON follow_requests(requester_sub_id, followee_sub_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_follow_requests_pending ON follow_requests(requester_sub_id, followee_sub_id) WHERE status = 'pending';
