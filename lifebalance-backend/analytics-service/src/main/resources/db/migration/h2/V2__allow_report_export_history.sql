ALTER TABLE analytics.analytics_histories
    DROP CONSTRAINT chk_analytics_histories_action;

ALTER TABLE analytics.analytics_histories
    ADD CONSTRAINT chk_analytics_histories_action CHECK (
        action_type IN (
            'ACTUAL_RECORDED',
            'ACTUAL_UPDATED',
            'ACTUAL_ARCHIVED',
            'EVALUATION_GENERATED',
            'EVALUATION_REGENERATED',
            'EVALUATION_ARCHIVED',
            'REPORT_GENERATED',
            'REPORT_ARCHIVED',
            'REPORT_EXPORTED'
        )
    );
