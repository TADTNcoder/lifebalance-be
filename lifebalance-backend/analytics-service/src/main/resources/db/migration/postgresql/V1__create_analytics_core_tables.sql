CREATE SCHEMA IF NOT EXISTS analytics;

CREATE TABLE IF NOT EXISTS analytics.actual_records (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    actor_id UUID,
    task_id UUID NOT NULL,
    capital_cycle_id UUID,
    category_id UUID,
    tag_ids TEXT,
    record_type VARCHAR(32) NOT NULL,
    status VARCHAR(16) NOT NULL,
    actual_minutes INTEGER,
    actual_cost NUMERIC(19, 4),
    currency_code VARCHAR(3),
    actual_date DATE NOT NULL,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    note VARCHAR(1000),
    source VARCHAR(64),
    archived_at TIMESTAMP WITH TIME ZONE,
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_actual_records_type CHECK (record_type IN ('TIME', 'MONEY', 'TIME_AND_MONEY')),
    CONSTRAINT chk_actual_records_status CHECK (status IN ('ACTIVE', 'ARCHIVED')),
    CONSTRAINT chk_actual_records_minutes CHECK (actual_minutes IS NULL OR actual_minutes >= 0),
    CONSTRAINT chk_actual_records_cost CHECK (actual_cost IS NULL OR actual_cost >= 0),
    CONSTRAINT chk_actual_records_currency CHECK (
        (actual_cost IS NULL AND currency_code IS NULL)
        OR (actual_cost IS NOT NULL AND currency_code IS NOT NULL)
    ),
    CONSTRAINT chk_actual_records_measurement CHECK (actual_minutes IS NOT NULL OR actual_cost IS NOT NULL)
);

CREATE TABLE IF NOT EXISTS analytics.evaluation_results (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    actor_id UUID,
    task_id UUID NOT NULL,
    capital_cycle_id UUID,
    period_start DATE,
    period_end DATE,
    planned_minutes INTEGER,
    actual_minutes INTEGER,
    minute_variance INTEGER,
    planned_cost NUMERIC(19, 4),
    actual_cost NUMERIC(19, 4),
    cost_variance NUMERIC(19, 4),
    currency_code VARCHAR(3),
    efficiency_percent NUMERIC(9, 4),
    status VARCHAR(32) NOT NULL,
    generated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reason VARCHAR(1000),
    archived_at TIMESTAMP WITH TIME ZONE,
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_evaluation_results_status CHECK (
        status IN ('ON_TRACK', 'UNDER_PLANNED', 'OVER_PLANNED', 'NO_PLAN', 'ARCHIVED')
    ),
    CONSTRAINT chk_evaluation_results_minutes CHECK (
        (planned_minutes IS NULL OR planned_minutes >= 0)
        AND (actual_minutes IS NULL OR actual_minutes >= 0)
    ),
    CONSTRAINT chk_evaluation_results_cost CHECK (
        (planned_cost IS NULL OR planned_cost >= 0)
        AND (actual_cost IS NULL OR actual_cost >= 0)
    ),
    CONSTRAINT chk_evaluation_results_period CHECK (
        period_start IS NULL OR period_end IS NULL OR period_start <= period_end
    ),
    CONSTRAINT chk_evaluation_results_currency CHECK (
        (planned_cost IS NULL AND actual_cost IS NULL AND currency_code IS NULL)
        OR ((planned_cost IS NOT NULL OR actual_cost IS NOT NULL) AND currency_code IS NOT NULL)
    )
);

CREATE TABLE IF NOT EXISTS analytics.analytics_reports (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    actor_id UUID,
    report_type VARCHAR(32) NOT NULL,
    status VARCHAR(16) NOT NULL,
    dimension VARCHAR(32) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    task_count INTEGER NOT NULL DEFAULT 0,
    actual_record_count INTEGER NOT NULL DEFAULT 0,
    total_actual_minutes INTEGER NOT NULL DEFAULT 0,
    total_actual_cost NUMERIC(19, 4),
    currency_code VARCHAR(3),
    average_efficiency_percent NUMERIC(9, 4),
    variance_summary TEXT,
    generated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reason VARCHAR(1000),
    archived_at TIMESTAMP WITH TIME ZONE,
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_analytics_reports_type CHECK (
        report_type IN ('SUMMARY', 'TASK', 'CATEGORY', 'TAG', 'PERIOD', 'EFFICIENCY', 'VARIANCE')
    ),
    CONSTRAINT chk_analytics_reports_status CHECK (status IN ('GENERATED', 'ARCHIVED')),
    CONSTRAINT chk_analytics_reports_dimension CHECK (dimension IN ('TASK', 'CATEGORY', 'TAG', 'PERIOD')),
    CONSTRAINT chk_analytics_reports_period CHECK (period_start <= period_end),
    CONSTRAINT chk_analytics_reports_counts CHECK (
        task_count >= 0
        AND actual_record_count >= 0
        AND total_actual_minutes >= 0
    ),
    CONSTRAINT chk_analytics_reports_cost CHECK (total_actual_cost IS NULL OR total_actual_cost >= 0),
    CONSTRAINT chk_analytics_reports_currency CHECK (
        (total_actual_cost IS NULL AND currency_code IS NULL)
        OR (total_actual_cost IS NOT NULL AND currency_code IS NOT NULL)
    )
);

