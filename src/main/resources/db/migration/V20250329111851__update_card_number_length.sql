-- Update card_number column to support encrypted data storage
ALTER TABLE card MODIFY COLUMN card_number VARCHAR(255) NOT NULL;
