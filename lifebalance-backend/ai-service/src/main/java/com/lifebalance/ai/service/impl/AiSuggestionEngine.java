package com.lifebalance.ai.service.impl;

import com.lifebalance.ai.domain.AiInsightSeverity;
import com.lifebalance.ai.domain.AiInsightType;
import com.lifebalance.ai.domain.AiIntent;
import com.lifebalance.ai.domain.AiPriority;
import com.lifebalance.ai.domain.AiRecommendationType;
import java.math.BigDecimal;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
class AiSuggestionEngine {

    private static final String MODEL_NAME = "lifebalance-rule-engine-v1";

    AiGeneratedReply reply(AiIntent intent, String message) {
        AiIntent resolvedIntent = intent == null ? AiIntent.GENERAL : intent;
        String normalized = message == null ? "" : message.toLowerCase(Locale.ROOT);
        String focus = switch (resolvedIntent) {
            case TASK_PLANNING -> "Break the work into a small next action, reserve a clear owner, and keep the next checkpoint visible.";
            case TIMELINE_OPTIMIZATION -> "Protect the most constrained time block first, then move flexible work around it.";
            case CAPITAL_ADVICE -> "Compare available capital with current commitments before adding more allocation.";
            case FINANCE_REVIEW -> "Separate recurring commitments from discretionary spend before changing the plan.";
            case ANALYTICS_SUMMARY -> "Review variance first, then decide whether the plan or execution pattern should change.";
            case GENERAL -> "Clarify the desired outcome, then choose the smallest reversible next step.";
        };
        String caution = normalized.contains("over")
                || normalized.contains("conflict")
                || normalized.contains("risk")
                || normalized.contains("late")
                ? " I see risk language in the request, so I would validate constraints before changing state."
                : "";
        return new AiGeneratedReply(
                resolvedIntent,
                MODEL_NAME,
                focus + caution,
                recommendationType(resolvedIntent, normalized),
                priority(normalized),
                confidence(normalized)
        );
    }

    AiGeneratedRecommendation recommendation(
            AiIntent intent,
            String signalSummary,
            String targetType
    ) {
        AiIntent resolvedIntent = intent == null ? AiIntent.GENERAL : intent;
        String normalized = signalSummary == null ? "" : signalSummary.toLowerCase(Locale.ROOT);
        AiRecommendationType type = recommendationType(resolvedIntent, normalized);
        AiPriority priority = priority(normalized);
        String target = targetType == null ? "current plan" : targetType.toLowerCase(Locale.ROOT);
        return new AiGeneratedRecommendation(
                type,
                priority,
                recommendationTitle(type, priority),
                "Review " + target + " with the latest signal: " + trimSignal(signalSummary)
                        + ". Apply only after the user confirms the operational change.",
                confidence(normalized)
        );
    }

    AiGeneratedInsight insight(AiInsightType requestedType, String signalSummary) {
        String normalized = signalSummary == null ? "" : signalSummary.toLowerCase(Locale.ROOT);
        AiInsightType type = requestedType == null ? insightType(normalized) : requestedType;
        AiInsightSeverity severity = severity(normalized);
        return new AiGeneratedInsight(
                type,
                severity,
                insightTitle(type, severity),
                "Detected pattern: " + trimSignal(signalSummary)
                        + ". Treat this as advisory context and keep source records unchanged until a user action is approved.",
                confidence(normalized)
        );
    }

    private static AiRecommendationType recommendationType(AiIntent intent, String normalizedSignal) {
        if (normalizedSignal.contains("budget") || normalizedSignal.contains("cost") || normalizedSignal.contains("money")) {
            return AiRecommendationType.BUDGET_OPTIMIZATION;
        }
        if (normalizedSignal.contains("capital") || normalizedSignal.contains("allocation")) {
            return AiRecommendationType.CAPITAL_ALLOCATION;
        }
        if (normalizedSignal.contains("schedule") || normalizedSignal.contains("timeline") || normalizedSignal.contains("conflict")) {
            return AiRecommendationType.SCHEDULE_ADJUSTMENT;
        }
        if (normalizedSignal.contains("focus") || normalizedSignal.contains("deep work")) {
            return AiRecommendationType.FOCUS_PROTECTION;
        }
        return switch (intent) {
            case TASK_PLANNING -> AiRecommendationType.TASK_PRIORITY;
            case TIMELINE_OPTIMIZATION -> AiRecommendationType.SCHEDULE_ADJUSTMENT;
            case CAPITAL_ADVICE -> AiRecommendationType.CAPITAL_ALLOCATION;
            case FINANCE_REVIEW -> AiRecommendationType.BUDGET_OPTIMIZATION;
            case ANALYTICS_SUMMARY, GENERAL -> AiRecommendationType.GENERAL;
        };
    }

