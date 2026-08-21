package com.lifebalance.analytics.service.impl;

import com.lifebalance.analytics.domain.ActualRecord;
import com.lifebalance.analytics.domain.AnalyticsHistory;
import com.lifebalance.analytics.domain.AnalyticsReport;
import com.lifebalance.analytics.domain.EvaluationResult;
import com.lifebalance.analytics.dto.ActualRecordResponse;
import com.lifebalance.analytics.dto.AnalyticsHistoryResponse;
import com.lifebalance.analytics.dto.AnalyticsReportResponse;
import com.lifebalance.analytics.dto.EvaluationResultResponse;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
class AnalyticsMapper {

    ActualRecordResponse toResponse(ActualRecord record) {
        return new ActualRecordResponse(
                record.getId(),
                record.getOwnerId(),
                record.getActorId(),
                record.getTaskId(),
                record.getCapitalCycleId(),
                record.getCategoryId(),
                toTagSet(record.getTagIds()),
                record.getRecordType(),
                record.getStatus(),
                record.getActualMinutes(),
                record.getActualCost(),
                record.getCurrencyCode(),
                record.getActualDate(),
                record.getRecordedAt(),
                record.getNote(),
                record.getSource(),
                record.getArchivedAt(),
                record.getCreatedBy(),
                record.getUpdatedBy(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }

    EvaluationResultResponse toResponse(EvaluationResult result) {
        return new EvaluationResultResponse(
                result.getId(),
                result.getOwnerId(),
                result.getActorId(),
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
                result.getStatus(),
                result.getGeneratedAt(),
                result.getReason(),
                result.getArchivedAt(),
                result.getCreatedBy(),
                result.getUpdatedBy(),
                result.getCreatedAt(),
                result.getUpdatedAt()
        );
    }

    AnalyticsReportResponse toResponse(AnalyticsReport report) {
        return new AnalyticsReportResponse(
                report.getId(),
                report.getOwnerId(),
                report.getActorId(),
                report.getReportType(),
                report.getStatus(),
                report.getDimension(),
                report.getPeriodStart(),
                report.getPeriodEnd(),
                report.getTaskCount(),
                report.getActualRecordCount(),
                report.getTotalActualMinutes(),
                report.getTotalActualCost(),
                report.getCurrencyCode(),
                report.getAverageEfficiencyPercent(),
                report.getVarianceSummary(),
                report.getGeneratedAt(),
                report.getReason(),
                report.getArchivedAt(),
                report.getCreatedBy(),
                report.getUpdatedBy(),
                report.getCreatedAt(),
                report.getUpdatedAt()
        );
    }

    AnalyticsHistoryResponse toResponse(AnalyticsHistory history) {
        return new AnalyticsHistoryResponse(
                history.getId(),
                history.getOwnerId(),
                history.getActorId(),
                history.getActionType(),
                history.getActualRecord() == null ? null : history.getActualRecord().getId(),
                history.getEvaluationResult() == null ? null : history.getEvaluationResult().getId(),
                history.getReport() == null ? null : history.getReport().getId(),
                history.getOldValue(),
                history.getNewValue(),
                history.getReason(),
                history.getOccurredAt()
        );
    }

    String toTagString(Set<UUID> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return null;
        }
        return tagIds.stream()
                .map(UUID::toString)
                .collect(Collectors.toCollection(TreeSet::new))
                .stream()
                .collect(Collectors.joining(","));
    }

    Set<UUID> toTagSet(String tagIds) {
        if (tagIds == null || tagIds.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(tagIds.split(","))
                .filter(value -> !value.isBlank())
                .map(String::trim)
                .map(UUID::fromString)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    String actualRecordSnapshot(ActualRecord record) {
        return "id=%s;taskId=%s;type=%s;status=%s;minutes=%s;cost=%s;currency=%s;date=%s"
                .formatted(
                        record.getId(),
                        record.getTaskId(),
                        record.getRecordType(),
                        record.getStatus(),
                        record.getActualMinutes(),
                        record.getActualCost(),
                        record.getCurrencyCode(),
                        record.getActualDate()
                );
    }

    String evaluationSnapshot(EvaluationResult result) {
        return "id=%s;taskId=%s;status=%s;minuteVariance=%s;costVariance=%s;efficiencyPercent=%s"
                .formatted(
                        result.getId(),
                        result.getTaskId(),
                        result.getStatus(),
                        result.getMinuteVariance(),
                        result.getCostVariance(),
                        result.getEfficiencyPercent()
                );
    }

    String reportSnapshot(AnalyticsReport report) {
        return "id=%s;type=%s;dimension=%s;status=%s;period=%s..%s;actualRecords=%s;minutes=%s;cost=%s"
                .formatted(
                        report.getId(),
                        report.getReportType(),
                        report.getDimension(),
                        report.getStatus(),
                        report.getPeriodStart(),
                        report.getPeriodEnd(),
                        report.getActualRecordCount(),
                        report.getTotalActualMinutes(),
                        report.getTotalActualCost()
                );
    }
}