CREATE TABLE IF NOT EXISTS analytics.analytics_histories (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    actor_id UUID,
    action_type VARCHAR(64) NOT NULL,
    actual_record_id UUID,
    evaluation_result_id UUID,
    report_id UUID,
    old_value TEXT,
    new_value TEXT,
    reason VARCHAR(1000),
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_analytics_histories_actual_record
        FOREIGN KEY (actual_record_id)
        REFERENCES analytics.actual_records(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_analytics_histories_evaluation_result
        FOREIGN KEY (evaluation_result_id)
        REFERENCES analytics.evaluation_results(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_analytics_histories_report
        FOREIGN KEY (report_id)
        REFERENCES analytics.analytics_reports(id)
        ON DELETE SET NULL,
    CONSTRAINT chk_analytics_histories_action CHECK (
        action_type IN (
            'ACTUAL_RECORDED',
            'ACTUAL_UPDATED',
            'ACTUAL_ARCHIVED',
            'EVALUATION_GENERATED',
            'EVALUATION_REGENERATED',
            'EVALUATION_ARCHIVED',
            'REPORT_GENERATED',
            'REPORT_ARCHIVED'
        )
    )
);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'identity'
          AND table_name = 'users'
    ) THEN
        ALTER TABLE analytics.actual_records
            ADD CONSTRAINT fk_actual_records_owner
            FOREIGN KEY (owner_id) REFERENCES identity.users(id) ON DELETE RESTRICT;
        ALTER TABLE analytics.actual_records
            ADD CONSTRAINT fk_actual_records_actor
            FOREIGN KEY (actor_id) REFERENCES identity.users(id) ON DELETE SET NULL;
        ALTER TABLE analytics.evaluation_results
            ADD CONSTRAINT fk_evaluation_results_owner
            FOREIGN KEY (owner_id) REFERENCES identity.users(id) ON DELETE RESTRICT;
        ALTER TABLE analytics.evaluation_results
            ADD CONSTRAINT fk_evaluation_results_actor
            FOREIGN KEY (actor_id) REFERENCES identity.users(id) ON DELETE SET NULL;
        ALTER TABLE analytics.analytics_reports
            ADD CONSTRAINT fk_analytics_reports_owner
            FOREIGN KEY (owner_id) REFERENCES identity.users(id) ON DELETE RESTRICT;
        ALTER TABLE analytics.analytics_reports
            ADD CONSTRAINT fk_analytics_reports_actor
            FOREIGN KEY (actor_id) REFERENCES identity.users(id) ON DELETE SET NULL;
        ALTER TABLE analytics.analytics_histories
            ADD CONSTRAINT fk_analytics_histories_owner
            FOREIGN KEY (owner_id) REFERENCES identity.users(id) ON DELETE RESTRICT;
        ALTER TABLE analytics.analytics_histories
            ADD CONSTRAINT fk_analytics_histories_actor
            FOREIGN KEY (actor_id) REFERENCES identity.users(id) ON DELETE SET NULL;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_actual_records_owner_task_date
    ON analytics.actual_records(owner_id, task_id, actual_date DESC);
CREATE INDEX IF NOT EXISTS idx_actual_records_owner_cycle_date
    ON analytics.actual_records(owner_id, capital_cycle_id, actual_date DESC);
CREATE INDEX IF NOT EXISTS idx_actual_records_owner_category_date
    ON analytics.actual_records(owner_id, category_id, actual_date DESC);
CREATE INDEX IF NOT EXISTS idx_actual_records_type_status
    ON analytics.actual_records(record_type, status);
CREATE INDEX IF NOT EXISTS idx_actual_records_created
    ON analytics.actual_records(owner_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_evaluation_results_owner_task_generated
    ON analytics.evaluation_results(owner_id, task_id, generated_at DESC);
CREATE INDEX IF NOT EXISTS idx_evaluation_results_owner_cycle_period
    ON analytics.evaluation_results(owner_id, capital_cycle_id, period_start, period_end);
CREATE INDEX IF NOT EXISTS idx_evaluation_results_status_generated
    ON analytics.evaluation_results(status, generated_at DESC);

CREATE INDEX IF NOT EXISTS idx_analytics_reports_owner_type_period
    ON analytics.analytics_reports(owner_id, report_type, period_start, period_end);
CREATE INDEX IF NOT EXISTS idx_analytics_reports_status_dimension
    ON analytics.analytics_reports(status, dimension);
CREATE INDEX IF NOT EXISTS idx_analytics_reports_generated
    ON analytics.analytics_reports(owner_id, generated_at DESC);

CREATE INDEX IF NOT EXISTS idx_analytics_histories_owner_time
    ON analytics.analytics_histories(owner_id, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_analytics_histories_action_time
    ON analytics.analytics_histories(action_type, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_analytics_histories_actual_record
    ON analytics.analytics_histories(actual_record_id, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_analytics_histories_evaluation
    ON analytics.analytics_histories(evaluation_result_id, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_analytics_histories_report
    ON analytics.analytics_histories(report_id, occurred_at DESC);
