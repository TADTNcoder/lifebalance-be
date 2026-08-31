ALTER TABLE finance.finance_accounts
    DROP CONSTRAINT IF EXISTS chk_finance_accounts_type;

UPDATE finance.finance_accounts
SET account_type = 'JAR';

UPDATE finance.finance_accounts account
SET account_type = 'MAIN_POOL'
WHERE account.id = (
    SELECT candidate.id
    FROM finance.finance_accounts candidate
    WHERE candidate.owner_id = account.owner_id
    ORDER BY
        CASE WHEN candidate.status = 'ACTIVE' THEN 0 ELSE 1 END,
        candidate.created_at,
        candidate.id
    FETCH FIRST 1 ROW ONLY
);

ALTER TABLE finance.finance_accounts
    ADD CONSTRAINT chk_finance_accounts_type
        CHECK (account_type IN ('MAIN_POOL', 'JAR'));

CREATE UNIQUE INDEX IF NOT EXISTS uq_fin_accounts_active_main_pool
    ON finance.finance_accounts(owner_id)
    WHERE account_type = 'MAIN_POOL' AND status = 'ACTIVE';

CREATE INDEX IF NOT EXISTS idx_fin_accounts_owner_type_status
    ON finance.finance_accounts(owner_id, account_type, status);
