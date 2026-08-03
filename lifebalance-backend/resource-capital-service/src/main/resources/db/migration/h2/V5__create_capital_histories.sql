CREATE TABLE resourcecapital.capital_histories (
    id UUID PRIMARY KEY,
    capital_cycle_id UUID NOT NULL,
    capital_type VARCHAR(32),
    action_type VARCHAR(64) NOT NULL,
    amount DECIMAL(19, 4),
    before_amount DECIMAL(19, 4),
    after_amount DECIMAL(19, 4),
    reason VARCHAR(1000),
    description VARCHAR(2000),
    reference_type VARCHAR(64),
    reference_id UUID,
    actor_type VARCHAR(32) NOT NULL,
    actor_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_capital_histories_cycle
        FOREIGN KEY (capital_cycle_id)
        REFERENCES resourcecapital.capital_cycles (id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_capital_histories_capital_type
        CHECK (capital_type IS NULL OR capital_type IN ('TIME', 'MONEY')),
    CONSTRAINT chk_capital_histories_action_type
        CHECK (action_type IN (
            'CYCLE_CREATED',
            'CYCLE_UPDATED',
            'CYCLE_ACTIVATED',
            'CYCLE_CLOSED',
            'CYCLE_REOPENED',
            'CAPITAL_SET',
            'ADJUSTMENT_INCREASE',
            'ADJUSTMENT_DECREASE',
            'ALLOCATE',
            'REALLOCATE',
            'RELEASE',
            'OVER_ALLOCATION_APPROVED',
            'TRANSFER_REMAINING'
        )),
    CONSTRAINT chk_capital_histories_reference_type
        CHECK (reference_type IS NULL OR reference_type IN ('TASK', 'ALLOCATION', 'TARGET_CAPITAL_CYCLE')),
    CONSTRAINT chk_capital_histories_reference_pair
        CHECK (
            (reference_type IS NULL AND reference_id IS NULL)
            OR (reference_type IS NOT NULL AND reference_id IS NOT NULL)
        ),
    CONSTRAINT chk_capital_histories_actor_type
        CHECK (actor_type IN ('USER', 'SYSTEM')),
    CONSTRAINT chk_capital_histories_user_actor_id
        CHECK (actor_type <> 'USER' OR actor_id IS NOT NULL),
    CONSTRAINT chk_capital_histories_amount
        CHECK (amount IS NULL OR amount >= 0)
);

CREATE INDEX idx_capital_histories_cycle_created_at
    ON resourcecapital.capital_histories (capital_cycle_id, created_at DESC);

CREATE INDEX idx_capital_histories_cycle_kind_created_at
    ON resourcecapital.capital_histories (capital_cycle_id, capital_type, created_at DESC);

CREATE INDEX idx_capital_histories_cycle_action_created_at
    ON resourcecapital.capital_histories (capital_cycle_id, action_type, created_at DESC);

CREATE INDEX idx_capital_histories_reference
    ON resourcecapital.capital_histories (reference_type, reference_id, created_at DESC);
