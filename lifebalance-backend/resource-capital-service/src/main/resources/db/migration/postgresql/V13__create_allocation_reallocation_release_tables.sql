ALTER TABLE resourcecapital.capital_allocations
    ADD COLUMN IF NOT EXISTS user_id UUID,
    ADD COLUMN IF NOT EXISTS spent_amount DECIMAL(19, 4) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS released_amount DECIMAL(19, 4) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN IF NOT EXISTS is_over_allocated BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS over_allocation_confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS note VARCHAR(1000);

UPDATE resourcecapital.capital_allocations allocation
SET user_id = cycle.owner_id
FROM resourcecapital.capital_cycles cycle
WHERE allocation.capital_cycle_id = cycle.id
  AND allocation.user_id IS NULL;

ALTER TABLE resourcecapital.capital_allocations
    ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE resourcecapital.capital_allocations
    DROP CONSTRAINT chk_capital_allocations_amount;

ALTER TABLE resourcecapital.capital_allocations
    ADD CONSTRAINT chk_capital_allocations_amount
        CHECK (allocated_amount >= 0),
    ADD CONSTRAINT chk_capital_allocations_spent_amount
        CHECK (spent_amount >= 0),
    ADD CONSTRAINT chk_capital_allocations_released_amount
        CHECK (released_amount >= 0),
    ADD CONSTRAINT chk_capital_allocations_status
        CHECK (status IN ('ACTIVE', 'REALLOCATED', 'RELEASED', 'CLOSED')),
    ADD CONSTRAINT chk_capital_allocations_over_allocation_flags
        CHECK (over_allocation_confirmed = FALSE OR is_over_allocated = TRUE);

CREATE INDEX idx_capital_allocations_cycle_status
    ON resourcecapital.capital_allocations (capital_cycle_id, status);

CREATE INDEX idx_capital_allocations_status_kind
    ON resourcecapital.capital_allocations (status, capital_type);

CREATE TABLE resourcecapital.capital_reallocations (
    id BIGSERIAL PRIMARY KEY,
    from_allocation_id UUID NOT NULL,
    to_allocation_id UUID NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    reason VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_capital_reallocations_from_allocation
        FOREIGN KEY (from_allocation_id)
        REFERENCES resourcecapital.capital_allocations (id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_capital_reallocations_to_allocation
        FOREIGN KEY (to_allocation_id)
        REFERENCES resourcecapital.capital_allocations (id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_capital_reallocations_amount
        CHECK (amount > 0),
    CONSTRAINT chk_capital_reallocations_distinct_allocations
        CHECK (from_allocation_id <> to_allocation_id)
);

CREATE INDEX idx_capital_reallocations_from
    ON resourcecapital.capital_reallocations (from_allocation_id, created_at DESC);

CREATE INDEX idx_capital_reallocations_to
    ON resourcecapital.capital_reallocations (to_allocation_id, created_at DESC);

CREATE TABLE resourcecapital.capital_releases (
    id BIGSERIAL PRIMARY KEY,
    allocation_id UUID NOT NULL,
    released_amount DECIMAL(19, 4) NOT NULL,
    reason VARCHAR(1000),
    released_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_capital_releases_allocation
        FOREIGN KEY (allocation_id)
        REFERENCES resourcecapital.capital_allocations (id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_capital_releases_released_amount
        CHECK (released_amount > 0)
);

CREATE INDEX idx_capital_releases_allocation_released_at
    ON resourcecapital.capital_releases (allocation_id, released_at DESC);
