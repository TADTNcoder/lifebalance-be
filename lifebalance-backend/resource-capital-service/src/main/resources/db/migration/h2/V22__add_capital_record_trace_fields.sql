ALTER TABLE resourcecapital.capital_adjustments
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE resourcecapital.capital_adjustments
    ADD COLUMN IF NOT EXISTS created_by UUID;

ALTER TABLE resourcecapital.capital_adjustments
    ADD COLUMN IF NOT EXISTS updated_by UUID;

UPDATE resourcecapital.capital_adjustments
SET updated_at = created_at
WHERE updated_at IS NULL;

UPDATE resourcecapital.capital_adjustments
SET created_by = user_id
WHERE created_by IS NULL;

UPDATE resourcecapital.capital_adjustments
SET updated_by = user_id
WHERE updated_by IS NULL;

ALTER TABLE resourcecapital.capital_adjustments
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE resourcecapital.capital_adjustments
    ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE resourcecapital.capital_allocations
    ADD COLUMN IF NOT EXISTS created_by UUID;

ALTER TABLE resourcecapital.capital_allocations
    ADD COLUMN IF NOT EXISTS updated_by UUID;

UPDATE resourcecapital.capital_allocations
SET created_by = user_id
WHERE created_by IS NULL;

UPDATE resourcecapital.capital_allocations
SET updated_by = user_id
WHERE updated_by IS NULL;

CREATE INDEX IF NOT EXISTS idx_cap_adj_created_by_created_at
    ON resourcecapital.capital_adjustments (created_by, created_at DESC, id DESC);

CREATE INDEX IF NOT EXISTS idx_cap_adj_updated_by_updated_at
    ON resourcecapital.capital_adjustments (updated_by, updated_at DESC, id DESC);

CREATE INDEX IF NOT EXISTS idx_cap_alloc_created_by_created_at
    ON resourcecapital.capital_allocations (created_by, created_at DESC, id DESC);

CREATE INDEX IF NOT EXISTS idx_cap_alloc_updated_by_updated_at
    ON resourcecapital.capital_allocations (updated_by, updated_at DESC, id DESC);

COMMENT ON COLUMN resourcecapital.capital_adjustments.updated_at IS
    'Technical trace timestamp. Adjustment records are append-only, so this normally mirrors created_at.';

COMMENT ON COLUMN resourcecapital.capital_adjustments.created_by IS
    'Technical actor reference used for traceability; business action history remains in capital_histories.';

COMMENT ON COLUMN resourcecapital.capital_adjustments.updated_by IS
    'Technical last actor reference used for traceability; business action history remains in capital_histories.';

COMMENT ON COLUMN resourcecapital.capital_allocations.created_by IS
    'Technical actor reference used for traceability; allocation lifecycle history remains in capital_histories.';

COMMENT ON COLUMN resourcecapital.capital_allocations.updated_by IS
    'Technical last actor reference used for traceability; allocation lifecycle history remains in capital_histories.';
