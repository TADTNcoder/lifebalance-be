package com.lifebalance.analytics.service.impl;

import com.lifebalance.analytics.domain.ActualRecord;
import com.lifebalance.analytics.domain.AnalyticsHistoryActionType;
import com.lifebalance.analytics.domain.EvaluationResult;
import com.lifebalance.analytics.domain.EvaluationStatus;
import com.lifebalance.analytics.dto.EvaluateTaskRequest;
import com.lifebalance.analytics.dto.EvaluationResultResponse;
import com.lifebalance.analytics.dto.ReasonRequest;
import com.lifebalance.analytics.error.AnalyticsExceptions;
import com.lifebalance.analytics.repository.ActualRecordRepository;
import com.lifebalance.analytics.repository.EvaluationResultRepository;
import com.lifebalance.analytics.service.EvaluationService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class EvaluationServiceImpl implements EvaluationService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final OffsetDateTime EARLIEST_EVALUATION_DATE =
            OffsetDateTime.parse("0001-01-01T00:00:00Z");
    private static final OffsetDateTime LATEST_EVALUATION_DATE =
            OffsetDateTime.parse("9999-12-31T23:59:59.999999999Z");

    private final ActualRecordRepository actualRecordRepository;
    private final EvaluationResultRepository evaluationResultRepository;
    private final AnalyticsHistoryRecorder historyRecorder;
    private final AnalyticsMapper mapper;

    EvaluationServiceImpl(
            ActualRecordRepository actualRecordRepository,
            EvaluationResultRepository evaluationResultRepository,
            AnalyticsHistoryRecorder historyRecorder,
            AnalyticsMapper mapper
    ) {
        this.actualRecordRepository = actualRecordRepository;
        this.evaluationResultRepository = evaluationResultRepository;
        this.historyRecorder = historyRecorder;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public EvaluationResultResponse evaluateTask(UUID ownerId, EvaluateTaskRequest request) {
        ActualRecordServiceImpl.validateOwner(ownerId);
        ActualRecordServiceImpl.validatePeriod(request.periodStart(), request.periodEnd());
        validateEvaluationRequest(request);

        String currencyCode = resolveCurrency(request);
        Integer actualMinutes = resolveActualMinutes(ownerId, request);
        BigDecimal actualCost = resolveActualCost(ownerId, request, currencyCode);
        Integer minuteVariance = variance(request.plannedMinutes(), actualMinutes);
        BigDecimal costVariance = variance(request.plannedCost(), actualCost);
        BigDecimal efficiencyPercent = averageEfficiencyPercent(
                efficiencyPercent(request.plannedMinutes(), actualMinutes),
                efficiencyPercent(request.plannedCost(), actualCost)
        );
        EvaluationStatus status = status(request.plannedMinutes(), actualMinutes, request.plannedCost(), actualCost);

        EvaluationResult result = EvaluationResult.create(
                ownerId,
                ownerId,
                request.taskId(),
                request.capitalCycleId(),
                request.periodStart(),
                request.periodEnd(),
                request.plannedMinutes(),
                actualMinutes,
                minuteVariance,
                request.plannedCost(),
                actualCost,
                costVariance,
                currencyCode,
                efficiencyPercent,
                status,
                request.reason()
        );

        EvaluationResult saved = evaluationResultRepository.save(result);
        historyRecorder.recordEvaluation(
                ownerId,
                ownerId,
                AnalyticsHistoryActionType.EVALUATION_GENERATED,
                saved,
                null,
                mapper.evaluationSnapshot(saved),
                request.reason()
        );
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public EvaluationResultResponse archive(UUID ownerId, UUID evaluationId, ReasonRequest request) {
        ActualRecordServiceImpl.validateOwner(ownerId);
        EvaluationResult result = evaluationResultRepository.findByIdAndOwnerIdForUpdate(evaluationId, ownerId)
                .orElseThrow(() -> AnalyticsExceptions.evaluationNotFound(evaluationId));
        String oldSnapshot = mapper.evaluationSnapshot(result);
        result.archive(ownerId);

        historyRecorder.recordEvaluation(
                ownerId,
                ownerId,
                AnalyticsHistoryActionType.EVALUATION_ARCHIVED,
                result,
                oldSnapshot,
                mapper.evaluationSnapshot(result),
                request == null ? null : request.reason()
        );
        return mapper.toResponse(result);
    }

    @Override
    @Transactional(readOnly = true)
    public EvaluationResultResponse getById(UUID ownerId, UUID evaluationId) {
        ActualRecordServiceImpl.validateOwner(ownerId);
        return evaluationResultRepository.findByIdAndOwnerId(evaluationId, ownerId)
                .map(mapper::toResponse)
                .orElseThrow(() -> AnalyticsExceptions.evaluationNotFound(evaluationId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EvaluationResultResponse> search(
            UUID ownerId,
            UUID taskId,
            UUID capitalCycleId,
            EvaluationStatus status,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable
    ) {
        ActualRecordServiceImpl.validateOwner(ownerId);
        if (from != null && to != null && from.isAfter(to)) {
            throw AnalyticsExceptions.invalidPeriod("from must be before or equal to to.");
        }
        OffsetDateTime normalizedFrom = from == null ? EARLIEST_EVALUATION_DATE : from;
        OffsetDateTime normalizedTo = to == null ? LATEST_EVALUATION_DATE : to;

        return evaluationResultRepository.search(
                        ownerId,
                        taskId,
                        capitalCycleId,
                        status,
                        normalizedFrom,
                        normalizedTo,
                        pageable
                )
                .map(mapper::toResponse);
    }

    private Integer resolveActualMinutes(UUID ownerId, EvaluateTaskRequest request) {
        if (request.actualMinutes() != null) {
            return request.actualMinutes();
        }
        if (request.plannedMinutes() == null) {
            return null;
        }
        Long actualMinutes = actualRecordRepository.sumActualMinutes(
                ownerId,
                request.taskId(),
                request.capitalCycleId(),
                request.periodStart(),
                request.periodEnd()
        );
        return actualMinutes == null ? 0 : Math.toIntExact(actualMinutes);
    }

    private BigDecimal resolveActualCost(UUID ownerId, EvaluateTaskRequest request, String currencyCode) {
        if (request.actualCost() != null) {
            return request.actualCost();
        }
        if (request.plannedCost() == null) {
            return null;
        }
        BigDecimal actualCost = actualRecordRepository.sumActualCost(
                ownerId,
                request.taskId(),
                request.capitalCycleId(),
                currencyCode,
                request.periodStart(),
                request.periodEnd()
        );
        return actualCost == null ? BigDecimal.ZERO : actualCost;
    }

    private static void validateEvaluationRequest(EvaluateTaskRequest request) {
        if (request.taskId() == null) {
            throw AnalyticsExceptions.invalidRequest("taskId is required.");
        }
        boolean hasAnyValue = request.plannedMinutes() != null
                || request.actualMinutes() != null
                || request.plannedCost() != null
                || request.actualCost() != null;
        if (!hasAnyValue) {
            throw AnalyticsExceptions.invalidRequest("At least one planned or actual value is required.");
        }
        if ((request.plannedCost() != null || request.actualCost() != null)
                && ActualRecord.normalizeText(request.currencyCode(), 3) == null) {
            throw AnalyticsExceptions.invalidCurrency(request.currencyCode());
        }
    }

    private static String resolveCurrency(EvaluateTaskRequest request) {
        if (request.plannedCost() == null && request.actualCost() == null) {
            return null;
        }
        return ActualRecord.normalizeCurrency(request.currencyCode());
    }

    private static Integer variance(Integer planned, Integer actual) {
        if (planned == null || actual == null) {
            return null;
        }
        return actual - planned;
    }

    private static BigDecimal variance(BigDecimal planned, BigDecimal actual) {
        if (planned == null || actual == null) {
            return null;
        }
        return actual.subtract(planned);
    }

    private static EvaluationStatus status(
            Integer plannedMinutes,
            Integer actualMinutes,
            BigDecimal plannedCost,
            BigDecimal actualCost
    ) {
        boolean hasPlan = plannedMinutes != null || plannedCost != null;
        if (!hasPlan) {
            return EvaluationStatus.NO_PLAN;
        }
        List<Integer> comparisons = new ArrayList<>();
        if (plannedMinutes != null && actualMinutes != null) {
            comparisons.add(Integer.compare(actualMinutes, plannedMinutes));
        }
        if (plannedCost != null && actualCost != null) {
            comparisons.add(actualCost.compareTo(plannedCost));
        }
        if (comparisons.stream().anyMatch(value -> value > 0)) {
            return EvaluationStatus.OVER_PLANNED;
        }
        if (comparisons.stream().allMatch(value -> value == 0)) {
            return EvaluationStatus.ON_TRACK;
        }
        return EvaluationStatus.UNDER_PLANNED;
    }

    private static BigDecimal efficiencyPercent(Integer planned, Integer actual) {
        if (planned == null || actual == null || actual == 0) {
            return null;
        }
        return BigDecimal.valueOf(planned)
                .multiply(ONE_HUNDRED)
                .divide(BigDecimal.valueOf(actual), 4, RoundingMode.HALF_UP);
    }

    private static BigDecimal efficiencyPercent(BigDecimal planned, BigDecimal actual) {
        if (planned == null || actual == null || actual.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return planned.multiply(ONE_HUNDRED).divide(actual, 4, RoundingMode.HALF_UP);
    }

    static BigDecimal averageEfficiencyPercent(List<BigDecimal> values) {
        List<BigDecimal> usableValues = values.stream()
                .filter(value -> value != null)
                .toList();
        if (usableValues.isEmpty()) {
            return null;
        }
        BigDecimal total = usableValues.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(usableValues.size()), 4, RoundingMode.HALF_UP);
    }

    private static BigDecimal averageEfficiencyPercent(BigDecimal timeEfficiency, BigDecimal costEfficiency) {
        List<BigDecimal> values = new ArrayList<>();
        values.add(timeEfficiency);
        values.add(costEfficiency);
        return averageEfficiencyPercent(values);
    }
}
