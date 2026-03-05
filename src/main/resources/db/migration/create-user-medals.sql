-- Migration: Create user_medals table
-- Columns: id, user_id, medal_name, awarded_at

CREATE TABLE IF NOT EXISTS user_medals (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL REFERENCES users(sub_id),
    medal_name VARCHAR(100) NOT NULL,
    awarded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_medals_user_id ON user_medals(user_id);
CREATE INDEX idx_user_medals_awarded_at ON user_medals(awarded_at DESC);
