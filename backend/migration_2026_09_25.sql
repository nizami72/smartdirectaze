-- Migration script for 2026-09-25
-- Backfill for the "Business as aggregate root" refactor (35a17f7): data created before it
-- has duplicate businesses per (user, industry) and shops without business_id.

BEGIN;

-- AI mode of a channel (OFF / TEST / ON). Existing channels start in TEST: the AI must not answer
-- real customers before the merchant checked it. Needed before ddl-auto adds the NOT NULL column.
ALTER TABLE ai_channels ADD COLUMN IF NOT EXISTS ai_mode VARCHAR(16) NOT NULL DEFAULT 'TEST';

-- Business kept for every (user, industry): the one with the smallest id
CREATE TEMP TABLE kept_business ON COMMIT DROP AS
SELECT DISTINCT ON (bm.user_id, b.industry) bm.user_id, b.industry, b.id AS business_id
FROM business_member bm
         JOIN business b ON b.id = bm.business_id
ORDER BY bm.user_id, b.industry, b.id;

CREATE TEMP TABLE duplicate_business ON COMMIT DROP AS
SELECT b.id AS business_id, k.business_id AS kept_id
FROM business_member bm
         JOIN business b ON b.id = bm.business_id
         JOIN kept_business k ON k.user_id = bm.user_id AND k.industry = b.industry
WHERE b.id <> k.business_id;

-- 1. Move shops and events of duplicates to the kept business, then drop the duplicates
UPDATE shops s SET business_id = d.kept_id FROM duplicate_business d WHERE s.business_id = d.business_id;
UPDATE event e SET business_id = d.kept_id FROM duplicate_business d WHERE e.business_id = d.business_id;
DELETE FROM business_member bm USING duplicate_business d WHERE bm.business_id = d.business_id;
DELETE FROM business b USING duplicate_business d WHERE b.id = d.business_id;

-- 2. Link shops created before the refactor to their owner's SHOP business
UPDATE shops s
SET business_id = k.business_id
FROM kept_business k
WHERE s.business_id IS NULL
  AND k.user_id = s.owner_id
  AND k.industry = 'SHOP';

COMMIT;
