ALTER TABLE resourcecapital.capital_allocations
    ADD COLUMN IF NOT EXISTS user_id UUID,
    ADD COLUMN IF NOT EXISTS released_amount NUMERIC(19, 4) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS is_over_allocated BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS over_allocation_confirmed BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS note VARCHAR(1000);

UPDATE resourcecapital.capital_allocations allocation
SET user_id = cycle.owner_id
FROM resourcecapital.capital_cycles cycle
WHERE allocation.capital_cycle_id = cycle.id
  AND allocation.user_id IS NULL;

UPDATE resourcecapital.capital_allocations
SET released_amount = 0
WHERE released_amount IS NULL;

UPDATE resourcecapital.capital_allocations
SET is_over_allocated = FALSE
WHERE is_over_allocated IS NULL;

UPDATE resourcecapital.capital_allocations
SET over_allocation_confirmed = FALSE
WHERE over_allocation_confirmed IS NULL;

ALTER TABLE resourcecapital.capital_allocations
    ALTER COLUMN user_id SET NOT NULL,
    ALTER COLUMN released_amount SET NOT NULL,
    ALTER COLUMN released_amount SET DEFAULT 0,
    ALTER COLUMN is_over_allocated SET NOT NULL,
    ALTER COLUMN is_over_allocated SET DEFAULT FALSE,
    ALTER COLUMN over_allocation_confirmed SET NOT NULL,
    ALTER COLUMN over_allocation_confirmed SET DEFAULT FALSE;

ALTER TABLE resourcecapital.capital_allocations
    DROP CONSTRAINT IF EXISTS chk_capital_allocations_target_type,
    DROP CONSTRAINT IF EXISTS chk_capital_allocations_status;

ALTER TABLE resourcecapital.capital_allocations
    ADD CONSTRAINT chk_capital_allocations_target_type
        CHECK (target_type IN ('TASK', 'TASK_CATALOG', 'PROJECT')),
    ADD CONSTRAINT chk_capital_allocations_status
        CHECK (status IN ('ACTIVE', 'REALLOCATED', 'RELEASED', 'CLOSED'));

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_capital_allocations_released_amount'
          AND conrelid = 'resourcecapital.capital_allocations'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_allocations
            ADD CONSTRAINT chk_capital_allocations_released_amount
            CHECK (released_amount >= 0);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_capital_allocations_over_allocation_flags'
          AND conrelid = 'resourcecapital.capital_allocations'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_allocations
            ADD CONSTRAINT chk_capital_allocations_over_allocation_flags
            CHECK (over_allocation_confirmed = FALSE OR is_over_allocated = TRUE);
    END IF;
END $$;
