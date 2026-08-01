CREATE TABLE resourcecapital.time_capitals (
    id UUID PRIMARY KEY,
    capital_cycle_id UUID NOT NULL,
    planned_minutes BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT uk_time_capitals_cycle UNIQUE (capital_cycle_id),
    CONSTRAINT fk_time_capitals_cycle
        FOREIGN KEY (capital_cycle_id)
        REFERENCES resourcecapital.capital_cycles (id),
    CONSTRAINT chk_time_capitals_planned_minutes CHECK (planned_minutes >= 0)
);
