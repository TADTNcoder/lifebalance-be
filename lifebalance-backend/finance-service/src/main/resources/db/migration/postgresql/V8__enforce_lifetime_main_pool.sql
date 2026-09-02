CREATE UNIQUE INDEX IF NOT EXISTS uq_fin_accounts_active_main_pool
    ON finance.finance_accounts(owner_id)
    WHERE account_type = 'MAIN_POOL' AND status = 'ACTIVE';
