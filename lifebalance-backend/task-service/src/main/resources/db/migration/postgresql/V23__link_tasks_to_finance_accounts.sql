ALTER TABLE task.tasks
    ADD COLUMN IF NOT EXISTS finance_account_id UUID;

CREATE INDEX IF NOT EXISTS idx_tasks_owner_finance_account
    ON task.tasks (owner_id, finance_account_id)
    WHERE deleted_at IS NULL AND finance_account_id IS NOT NULL;

COMMENT ON COLUMN task.tasks.finance_account_id IS
    'Cross-service reference to finance.finance_accounts.id; no foreign key because Finance owns its database.';
