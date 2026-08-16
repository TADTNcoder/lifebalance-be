CREATE SCHEMA IF NOT EXISTS resourcecapital;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS resourcecapital.capital_adjustments (
    id BIGSERIAL PRIMARY KEY,
    capital_cycle_id UUID NOT NULL,
    user_id UUID NOT NULL,
    capital_type VARCHAR(32) NOT NULL,
    adjustment_type VARCHAR(32) NOT NULL,
    amount_delta NUMERIC(19, 4) NOT NULL,
    previous_amount NUMERIC(19, 4) NOT NULL,
    new_amount NUMERIC(19, 4) NOT NULL,
    reason VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE resourcecapital.capital_adjustments
    ADD COLUMN IF NOT EXISTS capital_cycle_id UUID,
    ADD COLUMN IF NOT EXISTS user_id UUID,
    ADD COLUMN IF NOT EXISTS capital_type VARCHAR(32),
    ADD COLUMN IF NOT EXISTS adjustment_type VARCHAR(32),
    ADD COLUMN IF NOT EXISTS amount_delta NUMERIC(19, 4),
    ADD COLUMN IF NOT EXISTS previous_amount NUMERIC(19, 4),
    ADD COLUMN IF NOT EXISTS new_amount NUMERIC(19, 4),
    ADD COLUMN IF NOT EXISTS reason VARCHAR(1000),
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE resourcecapital.capital_adjustments
    ALTER COLUMN capital_cycle_id SET NOT NULL,
    ALTER COLUMN capital_type SET NOT NULL,
    ALTER COLUMN adjustment_type SET NOT NULL,
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_capital_adjustments_cycle'
          AND conrelid = 'resourcecapital.capital_adjustments'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_adjustments
            ADD CONSTRAINT fk_capital_adjustments_cycle
            FOREIGN KEY (capital_cycle_id)
            REFERENCES resourcecapital.capital_cycles (id)
            ON DELETE RESTRICT;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_capital_adjustments_capital_type'
          AND conrelid = 'resourcecapital.capital_adjustments'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_adjustments
            ADD CONSTRAINT chk_capital_adjustments_capital_type
            CHECK (capital_type IN ('TIME', 'MONEY'));
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_capital_adjustments_adjustment_type'
          AND conrelid = 'resourcecapital.capital_adjustments'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_adjustments
            ADD CONSTRAINT chk_capital_adjustments_adjustment_type
            CHECK (adjustment_type IN ('INCREASE', 'DECREASE', 'OVERRIDE'));
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_capital_adjustments_previous_amount'
          AND conrelid = 'resourcecapital.capital_adjustments'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_adjustments
            ADD CONSTRAINT chk_capital_adjustments_previous_amount
            CHECK (previous_amount IS NULL OR previous_amount >= 0);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_capital_adjustments_new_amount'
          AND conrelid = 'resourcecapital.capital_adjustments'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_adjustments
            ADD CONSTRAINT chk_capital_adjustments_new_amount
            CHECK (new_amount IS NULL OR new_amount >= 0);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_capital_adjustments_amount_delta'
          AND conrelid = 'resourcecapital.capital_adjustments'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_adjustments
            ADD CONSTRAINT chk_capital_adjustments_amount_delta
            CHECK (
                amount_delta IS NULL
                OR previous_amount IS NULL
                OR new_amount IS NULL
                OR amount_delta = new_amount - previous_amount
            );
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_capital_adjustments_cycle_created_at
    ON resourcecapital.capital_adjustments (capital_cycle_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_capital_adjustments_cycle_type_created_at
    ON resourcecapital.capital_adjustments (capital_cycle_id, capital_type, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_capital_adjustments_created_at
    ON resourcecapital.capital_adjustments (created_at DESC);

CREATE TABLE IF NOT EXISTS resourcecapital.capital_allocations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    capital_cycle_id UUID NOT NULL,
    capital_type VARCHAR(32) NOT NULL,
    target_type VARCHAR(64) NOT NULL,
    target_id UUID NOT NULL,
    allocated_amount NUMERIC(19, 4) NOT NULL,
    spent_amount NUMERIC(19, 4) NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

ALTER TABLE resourcecapital.capital_allocations
    ADD COLUMN IF NOT EXISTS capital_cycle_id UUID,
    ADD COLUMN IF NOT EXISTS capital_type VARCHAR(32),
    ADD COLUMN IF NOT EXISTS target_type VARCHAR(64),
    ADD COLUMN IF NOT EXISTS target_id UUID,
    ADD COLUMN IF NOT EXISTS allocated_amount NUMERIC(19, 4),
    ADD COLUMN IF NOT EXISTS spent_amount NUMERIC(19, 4) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS status VARCHAR(32) DEFAULT 'ACTIVE',
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

UPDATE resourcecapital.capital_allocations
SET spent_amount = 0
WHERE spent_amount IS NULL;

UPDATE resourcecapital.capital_allocations
SET status = 'ACTIVE'
WHERE status IS NULL;

UPDATE resourcecapital.capital_allocations
SET created_at = CURRENT_TIMESTAMP
WHERE created_at IS NULL;

UPDATE resourcecapital.capital_allocations
SET updated_at = created_at
WHERE updated_at IS NULL;

UPDATE resourcecapital.capital_allocations
SET version = 0
WHERE version IS NULL;

ALTER TABLE resourcecapital.capital_allocations
    ALTER COLUMN id SET DEFAULT gen_random_uuid(),
    ALTER COLUMN capital_cycle_id SET NOT NULL,
    ALTER COLUMN capital_type SET NOT NULL,
    ALTER COLUMN target_type SET NOT NULL,
    ALTER COLUMN target_id SET NOT NULL,
    ALTER COLUMN allocated_amount SET NOT NULL,
    ALTER COLUMN spent_amount SET NOT NULL,
    ALTER COLUMN spent_amount SET DEFAULT 0,
    ALTER COLUMN status SET NOT NULL,
    ALTER COLUMN status SET DEFAULT 'ACTIVE',
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN updated_at SET NOT NULL,
    ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN version SET NOT NULL,
    ALTER COLUMN version SET DEFAULT 0;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_capital_allocations_cycle'
          AND conrelid = 'resourcecapital.capital_allocations'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_allocations
            ADD CONSTRAINT fk_capital_allocations_cycle
            FOREIGN KEY (capital_cycle_id)
            REFERENCES resourcecapital.capital_cycles (id)
            ON DELETE RESTRICT;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_capital_allocations_target'
          AND conrelid = 'resourcecapital.capital_allocations'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_allocations
            ADD CONSTRAINT uk_capital_allocations_target
            UNIQUE (capital_cycle_id, capital_type, target_type, target_id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_capital_allocations_capital_type'
          AND conrelid = 'resourcecapital.capital_allocations'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_allocations
            ADD CONSTRAINT chk_capital_allocations_capital_type
            CHECK (capital_type IN ('TIME', 'MONEY'));
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_capital_allocations_target_type'
          AND conrelid = 'resourcecapital.capital_allocations'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_allocations
            ADD CONSTRAINT chk_capital_allocations_target_type
            CHECK (target_type IN ('TASK'));
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_capital_allocations_amount'
          AND conrelid = 'resourcecapital.capital_allocations'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_allocations
            ADD CONSTRAINT chk_capital_allocations_amount
            CHECK (allocated_amount >= 0);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_capital_allocations_spent_amount'
          AND conrelid = 'resourcecapital.capital_allocations'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_allocations
            ADD CONSTRAINT chk_capital_allocations_spent_amount
            CHECK (spent_amount >= 0);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_capital_allocations_status'
          AND conrelid = 'resourcecapital.capital_allocations'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_allocations
            ADD CONSTRAINT chk_capital_allocations_status
            CHECK (status IN ('ACTIVE', 'CLOSED', 'RELEASED'));
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_capital_allocations_time_whole_minutes'
          AND conrelid = 'resourcecapital.capital_allocations'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_allocations
            ADD CONSTRAINT chk_capital_allocations_time_whole_minutes
            CHECK (
                capital_type <> 'TIME'
                OR allocated_amount = floor(allocated_amount)
            );
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_capital_allocations_cycle_kind
    ON resourcecapital.capital_allocations (capital_cycle_id, capital_type);

CREATE INDEX IF NOT EXISTS idx_capital_allocations_target
    ON resourcecapital.capital_allocations (target_type, target_id);

CREATE INDEX IF NOT EXISTS idx_capital_allocations_task_id
    ON resourcecapital.capital_allocations (target_id)
    WHERE target_type = 'TASK';

CREATE INDEX IF NOT EXISTS idx_capital_allocations_cycle_status
    ON resourcecapital.capital_allocations (capital_cycle_id, status);

CREATE INDEX IF NOT EXISTS idx_capital_allocations_status_kind
    ON resourcecapital.capital_allocations (status, capital_type);

CREATE INDEX IF NOT EXISTS idx_capital_allocations_created_at
    ON resourcecapital.capital_allocations (created_at DESC);

CREATE TABLE IF NOT EXISTS resourcecapital.capital_reallocations (
    id BIGSERIAL PRIMARY KEY,
    from_allocation_id UUID NOT NULL,
    to_allocation_id UUID NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    reason VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE resourcecapital.capital_reallocations
    ADD COLUMN IF NOT EXISTS from_allocation_id UUID,
    ADD COLUMN IF NOT EXISTS to_allocation_id UUID,
    ADD COLUMN IF NOT EXISTS amount NUMERIC(19, 4),
    ADD COLUMN IF NOT EXISTS reason VARCHAR(1000),
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE resourcecapital.capital_reallocations
    ALTER COLUMN from_allocation_id SET NOT NULL,
    ALTER COLUMN to_allocation_id SET NOT NULL,
    ALTER COLUMN amount SET NOT NULL,
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_capital_reallocations_from_allocation'
          AND conrelid = 'resourcecapital.capital_reallocations'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_reallocations
            ADD CONSTRAINT fk_capital_reallocations_from_allocation
            FOREIGN KEY (from_allocation_id)
            REFERENCES resourcecapital.capital_allocations (id)
            ON DELETE RESTRICT;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_capital_reallocations_to_allocation'
          AND conrelid = 'resourcecapital.capital_reallocations'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_reallocations
            ADD CONSTRAINT fk_capital_reallocations_to_allocation
            FOREIGN KEY (to_allocation_id)
            REFERENCES resourcecapital.capital_allocations (id)
            ON DELETE RESTRICT;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_capital_reallocations_amount'
          AND conrelid = 'resourcecapital.capital_reallocations'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_reallocations
            ADD CONSTRAINT chk_capital_reallocations_amount
            CHECK (amount > 0);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_capital_reallocations_distinct_allocations'
          AND conrelid = 'resourcecapital.capital_reallocations'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_reallocations
            ADD CONSTRAINT chk_capital_reallocations_distinct_allocations
            CHECK (from_allocation_id <> to_allocation_id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_capital_reallocations_from
    ON resourcecapital.capital_reallocations (from_allocation_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_capital_reallocations_to
    ON resourcecapital.capital_reallocations (to_allocation_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_capital_reallocations_created_at
    ON resourcecapital.capital_reallocations (created_at DESC);

CREATE TABLE IF NOT EXISTS resourcecapital.capital_releases (
    id BIGSERIAL PRIMARY KEY,
    allocation_id UUID NOT NULL,
    released_amount NUMERIC(19, 4) NOT NULL,
    reason VARCHAR(1000),
    released_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE resourcecapital.capital_releases
    ADD COLUMN IF NOT EXISTS allocation_id UUID,
    ADD COLUMN IF NOT EXISTS released_amount NUMERIC(19, 4),
    ADD COLUMN IF NOT EXISTS reason VARCHAR(1000),
    ADD COLUMN IF NOT EXISTS released_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE resourcecapital.capital_releases
    ALTER COLUMN allocation_id SET NOT NULL,
    ALTER COLUMN released_amount SET NOT NULL,
    ALTER COLUMN released_at SET NOT NULL,
    ALTER COLUMN released_at SET DEFAULT CURRENT_TIMESTAMP;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_capital_releases_allocation'
          AND conrelid = 'resourcecapital.capital_releases'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_releases
            ADD CONSTRAINT fk_capital_releases_allocation
            FOREIGN KEY (allocation_id)
            REFERENCES resourcecapital.capital_allocations (id)
            ON DELETE RESTRICT;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_capital_releases_released_amount'
          AND conrelid = 'resourcecapital.capital_releases'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_releases
            ADD CONSTRAINT chk_capital_releases_released_amount
            CHECK (released_amount > 0);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_capital_releases_allocation_released_at
    ON resourcecapital.capital_releases (allocation_id, released_at DESC);

CREATE INDEX IF NOT EXISTS idx_capital_releases_released_at
    ON resourcecapital.capital_releases (released_at DESC);

CREATE TABLE IF NOT EXISTS resourcecapital.capital_histories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    capital_cycle_id UUID NOT NULL,
    capital_type VARCHAR(32),
    action_type VARCHAR(64) NOT NULL,
    amount NUMERIC(19, 4),
    before_amount NUMERIC(19, 4),
    after_amount NUMERIC(19, 4),
    reason VARCHAR(1000),
    description VARCHAR(2000),
    reference_type VARCHAR(64),
    reference_id UUID,
    actor_type VARCHAR(32) NOT NULL,
    actor_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE resourcecapital.capital_histories
    ADD COLUMN IF NOT EXISTS capital_cycle_id UUID,
    ADD COLUMN IF NOT EXISTS capital_type VARCHAR(32),
    ADD COLUMN IF NOT EXISTS action_type VARCHAR(64),
    ADD COLUMN IF NOT EXISTS amount NUMERIC(19, 4),
    ADD COLUMN IF NOT EXISTS before_amount NUMERIC(19, 4),
    ADD COLUMN IF NOT EXISTS after_amount NUMERIC(19, 4),
    ADD COLUMN IF NOT EXISTS reason VARCHAR(1000),
    ADD COLUMN IF NOT EXISTS description VARCHAR(2000),
    ADD COLUMN IF NOT EXISTS reference_type VARCHAR(64),
    ADD COLUMN IF NOT EXISTS reference_id UUID,
    ADD COLUMN IF NOT EXISTS actor_type VARCHAR(32),
    ADD COLUMN IF NOT EXISTS actor_id UUID,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP;

UPDATE resourcecapital.capital_histories
SET created_at = CURRENT_TIMESTAMP
WHERE created_at IS NULL;

ALTER TABLE resourcecapital.capital_histories
    ALTER COLUMN id SET DEFAULT gen_random_uuid(),
    ALTER COLUMN capital_cycle_id SET NOT NULL,
    ALTER COLUMN action_type SET NOT NULL,
    ALTER COLUMN actor_type SET NOT NULL,
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_capital_histories_cycle'
          AND conrelid = 'resourcecapital.capital_histories'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_histories
            ADD CONSTRAINT fk_capital_histories_cycle
            FOREIGN KEY (capital_cycle_id)
            REFERENCES resourcecapital.capital_cycles (id)
            ON DELETE RESTRICT;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_capital_histories_capital_type'
          AND conrelid = 'resourcecapital.capital_histories'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_histories
            ADD CONSTRAINT chk_capital_histories_capital_type
            CHECK (capital_type IS NULL OR capital_type IN ('TIME', 'MONEY'));
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_capital_histories_action_type'
          AND conrelid = 'resourcecapital.capital_histories'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_histories
            ADD CONSTRAINT chk_capital_histories_action_type
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
            ));
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_capital_histories_reference_type'
          AND conrelid = 'resourcecapital.capital_histories'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_histories
            ADD CONSTRAINT chk_capital_histories_reference_type
            CHECK (reference_type IS NULL OR reference_type IN ('MANUAL', 'TASK', 'ALLOCATION', 'TARGET_CAPITAL_CYCLE'));
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_capital_histories_reference_pair'
          AND conrelid = 'resourcecapital.capital_histories'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_histories
            ADD CONSTRAINT chk_capital_histories_reference_pair
            CHECK (
                (reference_type IS NULL AND reference_id IS NULL)
                OR (reference_type = 'MANUAL' AND reference_id IS NULL)
                OR (reference_type IS NOT NULL AND reference_type <> 'MANUAL' AND reference_id IS NOT NULL)
            );
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_capital_histories_actor_type'
          AND conrelid = 'resourcecapital.capital_histories'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_histories
            ADD CONSTRAINT chk_capital_histories_actor_type
            CHECK (actor_type IN ('USER', 'SYSTEM'));
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_capital_histories_user_actor_id'
          AND conrelid = 'resourcecapital.capital_histories'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_histories
            ADD CONSTRAINT chk_capital_histories_user_actor_id
            CHECK (actor_type <> 'USER' OR actor_id IS NOT NULL);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_capital_histories_amount'
          AND conrelid = 'resourcecapital.capital_histories'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_histories
            ADD CONSTRAINT chk_capital_histories_amount
            CHECK (amount IS NULL OR amount >= 0);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_capital_histories_cycle_created_at
    ON resourcecapital.capital_histories (capital_cycle_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_capital_histories_cycle_kind_created_at
    ON resourcecapital.capital_histories (capital_cycle_id, capital_type, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_capital_histories_cycle_action_created_at
    ON resourcecapital.capital_histories (capital_cycle_id, action_type, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_capital_histories_reference
    ON resourcecapital.capital_histories (reference_type, reference_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_capital_histories_created_at
    ON resourcecapital.capital_histories (created_at DESC);
