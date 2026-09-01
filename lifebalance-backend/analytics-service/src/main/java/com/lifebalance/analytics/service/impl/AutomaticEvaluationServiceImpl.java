package com.lifebalance.analytics.service.impl;

import com.lifebalance.analytics.domain.ActualRecordStatus;
import com.lifebalance.analytics.domain.EvaluationResult;
import com.lifebalance.analytics.domain.EvaluationStatus;
import com.lifebalance.analytics.dto.AutomaticEvaluationBaselineRequest;
import com.lifebalance.analytics.dto.EvaluateTaskRequest;
import com.lifebalance.analytics.repository.ActualRecordRepository;
import com.lifebalance.analytics.repository.EvaluationResultRepository;
import com.lifebalance.analytics.service.EvaluationService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
class AutomaticEvaluationServiceImpl implements AutomaticEvaluationService {

    private static final String DEFAULT_REASON = "Đánh giá tự động sau khi ghi nhận thay đổi.";

    private final EvaluationService evaluationService;
    private final ActualRecordRepository actualRecordRepository;
    private final EvaluationResultRepository evaluationResultRepository;

    AutomaticEvaluationServiceImpl(
            EvaluationService evaluationService,
            ActualRecordRepository actualRecordRepository,
            EvaluationResultRepository evaluationResultRepository
    ) {
        this.evaluationService = evaluationService;
        this.actualRecordRepository = actualRecordRepository;
        this.evaluationResultRepository = evaluationResultRepository;
    }

    @Override
    public void evaluateAfterActualChange(
            UUID ownerId,
            AutomaticEvaluationTarget target,
            AutomaticEvaluationBaselineRequest requestedBaseline,
            String changeReason
    ) {
        if (ownerId == null || target == null || target.taskId() == null) {
            return;
        }

        Optional<EvaluationResult> latest = evaluationResultRepository
                .findFirstByOwnerIdAndTaskIdAndStatusNotOrderByGeneratedAtDesc(
                        ownerId,
                        target.taskId(),
                        EvaluationStatus.ARCHIVED
                );

        // An archived record with no previous plan and no remaining actual data
        // has nothing meaningful to compare. Keep the actual/history mutation.
        if (!target.active()
                && requestedBaseline == null
                && latest.isEmpty()
                && actualRecordRepository.countByOwnerIdAndTaskIdAndStatus(
                        ownerId,
                        target.taskId(),
                        ActualRecordStatus.ACTIVE
                ) == 0) {
            return;
        }

        AutomaticEvaluationBaselineRequest baseline = requestedBaseline != null
                ? requestedBaseline
                : latest.map(AutomaticEvaluationServiceImpl::baselineOf).orElse(null);
        LocalDate fallbackDate = target.actualDate() == null ? LocalDate.now() : target.actualDate();
        LocalDate periodStart = baseline == null ? fallbackDate.withDayOfMonth(1) : baseline.periodStart();
        LocalDate periodEnd = baseline == null
                ? fallbackDate.withDayOfMonth(fallbackDate.lengthOfMonth())
                : baseline.periodEnd();
        if (target.active() && target.actualDate() != null) {
            if (periodStart != null && target.actualDate().isBefore(periodStart)) {
                periodStart = target.actualDate();
            }
            if (periodEnd != null && target.actualDate().isAfter(periodEnd)) {
                periodEnd = target.actualDate();
            }
        }
        String currencyCode = baseline != null && baseline.currencyCode() != null
                ? baseline.currencyCode()
                : target.currencyCode();
        boolean aggregateTime = target.hasTime()
                || (baseline != null && baseline.plannedMinutes() != null);
        boolean aggregateCost = target.hasCost()
                || (baseline != null && baseline.plannedCost() != null);
        Integer actualMinutes = aggregateTime
                ? sumMinutes(ownerId, target, periodStart, periodEnd)
                : null;
        BigDecimal actualCost = aggregateCost && currencyCode != null
                ? sumCost(ownerId, target, currencyCode, periodStart, periodEnd)
                : null;

        evaluationService.evaluateTask(
                ownerId,
                new EvaluateTaskRequest(
                        target.taskId(),
                        target.capitalCycleId(),
                        periodStart,
                        periodEnd,
                        baseline == null ? null : baseline.plannedMinutes(),
                        actualMinutes,
                        baseline == null ? null : baseline.plannedCost(),
                        actualCost,
                        currencyCode,
                        automaticReason(changeReason)
                )
        );
    }

    private Integer sumMinutes(
            UUID ownerId,
            AutomaticEvaluationTarget target,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {
        Long value = actualRecordRepository.sumActualMinutes(
                ownerId,
                target.taskId(),
                target.capitalCycleId(),
                periodStart,
                periodEnd
        );
        return value == null ? 0 : Math.toIntExact(value);
    }

    private BigDecimal sumCost(
            UUID ownerId,
            AutomaticEvaluationTarget target,
            String currencyCode,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {
        BigDecimal value = actualRecordRepository.sumActualCost(
                ownerId,
                target.taskId(),
                target.capitalCycleId(),
                currencyCode,
                periodStart,
                periodEnd
        );
        return value == null ? BigDecimal.ZERO : value;
    }

    private static AutomaticEvaluationBaselineRequest baselineOf(EvaluationResult evaluation) {
        return new AutomaticEvaluationBaselineRequest(
                evaluation.getPeriodStart(),
                evaluation.getPeriodEnd(),
                evaluation.getPlannedMinutes(),
                evaluation.getPlannedCost(),
                evaluation.getCurrencyCode()
        );
    }

    private static String automaticReason(String changeReason) {
        if (changeReason == null || changeReason.isBlank()) {
            return DEFAULT_REASON;
        }
        String reason = "Đánh giá tự động: " + changeReason.trim();
        return reason.substring(0, Math.min(1000, reason.length()));
    }
}
