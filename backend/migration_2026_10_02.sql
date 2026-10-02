-- PostgreSQL. Also represented by AiChannelEntity.riskAcceptedAt for existing ddl-auto:update installations.
-- When the merchant confirmed the WhatsApp ban risk; the QR is given only after that.
ALTER TABLE ai_channels ADD COLUMN IF NOT EXISTS risk_accepted_at TIMESTAMP(6);
