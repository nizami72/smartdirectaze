-- Migration script for 2026-06-03

-- Add registration_step to users table
ALTER TABLE users ADD COLUMN registration_step VARCHAR(255) DEFAULT 'ACCOUNT_CREATED';

-- Add channel setting fields to shops table
ALTER TABLE shops ADD COLUMN telegram_bot_token VARCHAR(255);
ALTER TABLE shops ADD COLUMN telegram_bot_username VARCHAR(255);
ALTER TABLE shops ADD COLUMN whatsapp_instance_id VARCHAR(255);
ALTER TABLE shops ADD COLUMN whatsapp_token VARCHAR(255);
ALTER TABLE shops ADD COLUMN whatsapp_status VARCHAR(255);
ALTER TABLE shops ADD COLUMN telegram_status VARCHAR(255);
