CREATE SCHEMA IF NOT EXISTS ai;

CREATE TABLE IF NOT EXISTS ai.ai_conversations (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    actor_id UUID,
    title VARCHAR(200) NOT NULL,
    intent VARCHAR(64) NOT NULL,
    context_type VARCHAR(64),
    context_id UUID,
    status VARCHAR(16) NOT NULL,
    archived_at TIMESTAMP WITH TIME ZONE,
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_ai_conversations_intent CHECK (
        intent IN (
            'TASK_PLANNING',
            'TIMELINE_OPTIMIZATION',
            'CAPITAL_ADVICE',
            'FINANCE_REVIEW',
            'ANALYTICS_SUMMARY',
            'GENERAL'
        )
    ),
    CONSTRAINT chk_ai_conversations_status CHECK (status IN ('ACTIVE', 'ARCHIVED')),
    CONSTRAINT chk_ai_conversations_context CHECK (
        (context_type IS NULL AND context_id IS NULL)
        OR (context_type IS NOT NULL AND context_id IS NOT NULL)
    )
);

CREATE TABLE IF NOT EXISTS ai.ai_messages (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL,
    owner_id UUID NOT NULL,
    actor_id UUID,
    message_role VARCHAR(16) NOT NULL,
    intent VARCHAR(64) NOT NULL,
    content VARCHAR(4000) NOT NULL,
    model_name VARCHAR(120),
    token_estimate INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ai_messages_conversation
        FOREIGN KEY (conversation_id)
        REFERENCES ai.ai_conversations(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_ai_messages_role CHECK (message_role IN ('USER', 'ASSISTANT', 'SYSTEM')),
    CONSTRAINT chk_ai_messages_intent CHECK (
        intent IN (
            'TASK_PLANNING',
            'TIMELINE_OPTIMIZATION',
            'CAPITAL_ADVICE',
            'FINANCE_REVIEW',
            'ANALYTICS_SUMMARY',
            'GENERAL'
        )
    ),
    CONSTRAINT chk_ai_messages_token_estimate CHECK (token_estimate >= 0)
);

CREATE TABLE IF NOT EXISTS ai.ai_recommendations (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    actor_id UUID,
    recommendation_type VARCHAR(64) NOT NULL,
    status VARCHAR(16) NOT NULL,
    priority VARCHAR(16) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    source_type VARCHAR(64),
    source_id UUID,
    target_type VARCHAR(64),
    target_id UUID,
    confidence_score NUMERIC(5, 4) NOT NULL,
    signal_summary VARCHAR(2000),
    generated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    decided_at TIMESTAMP WITH TIME ZONE,
    decision_reason VARCHAR(1000),
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_ai_recommendations_type CHECK (
        recommendation_type IN (
            'TASK_PRIORITY',
            'SCHEDULE_ADJUSTMENT',
            'CAPITAL_ALLOCATION',
            'BUDGET_OPTIMIZATION',
            'FOCUS_PROTECTION',
            'GENERAL'
        )
    ),
    CONSTRAINT chk_ai_recommendations_status CHECK (status IN ('PENDING', 'APPLIED', 'DISMISSED')),
    CONSTRAINT chk_ai_recommendations_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
    CONSTRAINT chk_ai_recommendations_confidence CHECK (
        confidence_score >= 0
        AND confidence_score <= 1
    ),
    CONSTRAINT chk_ai_recommendations_source CHECK (
        (source_type IS NULL AND source_id IS NULL)
        OR (source_type IS NOT NULL AND source_id IS NOT NULL)
    ),
    CONSTRAINT chk_ai_recommendations_target CHECK (
        (target_type IS NULL AND target_id IS NULL)
        OR (target_type IS NOT NULL AND target_id IS NOT NULL)
    )
);

CREATE TABLE IF NOT EXISTS ai.ai_insights (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    actor_id UUID,
    insight_type VARCHAR(64) NOT NULL,
    severity VARCHAR(16) NOT NULL,
    status VARCHAR(16) NOT NULL,
    title VARCHAR(200) NOT NULL,
    summary VARCHAR(2000) NOT NULL,
    period_start DATE,
    period_end DATE,
    reference_type VARCHAR(64),
    reference_id UUID,
    confidence_score NUMERIC(5, 4) NOT NULL,
    signal_summary VARCHAR(2000),
    generated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    archived_at TIMESTAMP WITH TIME ZONE,
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_ai_insights_type CHECK (
        insight_type IN (
            'TASK_LOAD',
            'TIMELINE_CONFLICT',
            'CAPITAL_RISK',
            'FINANCE_PATTERN',
            'BALANCE_RISK',
            'GENERAL'
        )
    ),
    CONSTRAINT chk_ai_insights_severity CHECK (severity IN ('INFO', 'WARNING', 'CRITICAL')),
    CONSTRAINT chk_ai_insights_status CHECK (status IN ('ACTIVE', 'ARCHIVED')),
    CONSTRAINT chk_ai_insights_confidence CHECK (
        confidence_score >= 0
        AND confidence_score <= 1
    ),
    CONSTRAINT chk_ai_insights_period CHECK (
        period_start IS NULL OR period_end IS NULL OR period_start <= period_end
    ),
    CONSTRAINT chk_ai_insights_reference CHECK (
        (reference_type IS NULL AND reference_id IS NULL)
        OR (reference_type IS NOT NULL AND reference_id IS NOT NULL)
    )
);

CREATE TABLE IF NOT EXISTS ai.ai_histories (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    actor_id UUID,
    action_type VARCHAR(64) NOT NULL,
    conversation_id UUID,
    message_id UUID,
    recommendation_id UUID,
    insight_id UUID,
    old_value VARCHAR(4000),
    new_value VARCHAR(4000),
    reason VARCHAR(1000),
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ai_histories_conversation
        FOREIGN KEY (conversation_id)
        REFERENCES ai.ai_conversations(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_ai_histories_message
        FOREIGN KEY (message_id)
        REFERENCES ai.ai_messages(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_ai_histories_recommendation
        FOREIGN KEY (recommendation_id)
        REFERENCES ai.ai_recommendations(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_ai_histories_insight
        FOREIGN KEY (insight_id)
        REFERENCES ai.ai_insights(id)
        ON DELETE SET NULL,
    CONSTRAINT chk_ai_histories_action CHECK (
        action_type IN (
            'CONVERSATION_CREATED',
            'MESSAGE_RECORDED',
            'ASSISTANT_RESPONDED',
            'CONVERSATION_ARCHIVED',
            'RECOMMENDATION_GENERATED',
            'RECOMMENDATION_APPLIED',
            'RECOMMENDATION_DISMISSED',
            'INSIGHT_GENERATED',
            'INSIGHT_ARCHIVED'
        )
    )
);

CREATE INDEX IF NOT EXISTS idx_ai_conversations_owner_status
    ON ai.ai_conversations(owner_id, status, updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_ai_conversations_context
    ON ai.ai_conversations(owner_id, context_type, context_id);
CREATE INDEX IF NOT EXISTS idx_ai_messages_conversation_time
    ON ai.ai_messages(conversation_id, created_at ASC);
CREATE INDEX IF NOT EXISTS idx_ai_messages_owner_intent_time
    ON ai.ai_messages(owner_id, intent, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_ai_recommendations_owner_status
    ON ai.ai_recommendations(owner_id, status, generated_at DESC);
CREATE INDEX IF NOT EXISTS idx_ai_recommendations_type_priority
    ON ai.ai_recommendations(recommendation_type, priority, generated_at DESC);
CREATE INDEX IF NOT EXISTS idx_ai_recommendations_target
    ON ai.ai_recommendations(owner_id, target_type, target_id);

CREATE INDEX IF NOT EXISTS idx_ai_insights_owner_status
    ON ai.ai_insights(owner_id, status, generated_at DESC);
CREATE INDEX IF NOT EXISTS idx_ai_insights_type_severity
    ON ai.ai_insights(insight_type, severity, generated_at DESC);
CREATE INDEX IF NOT EXISTS idx_ai_insights_reference
    ON ai.ai_insights(owner_id, reference_type, reference_id);

CREATE INDEX IF NOT EXISTS idx_ai_histories_owner_time
    ON ai.ai_histories(owner_id, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_ai_histories_action_time
    ON ai.ai_histories(action_type, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_ai_histories_conversation
    ON ai.ai_histories(conversation_id, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_ai_histories_recommendation
    ON ai.ai_histories(recommendation_id, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_ai_histories_insight
    ON ai.ai_histories(insight_id, occurred_at DESC);
