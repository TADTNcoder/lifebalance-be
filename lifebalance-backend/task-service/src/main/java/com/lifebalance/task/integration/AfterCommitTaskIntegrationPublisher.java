package com.lifebalance.task.integration;

import com.lifebalance.task.model.Task;
import com.lifebalance.task.model.TaskReminder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class AfterCommitTaskIntegrationPublisher implements TaskIntegrationPublisher {

    private static final Logger log = LoggerFactory.getLogger(AfterCommitTaskIntegrationPublisher.class);
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final String TASK_REFERENCE_TYPE = "TASK";
    private static final String TASK_REMINDER_REFERENCE_TYPE = "TASK_REMINDER";
    private static final String IN_APP_CHANNEL = "IN_APP";

    private final TaskIntegrationProperties properties;
    private final TaskIntegrationClient client;

    public AfterCommitTaskIntegrationPublisher(
            TaskIntegrationProperties properties,
            TaskIntegrationClient client
    ) {
        this.properties = properties;
        this.client = client;
    }

    @Override
    public void publishTaskChanged(Task task, UUID actorId, TaskIntegrationAction action, String reason) {
        if (task == null || task.getId() == null || task.getOwnerId() == null || !properties.isEnabled()) {
            return;
        }
        TaskIntegrationEvent event = TaskIntegrationEvent.from(
                task,
                actorId,
                action,
                reason,
                currentAuthorizationHeader()
        );
        afterCommit(() -> dispatchTaskChanged(event));
    }

    @Override
    public void publishMonthlyIncomeReady(Task task, UUID actorId, String reason) {
        if (task == null || task.getId() == null || task.getOwnerId() == null
                || !task.hasMonthlyIncomePlan() || !properties.isFinanceIncomeSyncEnabled()) {
            return;
        }
        TaskIntegrationEvent event = TaskIntegrationEvent.from(
                task,
                actorId,
                TaskIntegrationAction.TASK_COMPLETED,
                reason,
                currentAuthorizationHeader()
        );
        afterCommit(() -> dispatchMonthlyIncome(event));
    }

    @Override
    public void publishReminderChanged(
            TaskReminder reminder,
            UUID actorId,
            TaskIntegrationAction action,
            String reason
    ) {
        if (reminder == null
                || reminder.getId() == null
                || reminder.getOwnerId() == null
                || reminder.getTask() == null
                || (action != TaskIntegrationAction.TASK_REMINDER_CANCELLED
                        && !Boolean.TRUE.equals(reminder.getFeatureEnabled()))
                || !properties.isNotificationSyncEnabled()) {
            return;
        }
        TaskReminderIntegrationEvent event = TaskReminderIntegrationEvent.from(
                reminder,
                actorId,
                action,
                reason,
                currentAuthorizationHeader()
        );
        afterCommit(() -> dispatchReminderChanged(event));
    }

    private void dispatchTaskChanged(TaskIntegrationEvent event) {
        if (properties.isTimelineSyncEnabled()) {
            client.syncTimelineTask(event);
        }
        if (properties.isNotificationSyncEnabled()) {
            notificationForTask(event).forEach(request ->
                    client.createNotification(request, event.authorizationHeader()));
        }
        if (properties.isAnalyticsActualSeedEnabled()
                && event.action() == TaskIntegrationAction.TASK_COMPLETED) {
            actualSeedForCompletedTask(event).ifPresent(request ->
                    client.recordActualSeed(request, event.authorizationHeader()));
        }
    }

    private void dispatchReminderChanged(TaskReminderIntegrationEvent event) {
        client.createNotification(reminderNotification(event), event.authorizationHeader());
    }

    private void dispatchMonthlyIncome(TaskIntegrationEvent event) {
        monthlyIncomeRequest(event).ifPresent(request ->
                client.recordMonthlyIncome(request, event.authorizationHeader()));
    }

    private java.util.Optional<MonthlyIncomeTransactionRequest> monthlyIncomeRequest(
            TaskIntegrationEvent event) {

        if (event.monthlyIncomeAccountId() == null
                || event.monthlyIncomePeriod() == null
                || event.monthlyIncomePeriod().isBlank()
                || event.monthlyIncomeBase() == null
                || event.monthlyIncomeBase().signum() <= 0
                || event.monthlyIncomeCurrency() == null
                || event.monthlyIncomeCurrency().isBlank()) {
            return java.util.Optional.empty();
        }

        BigDecimal bonus = event.monthlyIncomeBonus() == null
                ? ZERO
                : event.monthlyIncomeBonus();
        BigDecimal deduction = event.monthlyIncomeDeduction() == null
                ? ZERO
                : event.monthlyIncomeDeduction();
        BigDecimal amount = event.monthlyIncomeBase()
                .add(bonus)
                .subtract(deduction);
        if (amount.signum() <= 0) {
            log.warn("Skipping monthly income settlement because net salary is not positive. taskId={} period={}",
                    event.taskId(), event.monthlyIncomePeriod());
            return java.util.Optional.empty();
        }

        String periodLabel = event.monthlyIncomePeriod().replace('-', '/');
        String taskTitle = event.title() == null || event.title().isBlank()
                ? "Công việc tháng"
                : event.title();
        OffsetDateTime transactionDate = event.completedAt() == null
                ? OffsetDateTime.now()
                : event.completedAt();
        return java.util.Optional.of(new MonthlyIncomeTransactionRequest(
                "INCOME",
                null,
                event.monthlyIncomeAccountId(),
                null,
                amount,
                event.monthlyIncomeCurrency().trim().toUpperCase(java.util.Locale.ROOT),
                transactionDate,
                ("Lương tháng " + periodLabel + " · " + taskTitle).substring(
                        0,
                        Math.min(255, ("Lương tháng " + periodLabel + " · " + taskTitle).length())),
                ("Ghi nhận lương tháng " + periodLabel
                        + " sau khi hoàn thành toàn bộ công việc: " + taskTitle).substring(
                                0,
                                Math.min(1000, ("Ghi nhận lương tháng " + periodLabel
                                        + " sau khi hoàn thành toàn bộ công việc: " + taskTitle).length())),
                event.monthlyIncomeGroupId(),
                null,
                null,
                "MONTHLY_SALARY",
                event.monthlyIncomePeriod(),
                event.monthlyIncomeBase(),
                bonus,
                deduction,
                event.reason() == null || event.reason().isBlank()
                        ? "Ghi nhận lương sau khi hoàn thành toàn bộ công việc trong tháng"
                        : event.reason()
        ));
    }

    private Set<TaskNotificationRequest> notificationForTask(TaskIntegrationEvent event) {
        return switch (event.action()) {
            case TASK_COMPLETED -> Set.of(new TaskNotificationRequest(
                    "ACTUAL_RECORDING_REMINDER",
                    Set.of(IN_APP_CHANNEL),
                    "NORMAL",
                    "Record actuals for completed task",
                    "Task \"" + event.title() + "\" is completed. Record actual time or cost to keep evaluation accurate.",
                    TASK_REFERENCE_TYPE,
                    event.taskId(),
                    "Prompt user to record actual values after task completion.",
                    true,
                    null,
                    event.reason()
            ));
            case TASK_CANCELLED, TASK_ARCHIVED, TASK_RESTORED, TASK_REOPENED -> Set.of(new TaskNotificationRequest(
                    "TIMELINE_CHANGE",
                    Set.of(IN_APP_CHANNEL),
                    "NORMAL",
                    "Task timeline changed",
                    "Task \"" + event.title() + "\" changed state to " + event.taskStatus() + ".",
                    TASK_REFERENCE_TYPE,
                    event.taskId(),
                    "Notify user about task lifecycle changes that can affect planning and timeline.",
                    true,
                    null,
                    event.reason()
            ));
            default -> Set.of();
        };
    }

    private TaskNotificationRequest reminderNotification(TaskReminderIntegrationEvent event) {
        String title = switch (event.action()) {
            case TASK_REMINDER_CANCELLED -> "Task reminder cancelled";
            case TASK_REMINDER_UPDATED -> "Task reminder updated";
            default -> "Task reminder scheduled";
        };
        String message = event.message() == null || event.message().isBlank()
                ? "Reminder for task \"" + event.taskTitle() + "\"."
                : event.message();
        return new TaskNotificationRequest(
                "TASK_REMINDER",
                Set.of(event.channel() == null ? IN_APP_CHANNEL : event.channel().name()),
                "NORMAL",
                title,
                message,
                TASK_REMINDER_REFERENCE_TYPE,
                event.reminderId(),
                "Notify user about task reminder scheduling.",
                true,
                event.action() == TaskIntegrationAction.TASK_REMINDER_CANCELLED ? null : event.remindAt(),
                event.reason()
        );
    }

    private java.util.Optional<TaskActualRecordRequest> actualSeedForCompletedTask(TaskIntegrationEvent event) {
        boolean hasMinutes = event.estimatedMinutes() != null && event.estimatedMinutes() > 0;
        boolean hasCost = event.estimatedCost() != null && event.estimatedCost().compareTo(ZERO) > 0;
        if (!hasMinutes && !hasCost) {
            return java.util.Optional.empty();
        }
        String recordType = hasMinutes && hasCost ? "TIME_AND_MONEY" : hasMinutes ? "TIME" : "MONEY";
        LocalDate actualDate = event.completedAt() == null
                ? LocalDate.now()
                : event.completedAt().toLocalDate();
        LocalDate periodStart = event.plannedStartAt() != null
                ? event.plannedStartAt().toLocalDate()
                : event.scheduledStartAt() != null
                        ? event.scheduledStartAt().toLocalDate()
                        : actualDate;
        LocalDate periodEnd = event.plannedEndAt() != null
                ? event.plannedEndAt().toLocalDate()
                : event.scheduledEndAt() != null
                        ? event.scheduledEndAt().toLocalDate()
                        : event.deadline() == null ? actualDate : event.deadline();
        if (periodStart.isAfter(periodEnd)) {
            periodStart = actualDate;
            periodEnd = actualDate;
        }
        return java.util.Optional.of(new TaskActualRecordRequest(
                recordType,
                event.taskId(),
                null,
                event.categoryId(),
                Set.of(),
                hasMinutes ? event.estimatedMinutes() : null,
                hasCost ? event.estimatedCost() : null,
                hasCost ? normalizedCurrencyCode() : null,
                actualDate,
                "Seeded from task completion. User should review actual values.",
                "TASK_COMPLETION_INTEGRATION",
                new TaskEvaluationBaselineRequest(
                        periodStart,
                        periodEnd,
                        hasMinutes ? event.estimatedMinutes() : null,
                        hasCost ? event.estimatedCost() : null,
                        hasCost ? normalizedCurrencyCode() : null
                )
        ));
    }

    private String normalizedCurrencyCode() {
        String configured = properties.getAnalyticsService().getDefaultCurrencyCode();
        if (configured == null || configured.isBlank()) {
            return "VND";
        }
        return configured.trim().toUpperCase();
    }

    private static void afterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }

    private static String currentAuthorizationHeader() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return null;
        }
        return servletRequestAttributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
    }
}
