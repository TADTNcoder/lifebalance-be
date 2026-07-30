CREATE SCHEMA IF NOT EXISTS resourcecapital;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE resourcecapital.capital_cycles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    cycle_type VARCHAR(32) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    over_allocation_allowed BOOLEAN NOT NULL DEFAULT false,
    activated_at TIMESTAMPTZ,
    closed_at TIMESTAMPTZ,
    reopened_at TIMESTAMPTZ,
    close_reason VARCHAR(1000),
    reopen_reason VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_resourcecapital_capital_cycles_type CHECK (cycle_type IN ('DAILY', 'WEEKLY', 'MONTHLY')),
    CONSTRAINT chk_resourcecapital_capital_cycles_status CHECK (status IN ('DRAFT', 'ACTIVE', 'CLOSED', 'REOPENED')),
    CONSTRAINT chk_resourcecapital_capital_cycles_date_range CHECK (start_date <= end_date)
);

CREATE INDEX idx_capital_cycle_owner_status
    ON resourcecapital.capital_cycles (owner_id, status);

CREATE INDEX idx_capital_cycle_owner_period
    ON resourcecapital.capital_cycles (owner_id, start_date, end_date);
