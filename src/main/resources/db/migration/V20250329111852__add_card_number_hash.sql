-- Add card_number_hash column for efficient searching
-- SHA-256 hash allows search without decrypting all records
ALTER TABLE card ADD COLUMN card_number_hash VARCHAR(64) NULL;

-- Create index for fast lookups
CREATE UNIQUE INDEX idx_card_number_hash ON card(card_number_hash);

-- Note: Existing cards will need to be updated with hash values
-- This can be done via application logic on first run or manual migration
