ALTER TABLE finance.finance_accounts
    ADD COLUMN IF NOT EXISTS active_main_pool_owner_key VARCHAR(36)
    GENERATED ALWAYS AS (
        CASE
            WHEN account_type = 'MAIN_POOL' AND status = 'ACTIVE'
            THEN CAST(owner_id AS VARCHAR)
            ELSE NULL
        END
    );

CREATE UNIQUE INDEX IF NOT EXISTS uq_fin_accounts_active_main_pool
    ON finance.finance_accounts(active_main_pool_owner_key);
