CREATE INDEX IF NOT EXISTS idx_capital_allocations_user_cycle_created_at
    ON resourcecapital.capital_allocations (user_id, capital_cycle_id, created_at DESC, id DESC);

CREATE INDEX IF NOT EXISTS idx_capital_allocations_user_cycle_kind_created_at
    ON resourcecapital.capital_allocations (user_id, capital_cycle_id, capital_type, created_at DESC, id DESC);

CREATE INDEX IF NOT EXISTS idx_capital_allocations_user_cycle_status_created_at
    ON resourcecapital.capital_allocations (user_id, capital_cycle_id, status, created_at DESC, id DESC);

CREATE INDEX IF NOT EXISTS idx_capital_allocations_user_target_status
    ON resourcecapital.capital_allocations (user_id, target_type, target_id, status);

CREATE INDEX IF NOT EXISTS idx_capital_allocations_user_cycle_target_kind
    ON resourcecapital.capital_allocations (user_id, capital_cycle_id, target_type, target_id, capital_type);
