UPDATE resourcecapital.capital_adjustments adjustment
SET user_id = (
    SELECT cycle.owner_id
    FROM resourcecapital.capital_cycles cycle
    WHERE cycle.id = adjustment.capital_cycle_id
)
WHERE EXISTS (
    SELECT 1
    FROM resourcecapital.capital_cycles cycle
    WHERE cycle.id = adjustment.capital_cycle_id
      AND adjustment.user_id <> cycle.owner_id
);

ALTER TABLE resourcecapital.capital_adjustments
    ADD CONSTRAINT fk_capital_adjustments_owner_cycle
    FOREIGN KEY (capital_cycle_id, user_id)
    REFERENCES resourcecapital.capital_cycles (id, owner_id)
    ON DELETE RESTRICT;

CREATE INDEX IF NOT EXISTS idx_cap_adj_user_cycle
    ON resourcecapital.capital_adjustments (user_id, capital_cycle_id);

CREATE INDEX IF NOT EXISTS idx_cap_adj_type_created
    ON resourcecapital.capital_adjustments (capital_type, created_at DESC, id DESC);

CREATE INDEX IF NOT EXISTS idx_cap_adj_action_created
    ON resourcecapital.capital_adjustments (adjustment_type, created_at DESC, id DESC);

CREATE INDEX IF NOT EXISTS idx_cap_alloc_target
    ON resourcecapital.capital_allocations (target_type, target_id, user_id);

CREATE INDEX IF NOT EXISTS idx_cap_alloc_status
    ON resourcecapital.capital_allocations (status, capital_type);

CREATE INDEX IF NOT EXISTS idx_cap_alloc_user_cycle_status_updated
    ON resourcecapital.capital_allocations (user_id, capital_cycle_id, status, updated_at DESC, id DESC);

CREATE INDEX IF NOT EXISTS idx_cap_alloc_over_allocated
    ON resourcecapital.capital_allocations (is_over_allocated, user_id, capital_cycle_id, capital_type, updated_at DESC, id DESC);

CREATE INDEX IF NOT EXISTS idx_cap_hist_cycle_action_time
    ON resourcecapital.capital_histories (capital_cycle_id, action_type, created_at DESC, id DESC);

CREATE INDEX IF NOT EXISTS idx_cap_hist_type_time
    ON resourcecapital.capital_histories (capital_type, created_at DESC, id DESC);

CREATE INDEX IF NOT EXISTS idx_cap_hist_actor_time
    ON resourcecapital.capital_histories (actor_type, actor_id, created_at DESC, id DESC);

CREATE INDEX IF NOT EXISTS idx_cap_hist_reference_time
    ON resourcecapital.capital_histories (reference_type, reference_id, created_at DESC, id DESC);

CREATE INDEX IF NOT EXISTS idx_cap_realloc_time
    ON resourcecapital.capital_reallocations (created_at DESC, id DESC);

CREATE INDEX IF NOT EXISTS idx_cap_release_time
    ON resourcecapital.capital_releases (released_at DESC, id DESC);

COMMENT ON TABLE resourcecapital.capital_allocations IS
    'Stores current allocation state and supports valid over-allocation policy states; planned-capital limits are enforced by service policy, not by a hard database cap.';
