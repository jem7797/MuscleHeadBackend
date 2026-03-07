-- Add gender column to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS gender VARCHAR(20);

-- Set Jordan7797 as female (case-sensitive match)
UPDATE users SET gender = 'female' WHERE username = 'Jordan7797';
