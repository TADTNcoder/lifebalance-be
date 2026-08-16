CREATE TABLE resourcecapital.capital_adjustments (
    id BIGSERIAL PRIMARY KEY,
    capital_cycle_id UUID NOT NULL,
    user_id UUID NOT NULL,
    capital_type VARCHAR(32) NOT NULL,
    adjustment_type VARCHAR(32) NOT NULL,
    amount_delta NUMERIC(19, 4) NOT NULL,
    previous_amount NUMERIC(19, 4) NOT NULL,
    new_amount NUMERIC(19, 4) NOT NULL,
    reason VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_capital_adjustments_cycle
        FOREIGN KEY (capital_cycle_id)
        REFERENCES resourcecapital.capital_cycles (id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_capital_adjustments_capital_type
        CHECK (capital_type IN ('TIME', 'MONEY')),
    CONSTRAINT chk_capital_adjustments_adjustment_type
        CHECK (adjustment_type IN ('INCREASE', 'DECREASE', 'OVERRIDE')),
    CONSTRAINT chk_capital_adjustments_previous_amount
        CHECK (previous_amount >= 0),
    CONSTRAINT chk_capital_adjustments_new_amount
        CHECK (new_amount >= 0),
    CONSTRAINT chk_capital_adjustments_amount_delta
        CHECK (amount_delta = new_amount - previous_amount)
);

CREATE INDEX idx_capital_adjustments_cycle_created_at
    ON resourcecapital.capital_adjustments (capital_cycle_id, created_at DESC);

CREATE INDEX idx_capital_adjustments_cycle_type_created_at
    ON resourcecapital.capital_adjustments (capital_cycle_id, capital_type, created_at DESC);
