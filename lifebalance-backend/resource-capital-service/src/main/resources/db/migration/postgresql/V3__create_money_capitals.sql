CREATE TABLE resourcecapital.money_capitals (
    id UUID PRIMARY KEY,
    capital_cycle_id UUID NOT NULL,
    planned_amount DECIMAL(19, 4) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT uk_money_capitals_cycle UNIQUE (capital_cycle_id),
    CONSTRAINT fk_money_capitals_cycle
        FOREIGN KEY (capital_cycle_id)
        REFERENCES resourcecapital.capital_cycles (id),
    CONSTRAINT chk_money_capitals_planned_amount CHECK (planned_amount >= 0),
    CONSTRAINT chk_money_capitals_currency_code CHECK (CHAR_LENGTH(currency_code) = 3)
);
