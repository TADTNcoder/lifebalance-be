CREATE TABLE resourcecapital.capital_allocations (
    id UUID PRIMARY KEY,
    capital_cycle_id UUID NOT NULL,
    capital_type VARCHAR(32) NOT NULL,
    target_type VARCHAR(64) NOT NULL,
    target_id UUID NOT NULL,
    allocated_amount DECIMAL(19, 4) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT fk_capital_allocations_cycle
        FOREIGN KEY (capital_cycle_id)
        REFERENCES resourcecapital.capital_cycles (id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_capital_allocations_capital_type
        CHECK (capital_type IN ('TIME', 'MONEY')),
    CONSTRAINT chk_capital_allocations_target_type
        CHECK (target_type IN ('TASK')),
    CONSTRAINT chk_capital_allocations_amount
        CHECK (allocated_amount > 0),
    CONSTRAINT uk_capital_allocations_target
        UNIQUE (capital_cycle_id, capital_type, target_type, target_id)
);

CREATE INDEX idx_capital_allocations_cycle_kind
    ON resourcecapital.capital_allocations (capital_cycle_id, capital_type);

CREATE INDEX idx_capital_allocations_target
    ON resourcecapital.capital_allocations (target_type, target_id);
