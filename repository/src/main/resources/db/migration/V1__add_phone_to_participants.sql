-- Add phone column to participants table
ALTER TABLE participants ADD COLUMN IF NOT EXISTS phone VARCHAR(255);

-- Add index for phone column to improve query performance
CREATE INDEX IF NOT EXISTS idx_participant_phone ON participants(phone);
