CREATE INDEX IF NOT EXISTS idx_capital_adjustments_user_cycle_created_at
    ON resourcecapital.capital_adjustments (user_id, capital_cycle_id, created_at DESC, id DESC);

CREATE INDEX IF NOT EXISTS idx_capital_adjustments_user_cycle_type_created_at
    ON resourcecapital.capital_adjustments (user_id, capital_cycle_id, capital_type, created_at DESC, id DESC);

CREATE INDEX IF NOT EXISTS idx_capital_adjustments_user_adjustment_created_at
    ON resourcecapital.capital_adjustments (user_id, adjustment_type, created_at DESC, id DESC);

CREATE INDEX IF NOT EXISTS idx_capital_adjustments_user_created_at
    ON resourcecapital.capital_adjustments (user_id, created_at DESC, id DESC);
