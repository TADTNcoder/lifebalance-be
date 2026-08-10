CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS resourcecapital.time_capitals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    capital_cycle_id UUID NOT NULL,
    allocated_hours NUMERIC(10, 2) NOT NULL DEFAULT 0,
    available_hours NUMERIC(10, 2) NOT NULL DEFAULT 0,
    spent_hours NUMERIC(10, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE resourcecapital.time_capitals
    ADD COLUMN IF NOT EXISTS allocated_hours NUMERIC(10, 2) NOT NULL DEFAULT 0;

ALTER TABLE resourcecapital.time_capitals
    ADD COLUMN IF NOT EXISTS available_hours NUMERIC(10, 2) NOT NULL DEFAULT 0;

ALTER TABLE resourcecapital.time_capitals
    ADD COLUMN IF NOT EXISTS spent_hours NUMERIC(10, 2) NOT NULL DEFAULT 0;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_time_capitals_cycle'
          AND conrelid = 'resourcecapital.time_capitals'::regclass
    ) THEN
        ALTER TABLE resourcecapital.time_capitals
            ADD CONSTRAINT fk_time_capitals_cycle
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
        WHERE conname = 'uk_time_capitals_cycle'
          AND conrelid = 'resourcecapital.time_capitals'::regclass
    ) THEN
        ALTER TABLE resourcecapital.time_capitals
            ADD CONSTRAINT uk_time_capitals_cycle
            UNIQUE (capital_cycle_id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_time_capitals_allocated_hours'
          AND conrelid = 'resourcecapital.time_capitals'::regclass
    ) THEN
        ALTER TABLE resourcecapital.time_capitals
            ADD CONSTRAINT chk_time_capitals_allocated_hours
            CHECK (allocated_hours >= 0);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_time_capitals_available_hours'
          AND conrelid = 'resourcecapital.time_capitals'::regclass
    ) THEN
        ALTER TABLE resourcecapital.time_capitals
            ADD CONSTRAINT chk_time_capitals_available_hours
            CHECK (available_hours >= 0);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_time_capitals_spent_hours'
          AND conrelid = 'resourcecapital.time_capitals'::regclass
    ) THEN
        ALTER TABLE resourcecapital.time_capitals
            ADD CONSTRAINT chk_time_capitals_spent_hours
            CHECK (spent_hours >= 0);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_time_capitals_capital_cycle_id
    ON resourcecapital.time_capitals (capital_cycle_id);
