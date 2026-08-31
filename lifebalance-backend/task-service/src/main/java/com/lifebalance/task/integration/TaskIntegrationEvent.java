package com.lifebalance.task.integration;

import com.lifebalance.task.model.Task;
import com.lifebalance.task.model.enums.TaskStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TaskIntegrationEvent(
        UUID ownerId,
        UUID actorId,
        UUID taskId,
        String title,
        TaskStatus taskStatus,
        Integer estimatedMinutes,
        BigDecimal estimatedCost,
        LocalDate deadline,
        UUID categoryId,
        OffsetDateTime plannedStartAt,
        OffsetDateTime plannedEndAt,
        OffsetDateTime scheduledStartAt,
        OffsetDateTime scheduledEndAt,
        OffsetDateTime completedAt,
        UUID monthlyIncomeGroupId,
        UUID monthlyIncomeAccountId,
        String monthlyIncomeCurrency,
        String monthlyIncomePeriod,
        BigDecimal monthlyIncomeBase,
        BigDecimal monthlyIncomeBonus,
        BigDecimal monthlyIncomeDeduction,
        TaskIntegrationAction action,
        String reason,
        String authorizationHeader
) {

    static TaskIntegrationEvent from(
            Task task,
            UUID actorId,
            TaskIntegrationAction action,
            String reason,
            String authorizationHeader
    ) {
        UUID categoryId = task.getCategory() == null ? null : task.getCategory().getId();
        return new TaskIntegrationEvent(
                task.getOwnerId(),
                actorId,
                task.getId(),
                task.getName(),
                task.getStatus(),
                task.getEstimatedMinutes(),
                task.getEstimatedCost(),
                task.getDeadline(),
                categoryId,
                task.getPlannedStartAt(),
                task.getPlannedEndAt(),
                task.getScheduledStartAt(),
                task.getScheduledEndAt(),
                task.getCompletedAt(),
                task.getMonthlyIncomeGroupId(),
                task.getMonthlyIncomeAccountId(),
                task.getMonthlyIncomeCurrency(),
                task.getMonthlyIncomePeriod(),
                task.getMonthlyIncomeBase(),
                task.getMonthlyIncomeBonus(),
                task.getMonthlyIncomeDeduction(),
                action,
                reason,
                authorizationHeader
        );
    }
}
