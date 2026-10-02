-- PostgreSQL. Also represented by AiChannelEntity.riskAcceptedAt for existing ddl-auto:update installations.
-- When the merchant confirmed the WhatsApp ban risk; the QR is given only after that.
ALTER TABLE ai_channels ADD COLUMN IF NOT EXISTS risk_accepted_at TIMESTAMP(6);

-- Pilot terms (/terms) accepted at registration: when and which version of the text.
ALTER TABLE users ADD COLUMN IF NOT EXISTS terms_accepted_at TIMESTAMP(6);
ALTER TABLE users ADD COLUMN IF NOT EXISTS terms_version VARCHAR(32);
