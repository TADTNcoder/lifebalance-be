UPDATE resourcecapital.capital_allocations allocation
SET user_id = (
    SELECT cycle.owner_id
    FROM resourcecapital.capital_cycles cycle
    WHERE cycle.id = allocation.capital_cycle_id
)
WHERE EXISTS (
    SELECT 1
    FROM resourcecapital.capital_cycles cycle
    WHERE cycle.id = allocation.capital_cycle_id
      AND allocation.user_id <> cycle.owner_id
);

ALTER TABLE resourcecapital.capital_cycles
    ADD CONSTRAINT uk_capital_cycles_id_owner
    UNIQUE (id, owner_id);

ALTER TABLE resourcecapital.capital_allocations
    ADD CONSTRAINT fk_capital_allocations_owner_cycle
    FOREIGN KEY (capital_cycle_id, user_id)
    REFERENCES resourcecapital.capital_cycles (id, owner_id)
    ON DELETE RESTRICT;

ALTER TABLE resourcecapital.capital_allocations
    ADD CONSTRAINT chk_capital_allocations_effective_amounts
    CHECK (
        allocated_amount >= 0
        AND spent_amount >= 0
        AND released_amount >= 0
        AND spent_amount <= allocated_amount
    );

CREATE INDEX IF NOT EXISTS idx_capital_allocations_effective_owner_cycle_kind_target
    ON resourcecapital.capital_allocations (
        user_id,
        capital_cycle_id,
        capital_type,
        target_type,
        target_id,
        status,
        allocated_amount
    );

CREATE INDEX IF NOT EXISTS idx_capital_histories_allocation_lifecycle
    ON resourcecapital.capital_histories (
        reference_type,
        reference_id,
        action_type,
        created_at DESC
    );

COMMENT ON TABLE resourcecapital.capital_allocations IS
    'Stores the current CapitalAllocation state for one owner/cycle/resource type/target; lifecycle evidence remains in capital_histories, capital_reallocations, and capital_releases.';