    private static AiInsightType insightType(String normalizedSignal) {
        if (normalizedSignal.contains("conflict") || normalizedSignal.contains("overlap")) {
            return AiInsightType.TIMELINE_CONFLICT;
        }
        if (normalizedSignal.contains("capital") || normalizedSignal.contains("over allocation")) {
            return AiInsightType.CAPITAL_RISK;
        }
        if (normalizedSignal.contains("cost") || normalizedSignal.contains("budget") || normalizedSignal.contains("money")) {
            return AiInsightType.FINANCE_PATTERN;
        }
        if (normalizedSignal.contains("load") || normalizedSignal.contains("task")) {
            return AiInsightType.TASK_LOAD;
        }
        if (normalizedSignal.contains("balance") || normalizedSignal.contains("burnout")) {
            return AiInsightType.BALANCE_RISK;
        }
        return AiInsightType.GENERAL;
    }

    private static AiPriority priority(String normalizedSignal) {
        if (normalizedSignal.contains("critical") || normalizedSignal.contains("blocked") || normalizedSignal.contains("overdue")) {
            return AiPriority.HIGH;
        }
        if (normalizedSignal.contains("risk") || normalizedSignal.contains("conflict") || normalizedSignal.contains("shortage")) {
            return AiPriority.HIGH;
        }
        if (normalizedSignal.contains("soon") || normalizedSignal.contains("review")) {
            return AiPriority.MEDIUM;
        }
        return AiPriority.LOW;
    }

    private static AiInsightSeverity severity(String normalizedSignal) {
        if (normalizedSignal.contains("critical") || normalizedSignal.contains("blocked") || normalizedSignal.contains("overdue")) {
            return AiInsightSeverity.CRITICAL;
        }
        if (normalizedSignal.contains("risk") || normalizedSignal.contains("conflict") || normalizedSignal.contains("shortage")) {
            return AiInsightSeverity.WARNING;
        }
        return AiInsightSeverity.INFO;
    }

    private static BigDecimal confidence(String normalizedSignal) {
        if (normalizedSignal.contains("critical") || normalizedSignal.contains("blocked") || normalizedSignal.contains("overdue")) {
            return new BigDecimal("0.8600");
        }
        if (normalizedSignal.contains("risk") || normalizedSignal.contains("conflict") || normalizedSignal.contains("shortage")) {
            return new BigDecimal("0.7800");
        }
        return new BigDecimal("0.6400");
    }

    private static String recommendationTitle(AiRecommendationType type, AiPriority priority) {
        return switch (type) {
            case TASK_PRIORITY -> "Review task priority";
            case SCHEDULE_ADJUSTMENT -> "Adjust schedule pressure";
            case CAPITAL_ALLOCATION -> "Review capital allocation";
            case BUDGET_OPTIMIZATION -> "Review budget pressure";
            case FOCUS_PROTECTION -> "Protect focus time";
            case GENERAL -> priority == AiPriority.HIGH ? "Review high-priority signal" : "Review AI signal";
        };
    }

    private static String insightTitle(AiInsightType type, AiInsightSeverity severity) {
        return switch (type) {
            case TASK_LOAD -> "Task load signal";
            case TIMELINE_CONFLICT -> "Timeline conflict signal";
            case CAPITAL_RISK -> "Capital risk signal";
            case FINANCE_PATTERN -> "Finance pattern signal";
            case BALANCE_RISK -> "Balance risk signal";
            case GENERAL -> severity == AiInsightSeverity.CRITICAL ? "Critical AI signal" : "AI signal";
        };
    }

    private static String trimSignal(String signalSummary) {
        if (signalSummary == null || signalSummary.isBlank()) {
            return "no signal summary provided";
        }
        String normalized = signalSummary.trim();
        return normalized.length() <= 220 ? normalized : normalized.substring(0, 220);
    }

    record AiGeneratedReply(
            AiIntent intent,
            String modelName,
            String content,
            AiRecommendationType recommendationType,
            AiPriority priority,
            BigDecimal confidenceScore
    ) {
    }

    record AiGeneratedRecommendation(
            AiRecommendationType recommendationType,
            AiPriority priority,
            String title,
            String description,
            BigDecimal confidenceScore
    ) {
    }

    record AiGeneratedInsight(
            AiInsightType insightType,
            AiInsightSeverity severity,
            String title,
            String summary,
            BigDecimal confidenceScore
    ) {
    }
}
