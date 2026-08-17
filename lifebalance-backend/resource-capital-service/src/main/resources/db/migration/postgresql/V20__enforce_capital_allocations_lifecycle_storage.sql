UPDATE resourcecapital.capital_allocations allocation
SET user_id = cycle.owner_id
FROM resourcecapital.capital_cycles cycle
WHERE allocation.capital_cycle_id = cycle.id
  AND allocation.user_id <> cycle.owner_id;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_capital_cycles_id_owner'
          AND conrelid = 'resourcecapital.capital_cycles'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_cycles
            ADD CONSTRAINT uk_capital_cycles_id_owner
            UNIQUE (id, owner_id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_capital_allocations_owner_cycle'
          AND conrelid = 'resourcecapital.capital_allocations'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_allocations
            ADD CONSTRAINT fk_capital_allocations_owner_cycle
            FOREIGN KEY (capital_cycle_id, user_id)
            REFERENCES resourcecapital.capital_cycles (id, owner_id)
            ON DELETE RESTRICT;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_capital_allocations_effective_amounts'
          AND conrelid = 'resourcecapital.capital_allocations'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_allocations
            ADD CONSTRAINT chk_capital_allocations_effective_amounts
            CHECK (
                allocated_amount >= 0
                AND spent_amount >= 0
                AND released_amount >= 0
                AND spent_amount <= allocated_amount
            );
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_capital_allocations_effective_owner_cycle_kind_target
    ON resourcecapital.capital_allocations (
        user_id,
        capital_cycle_id,
        capital_type,
        target_type,
        target_id,
        status
    )
    WHERE status = 'ACTIVE'
      AND allocated_amount > 0;

CREATE INDEX IF NOT EXISTS idx_capital_histories_allocation_lifecycle
    ON resourcecapital.capital_histories (reference_id, action_type, created_at DESC)
    WHERE reference_type = 'ALLOCATION'
      AND action_type IN ('ALLOCATE', 'REALLOCATE', 'RELEASE');

COMMENT ON TABLE resourcecapital.capital_allocations IS
    'Stores the current CapitalAllocation state for one owner/cycle/resource type/target. Allocate, reallocate, and release evidence is kept append-style in capital_histories, capital_reallocations, and capital_releases.';

COMMENT ON CONSTRAINT fk_capital_allocations_owner_cycle
    ON resourcecapital.capital_allocations IS
    'Guarantees allocation.user_id is the owner of the referenced capital cycle.';

COMMENT ON CONSTRAINT chk_capital_allocations_effective_amounts
    ON resourcecapital.capital_allocations IS
    'Tracks effective allocation balance: released amount is cumulative history, while current spent cannot exceed current allocated amount.';
