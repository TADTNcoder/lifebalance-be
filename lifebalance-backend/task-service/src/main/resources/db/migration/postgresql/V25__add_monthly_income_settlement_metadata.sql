ALTER TABLE task.tasks
    ADD COLUMN monthly_income_group_id UUID,
    ADD COLUMN monthly_income_account_id UUID,
    ADD COLUMN monthly_income_currency VARCHAR(3),
    ADD COLUMN monthly_income_period VARCHAR(7),
    ADD COLUMN monthly_income_base NUMERIC(19, 4),
    ADD COLUMN monthly_income_bonus NUMERIC(19, 4),
    ADD COLUMN monthly_income_deduction NUMERIC(19, 4);

CREATE INDEX idx_tasks_monthly_income_group
    ON task.tasks (owner_id, monthly_income_group_id, status);

COMMENT ON COLUMN task.tasks.monthly_income_group_id IS
    'Groups all occurrences of one monthly income job and salary period.';
