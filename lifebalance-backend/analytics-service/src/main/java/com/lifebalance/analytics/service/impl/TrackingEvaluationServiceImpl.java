package com.lifebalance.analytics.service.impl;

import com.lifebalance.analytics.domain.ActualRecord;
import com.lifebalance.analytics.domain.EvaluationResult;
import com.lifebalance.analytics.domain.EvaluationStatus;
import com.lifebalance.analytics.domain.TrendGranularity;
import com.lifebalance.analytics.dto.ComparePeriodsRequest;
import com.lifebalance.analytics.dto.EvaluationDeltaResponse;
import com.lifebalance.analytics.dto.EvaluationTrendPointResponse;
import com.lifebalance.analytics.dto.PeriodComparisonResponse;
import com.lifebalance.analytics.dto.PlannedActualDetailResponse;
import com.lifebalance.analytics.dto.ResourceUtilizationResponse;
import com.lifebalance.analytics.dto.TrackingEvaluationSummaryResponse;
import com.lifebalance.analytics.error.AnalyticsExceptions;
import com.lifebalance.analytics.repository.ActualRecordRepository;
import com.lifebalance.analytics.repository.EvaluationResultRepository;
import com.lifebalance.analytics.service.TrackingEvaluationService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class TrackingEvaluationServiceImpl implements TrackingEvaluationService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final ActualRecordRepository actualRecordRepository;
    private final EvaluationResultRepository evaluationResultRepository;

    TrackingEvaluationServiceImpl(
            ActualRecordRepository actualRecordRepository,
            EvaluationResultRepository evaluationResultRepository
    ) {
        this.actualRecordRepository = actualRecordRepository;
        this.evaluationResultRepository = evaluationResultRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public TrackingEvaluationSummaryResponse summary(
            UUID ownerId,
            LocalDate periodStart,
            LocalDate periodEnd,
            String currencyCode
    ) {
        ActualRecordServiceImpl.validateOwner(ownerId);
        ActualRecordServiceImpl.validatePeriod(periodStart, periodEnd);
        String normalizedCurrency = normalizeCurrencyOrNull(currencyCode);
        List<EvaluationResult> results = evaluationResultRepository.findActiveByOwnerAndPeriod(
                ownerId,
                periodStart,
                periodEnd
        );
        return summarize(
                ownerId,
                periodStart,
                periodEnd,
                normalizedCurrency,
                results,
                actualRecordRepository.countActiveRecords(ownerId, periodStart, periodEnd)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PlannedActualDetailResponse> plannedVsActual(
            UUID ownerId,
            UUID taskId,
            UUID capitalCycleId,
            EvaluationStatus status,
            LocalDate periodStart,
            LocalDate periodEnd,
            Pageable pageable
    ) {
        ActualRecordServiceImpl.validateOwner(ownerId);
        ActualRecordServiceImpl.validatePeriod(periodStart, periodEnd);
        return evaluationResultRepository.searchByEvaluationPeriod(
                ownerId,
                taskId,
                capitalCycleId,
                status,
                periodStart,
                periodEnd,
                pageable
        ).map(this::toPlannedActualDetail);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceUtilizationResponse> resourceUtilization(
            UUID ownerId,
            LocalDate periodStart,
            LocalDate periodEnd,
            String currencyCode
    ) {
        ActualRecordServiceImpl.validateOwner(ownerId);
        ActualRecordServiceImpl.validatePeriod(periodStart, periodEnd);
        String normalizedCurrency = normalizeCurrencyOrNull(currencyCode);
        List<EvaluationResult> results = evaluationResultRepository.findActiveByOwnerAndPeriod(
                ownerId,
                periodStart,
                periodEnd
        );

        List<ResourceUtilizationResponse> utilization = new ArrayList<>();
        utilization.add(timeUtilization(results));
        if (normalizedCurrency != null) {
            utilization.add(moneyUtilization(results, normalizedCurrency));
        }
        return utilization;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvaluationTrendPointResponse> trend(
            UUID ownerId,
            LocalDate periodStart,
            LocalDate periodEnd,
            TrendGranularity granularity,
            String currencyCode
    ) {
        ActualRecordServiceImpl.validateOwner(ownerId);
        validateRequiredPeriod(periodStart, periodEnd);
        String normalizedCurrency = normalizeCurrencyOrNull(currencyCode);
        TrendGranularity resolvedGranularity = granularity == null ? TrendGranularity.MONTHLY : granularity;
        List<EvaluationResult> results = evaluationResultRepository.findActiveByOwnerAndPeriod(
                ownerId,
                periodStart,
                periodEnd
        );
        Map<LocalDate, List<EvaluationResult>> resultsByBucket = bucketResults(results, resolvedGranularity);

        List<EvaluationTrendPointResponse> trend = new ArrayList<>();
        LocalDate cursor = bucketStart(periodStart, resolvedGranularity);
        while (!cursor.isAfter(periodEnd)) {
            LocalDate bucketStart = max(cursor, periodStart);
            LocalDate bucketEnd = min(bucketEnd(cursor, resolvedGranularity), periodEnd);
            TrackingEvaluationSummaryResponse bucket = summarize(
                    ownerId,
                    bucketStart,
                    bucketEnd,
                    normalizedCurrency,
                    resultsByBucket.getOrDefault(cursor, List.of()),
                    actualRecordRepository.countActiveRecords(ownerId, bucketStart, bucketEnd)
            );
            trend.add(new EvaluationTrendPointResponse(
                    bucket.periodStart(),
                    bucket.periodEnd(),
                    bucket.evaluatedTaskCount(),
                    bucket.actualRecordCount(),
                    bucket.plannedMinutes(),
                    bucket.actualMinutes(),
                    bucket.minuteVariance(),
                    bucket.plannedCost(),
                    bucket.actualCost(),
                    bucket.costVariance(),
                    bucket.overallEfficiencyPercent(),
                    bucket.planningAccuracyPercent(),
                    bucket.productivityScore()
            ));
            cursor = nextBucket(cursor, resolvedGranularity);
        }
        return trend;
    }

    @Override
    @Transactional(readOnly = true)
    public PeriodComparisonResponse comparePeriods(UUID ownerId, ComparePeriodsRequest request) {
        ActualRecordServiceImpl.validateOwner(ownerId);
        validateRequiredPeriod(request.baselineStart(), request.baselineEnd());
        validateRequiredPeriod(request.comparisonStart(), request.comparisonEnd());
        String normalizedCurrency = normalizeCurrencyOrNull(request.currencyCode());
        TrackingEvaluationSummaryResponse baseline = summary(
                ownerId,
                request.baselineStart(),
                request.baselineEnd(),
                normalizedCurrency
        );
        TrackingEvaluationSummaryResponse comparison = summary(
                ownerId,
                request.comparisonStart(),
                request.comparisonEnd(),
                normalizedCurrency
        );
        return new PeriodComparisonResponse(ownerId, baseline, comparison, delta(baseline, comparison));
    }

    private TrackingEvaluationSummaryResponse summarize(
            UUID ownerId,
            LocalDate periodStart,
            LocalDate periodEnd,
            String currencyCode,
            List<EvaluationResult> results,
            long actualRecordCount
    ) {
        List<EvaluationResult> moneyResults = filterMoneyResults(results, currencyCode);
        Integer plannedMinutes = sumIntegers(results.stream().map(EvaluationResult::getPlannedMinutes));
        Integer actualMinutes = sumIntegers(results.stream().map(EvaluationResult::getActualMinutes));
        Integer minuteVariance = sumIntegers(results.stream().map(EvaluationResult::getMinuteVariance));
        BigDecimal plannedCost = currencyCode == null
                ? null
                : sumDecimals(moneyResults.stream().map(EvaluationResult::getPlannedCost));
        BigDecimal actualCost = currencyCode == null
                ? null
                : sumDecimals(moneyResults.stream().map(EvaluationResult::getActualCost));
        BigDecimal costVariance = currencyCode == null
                ? null
                : sumDecimals(moneyResults.stream().map(EvaluationResult::getCostVariance));
        BigDecimal timeEfficiency = efficiency(plannedMinutes, actualMinutes);
        BigDecimal costEfficiency = efficiency(plannedCost, actualCost);
        BigDecimal overallEfficiency = EvaluationServiceImpl.averageEfficiencyPercent(nullableValues(
                timeEfficiency,
                costEfficiency
        ));
        BigDecimal planningAccuracy = averagePercent(nullableValues(
                accuracy(plannedMinutes, actualMinutes),
                accuracy(plannedCost, actualCost)
        ));
        BigDecimal dataCompleteness = dataCompleteness(results, currencyCode);
        BigDecimal productivityScore = productivityScore(planningAccuracy, overallEfficiency, dataCompleteness);

        return new TrackingEvaluationSummaryResponse(
                ownerId,
                periodStart,
                periodEnd,
                currencyCode,
                results.stream().map(EvaluationResult::getTaskId).filter(Objects::nonNull).distinct().count(),
                results.stream().filter(result -> isComparable(result, currencyCode)).count(),
                actualRecordCount,
                countStatus(results, EvaluationStatus.ON_TRACK),
                countStatus(results, EvaluationStatus.UNDER_PLANNED),
                countStatus(results, EvaluationStatus.OVER_PLANNED),
                countStatus(results, EvaluationStatus.NO_PLAN),
                plannedMinutes,
                actualMinutes,
                minuteVariance,
                plannedCost,
                actualCost,
                costVariance,
                timeEfficiency,
                costEfficiency,
                overallEfficiency,
                planningAccuracy,
                productivityScore,
                dataCompleteness
        );
    }

    private PlannedActualDetailResponse toPlannedActualDetail(EvaluationResult result) {
        return new PlannedActualDetailResponse(
                result.getId(),
                result.getTaskId(),
                result.getCapitalCycleId(),
                result.getPeriodStart(),
                result.getPeriodEnd(),
                result.getPlannedMinutes(),
                result.getActualMinutes(),
                result.getMinuteVariance(),
                result.getPlannedCost(),
                result.getActualCost(),
                result.getCostVariance(),
                result.getCurrencyCode(),
                result.getEfficiencyPercent(),
                averagePercent(nullableValues(
                        accuracy(result.getPlannedMinutes(), result.getActualMinutes()),
                        accuracy(result.getPlannedCost(), result.getActualCost())
                )),
                result.getStatus(),
                result.getGeneratedAt()
        );
    }

    private static ResourceUtilizationResponse timeUtilization(List<EvaluationResult> results) {
        Integer planned = sumIntegers(results.stream().map(EvaluationResult::getPlannedMinutes));
        Integer actual = sumIntegers(results.stream().map(EvaluationResult::getActualMinutes));
        Integer variance = sumIntegers(results.stream().map(EvaluationResult::getMinuteVariance));
        return new ResourceUtilizationResponse(
                "TIME",
                "MINUTES",
                toDecimal(planned),
                toDecimal(actual),
                toDecimal(variance),
                efficiency(planned, actual),
                accuracy(planned, actual),
                count(results, result -> result.getPlannedMinutes() != null && result.getActualMinutes() != null),
                count(results, result -> result.getPlannedMinutes() == null && result.getActualMinutes() != null),
                count(results, result -> result.getPlannedMinutes() != null && result.getActualMinutes() == null),
                aggregateStatus(variance)
        );
    }

    private static ResourceUtilizationResponse moneyUtilization(List<EvaluationResult> results, String currencyCode) {
        List<EvaluationResult> moneyResults = filterMoneyResults(results, currencyCode);
        BigDecimal planned = sumDecimals(moneyResults.stream().map(EvaluationResult::getPlannedCost));
        BigDecimal actual = sumDecimals(moneyResults.stream().map(EvaluationResult::getActualCost));
        BigDecimal variance = sumDecimals(moneyResults.stream().map(EvaluationResult::getCostVariance));
        return new ResourceUtilizationResponse(
                "MONEY",
                currencyCode,
                planned,
                actual,
                variance,
                efficiency(planned, actual),
                accuracy(planned, actual),
                count(moneyResults, result -> result.getPlannedCost() != null && result.getActualCost() != null),
                count(moneyResults, result -> result.getPlannedCost() == null && result.getActualCost() != null),
                count(moneyResults, result -> result.getPlannedCost() != null && result.getActualCost() == null),
                aggregateStatus(variance)
        );
    }

    private static Map<LocalDate, List<EvaluationResult>> bucketResults(
            List<EvaluationResult> results,
            TrendGranularity granularity
    ) {
        Map<LocalDate, List<EvaluationResult>> buckets = new LinkedHashMap<>();
        for (EvaluationResult result : results) {
            LocalDate bucket = bucketStart(anchorDate(result), granularity);
            buckets.computeIfAbsent(bucket, ignored -> new ArrayList<>()).add(result);
        }
        return buckets;
    }

    private static LocalDate anchorDate(EvaluationResult result) {
        if (result.getPeriodStart() != null) {
            return result.getPeriodStart();
        }
        if (result.getPeriodEnd() != null) {
            return result.getPeriodEnd();
        }
        return result.getGeneratedAt().toLocalDate();
    }

    private static EvaluationDeltaResponse delta(
            TrackingEvaluationSummaryResponse baseline,
            TrackingEvaluationSummaryResponse comparison
    ) {
        return new EvaluationDeltaResponse(
                comparison.evaluatedTaskCount() - baseline.evaluatedTaskCount(),
                comparison.actualRecordCount() - baseline.actualRecordCount(),
                delta(comparison.plannedMinutes(), baseline.plannedMinutes()),
                delta(comparison.actualMinutes(), baseline.actualMinutes()),
                delta(comparison.minuteVariance(), baseline.minuteVariance()),
                delta(comparison.plannedCost(), baseline.plannedCost()),
                delta(comparison.actualCost(), baseline.actualCost()),
                delta(comparison.costVariance(), baseline.costVariance()),
                delta(comparison.overallEfficiencyPercent(), baseline.overallEfficiencyPercent()),
                delta(comparison.planningAccuracyPercent(), baseline.planningAccuracyPercent()),
                delta(comparison.productivityScore(), baseline.productivityScore())
        );
    }

    private static boolean isComparable(EvaluationResult result, String currencyCode) {
        boolean comparableTime = result.getPlannedMinutes() != null && result.getActualMinutes() != null;
        boolean comparableCost = result.getPlannedCost() != null
                && result.getActualCost() != null
                && (currencyCode == null || currencyCode.equals(result.getCurrencyCode()));
        return comparableTime || comparableCost;
    }

    private static BigDecimal dataCompleteness(List<EvaluationResult> results, String currencyCode) {
        if (results.isEmpty()) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        long comparable = results.stream().filter(result -> isComparable(result, currencyCode)).count();
        return BigDecimal.valueOf(comparable)
                .multiply(ONE_HUNDRED)
                .divide(BigDecimal.valueOf(results.size()), 4, RoundingMode.HALF_UP);
    }

    private static BigDecimal productivityScore(
            BigDecimal planningAccuracy,
            BigDecimal overallEfficiency,
            BigDecimal dataCompleteness
    ) {
        return averagePercent(nullableValues(
                planningAccuracy,
                capAtOneHundred(overallEfficiency),
                dataCompleteness
        ));
    }

    private static BigDecimal capAtOneHundred(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.min(ONE_HUNDRED);
    }

    private static long countStatus(List<EvaluationResult> results, EvaluationStatus status) {
        return results.stream().filter(result -> result.getStatus() == status).count();
    }

    private static Integer sumIntegers(Stream<Integer> values) {
        List<Integer> usableValues = values.filter(Objects::nonNull).toList();
        if (usableValues.isEmpty()) {
            return null;
        }
        return usableValues.stream().mapToInt(Integer::intValue).sum();
    }

    private static BigDecimal sumDecimals(Stream<BigDecimal> values) {
        List<BigDecimal> usableValues = values.filter(Objects::nonNull).toList();
        if (usableValues.isEmpty()) {
            return null;
        }
        return usableValues.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal toDecimal(Integer value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    private static BigDecimal efficiency(Integer planned, Integer actual) {
        if (planned == null || actual == null || actual == 0) {
            return null;
        }
        return BigDecimal.valueOf(planned)
                .multiply(ONE_HUNDRED)
                .divide(BigDecimal.valueOf(actual), 4, RoundingMode.HALF_UP);
    }

    private static BigDecimal efficiency(BigDecimal planned, BigDecimal actual) {
        if (planned == null || actual == null || actual.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return planned.multiply(ONE_HUNDRED).divide(actual, 4, RoundingMode.HALF_UP);
    }

    private static BigDecimal accuracy(Integer planned, Integer actual) {
        if (planned == null || actual == null) {
            return null;
        }
        return accuracy(BigDecimal.valueOf(planned), BigDecimal.valueOf(actual));
    }

    private static BigDecimal accuracy(BigDecimal planned, BigDecimal actual) {
        if (planned == null || actual == null) {
            return null;
        }
        if (planned.compareTo(BigDecimal.ZERO) == 0) {
            return actual.compareTo(BigDecimal.ZERO) == 0
                    ? ONE_HUNDRED.setScale(4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        BigDecimal varianceRate = actual.subtract(planned)
                .abs()
                .multiply(ONE_HUNDRED)
                .divide(planned, 4, RoundingMode.HALF_UP);
        return ONE_HUNDRED.subtract(varianceRate).max(BigDecimal.ZERO).setScale(4, RoundingMode.HALF_UP);
    }

    private static BigDecimal averagePercent(List<BigDecimal> values) {
        return EvaluationServiceImpl.averageEfficiencyPercent(values);
    }

    private static List<BigDecimal> nullableValues(BigDecimal first, BigDecimal second) {
        List<BigDecimal> values = new ArrayList<>();
        values.add(first);
        values.add(second);
        return values;
    }

    private static List<BigDecimal> nullableValues(BigDecimal first, BigDecimal second, BigDecimal third) {
        List<BigDecimal> values = nullableValues(first, second);
        values.add(third);
        return values;
    }

    private static List<EvaluationResult> filterMoneyResults(List<EvaluationResult> results, String currencyCode) {
        if (currencyCode == null) {
            return List.of();
        }
        return results.stream()
                .filter(result -> currencyCode.equals(result.getCurrencyCode()))
                .toList();
    }

    private static EvaluationStatus aggregateStatus(Integer variance) {
        if (variance == null) {
            return EvaluationStatus.NO_PLAN;
        }
        return aggregateStatus(BigDecimal.valueOf(variance));
    }

    private static EvaluationStatus aggregateStatus(BigDecimal variance) {
        if (variance == null) {
            return EvaluationStatus.NO_PLAN;
        }
        int comparison = variance.compareTo(BigDecimal.ZERO);
        if (comparison > 0) {
            return EvaluationStatus.OVER_PLANNED;
        }
        if (comparison < 0) {
            return EvaluationStatus.UNDER_PLANNED;
        }
        return EvaluationStatus.ON_TRACK;
    }

    private static <T> long count(List<T> values, java.util.function.Predicate<T> predicate) {
        return values.stream().filter(predicate).count();
    }

    private static Integer delta(Integer comparison, Integer baseline) {
        if (comparison == null || baseline == null) {
            return null;
        }
        return comparison - baseline;
    }

    private static BigDecimal delta(BigDecimal comparison, BigDecimal baseline) {
        if (comparison == null || baseline == null) {
            return null;
        }
        return comparison.subtract(baseline);
    }

    private static void validateRequiredPeriod(LocalDate periodStart, LocalDate periodEnd) {
        if (periodStart == null || periodEnd == null) {
            throw AnalyticsExceptions.invalidPeriod("periodStart and periodEnd are required.");
        }
        ActualRecordServiceImpl.validatePeriod(periodStart, periodEnd);
    }

    private static String normalizeCurrencyOrNull(String currencyCode) {
        if (ActualRecord.normalizeText(currencyCode, 3) == null) {
            return null;
        }
        return ActualRecord.normalizeCurrency(currencyCode);
    }

    private static LocalDate bucketStart(LocalDate date, TrendGranularity granularity) {
        return switch (granularity) {
            case DAILY -> date;
            case WEEKLY -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTHLY -> date.withDayOfMonth(1);
            case YEARLY -> date.withDayOfYear(1);
        };
    }

    private static LocalDate bucketEnd(LocalDate bucketStart, TrendGranularity granularity) {
        return switch (granularity) {
            case DAILY -> bucketStart;
            case WEEKLY -> bucketStart.plusDays(6);
            case MONTHLY -> bucketStart.with(TemporalAdjusters.lastDayOfMonth());
            case YEARLY -> bucketStart.with(TemporalAdjusters.lastDayOfYear());
        };
    }

    private static LocalDate nextBucket(LocalDate bucketStart, TrendGranularity granularity) {
        return switch (granularity) {
            case DAILY -> bucketStart.plusDays(1);
            case WEEKLY -> bucketStart.plusWeeks(1);
            case MONTHLY -> bucketStart.plusMonths(1);
            case YEARLY -> bucketStart.plusYears(1);
        };
    }

    private static LocalDate min(LocalDate first, LocalDate second) {
        return first.isBefore(second) ? first : second;
    }

    private static LocalDate max(LocalDate first, LocalDate second) {
        return first.isAfter(second) ? first : second;
    }
}
