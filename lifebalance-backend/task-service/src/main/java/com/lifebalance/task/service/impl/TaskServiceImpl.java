package com.lifebalance.task.service.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifebalance.task.dto.request.CreateTaskRequest;
import com.lifebalance.task.dto.request.TaskLifecycleActionRequest;
import com.lifebalance.task.dto.request.TaskPlanningRequest;
import com.lifebalance.task.dto.request.UpdateTaskRequest;
import com.lifebalance.task.dto.request.UpdateTaskProgressRequest;
import com.lifebalance.task.dto.response.TaskResponse;
import com.lifebalance.task.error.TaskExceptions;
import com.lifebalance.task.history.TaskChangeHistoryService;
import com.lifebalance.task.integration.TaskIntegrationAction;
import com.lifebalance.task.integration.TaskIntegrationPublisher;
import com.lifebalance.task.model.Category;
import com.lifebalance.task.model.Task;
import com.lifebalance.task.model.TimelinePlacement;
import com.lifebalance.task.model.enums.PriorityLevel;
import com.lifebalance.task.model.enums.TaskHistoryActionType;
import com.lifebalance.task.model.enums.TaskStatus;
import com.lifebalance.task.model.enums.TimelinePlacementStatus;
import com.lifebalance.task.repository.CategoryRepository;
import com.lifebalance.task.repository.TaskRepository;
import com.lifebalance.task.repository.TimelinePlacementRepository;
import com.lifebalance.task.service.TaskService;
import com.lifebalance.task.validation.TaskLifecyclePolicy;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final TimelinePlacementRepository timelinePlacementRepository;
    private final TaskLifecyclePolicy taskLifecyclePolicy;
    private final TaskChangeHistoryService taskChangeHistoryService;
    private final TaskIntegrationPublisher taskIntegrationPublisher;

    @Override
    @Transactional
    public TaskResponse create(
            UUID ownerId,
            CreateTaskRequest request) {

        ensureNameAvailable(
                request.getName(),
                ownerId,
                null,
                TaskTimeKey.of(
                        request.getDeadline(),
                        request.getPlannedStartAt(),
                        request.getPlannedEndAt(),
                        null,
                        null));

        Category category = resolveCategory(
                ownerId,
                request.getCategoryId());

        validateOptionalPlanningWindow(
                request.getPlannedStartAt(),
                request.getPlannedEndAt());

        Task task = Task.builder()
                .ownerId(ownerId)
                .userId(ownerId)
                .name(request.getName())
                .description(request.getDescription())
                .note(request.getNote())
                .currency(request.getCurrency())
                .priority(request.getPriority())
                .deadline(request.getDeadline())
                .plannedStartAt(request.getPlannedStartAt())
                .plannedEndAt(request.getPlannedEndAt())
                .estimatedMinutes(request.getEstimatedMinutes())
                .estimatedCost(request.getEstimatedCost())
                .financeAccountId(request.getFinanceAccountId())
                .monthlyIncomeGroupId(request.getMonthlyIncomeGroupId())
                .monthlyIncomeAccountId(request.getMonthlyIncomeAccountId())
                .monthlyIncomeCurrency(request.getMonthlyIncomeCurrency())
                .monthlyIncomePeriod(request.getMonthlyIncomePeriod())
                .monthlyIncomeBase(request.getMonthlyIncomeBase())
                .monthlyIncomeBonus(request.getMonthlyIncomeBonus())
                .monthlyIncomeDeduction(request.getMonthlyIncomeDeduction())
                .category(category)
                .status(TaskStatus.DRAFT)
                .createdBy(ownerId)
                .updatedBy(ownerId)
                .build();

        task = taskRepository.save(task);
        taskChangeHistoryService.recordTaskChange(
                task,
                ownerId,
                TaskHistoryActionType.TASK_CREATED,
                null,
                null,
                taskSnapshot(task),
                null);
        taskIntegrationPublisher.publishTaskChanged(
                task,
                ownerId,
                TaskIntegrationAction.TASK_CREATED,
                null);

        return mapToResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse update(
            UUID id,
            UUID ownerId,
            UpdateTaskRequest request) {

        lockMonthlyIncomeGroup(id, ownerId);
        if (request.getStatus() == TaskStatus.COMPLETED
                && request.getMonthlyIncomeGroupId() != null) {
            taskRepository.lockMonthlyIncomeGroup(ownerId, request.getMonthlyIncomeGroupId());
        }
        Task task = taskRepository
                .findByIdAndOwnerId(id, ownerId)
                .orElseThrow(TaskExceptions::taskNotFound);

        TaskStatus oldStatus = task.getStatus();
        String oldSnapshot = taskSnapshot(task);
        if (request.getStatus() != null) {
            taskLifecyclePolicy.validateTransition(
                    oldStatus,
                    request.getStatus());
        }
        boolean planningChangeRequested = isPlanningChangeRequested(
                task,
                request);
        if (planningChangeRequested) {
            taskLifecyclePolicy.validatePlanningEditable(task);
        }

        TaskTimeKey candidateTime = isPlanningLocked(oldStatus)
                ? TaskTimeKey.from(task)
                : TaskTimeKey.of(
                        request.getDeadline(),
                        request.getPlannedStartAt(),
                        request.getPlannedEndAt(),
                        task.getScheduledStartAt(),
                        task.getScheduledEndAt());
        ensureNameAvailable(
                request.getName(),
                ownerId,
                id,
                candidateTime);

        Category category = resolveCategory(
                ownerId,
                request.getCategoryId());

        if (!isPlanningLocked(oldStatus)) {
            validateOptionalPlanningWindow(
                    request.getPlannedStartAt(),
                    request.getPlannedEndAt());
            task.updateDetails(
                    request.getName(),
                    request.getDescription(),
                    request.getPriority(),
                    request.getDeadline(),
                    request.getEstimatedMinutes(),
                    request.getEstimatedCost(),
                    category);
            if (request.getNote() != null) {
                task.setNote(request.getNote());
            }
            if (request.getCurrency() != null) {
                task.setCurrency(request.getCurrency());
            }
            task.setFinanceAccountId(request.getFinanceAccountId());
            applyMonthlyIncomeFields(
                    task,
                    request.getMonthlyIncomeGroupId(),
                    request.getMonthlyIncomeAccountId(),
                    request.getMonthlyIncomeCurrency(),
                    request.getMonthlyIncomePeriod(),
                    request.getMonthlyIncomeBase(),
                    request.getMonthlyIncomeBonus(),
                    request.getMonthlyIncomeDeduction());
            task.planWindow(
                    request.getPlannedStartAt(),
                    request.getPlannedEndAt());
        } else {
            // Completed/cancelled/archived tasks keep their planning immutable, but
            // core descriptive fields (name, description and note) remain editable.
            task.setName(request.getName());
            task.setDescription(request.getDescription());
            if (request.getNote() != null) {
                task.setNote(request.getNote());
            }
            if (request.getCurrency() != null) {
                task.setCurrency(request.getCurrency());
            }
        }

        if (request.getProgress() != null) {
            TaskStatus progressStatus = request.getStatus() == null
                    ? oldStatus
                    : request.getStatus();
            taskLifecyclePolicy.validateProgressEditable(progressStatus);
            task.updateProgress(
                    request.getProgress());
        }

        if (request.getStatus() != null) {
            task.transitionTo(
                    request.getStatus());
        }

        task.setUpdatedBy(ownerId);
        task = taskRepository.save(task);
        String newSnapshot = taskSnapshot(task);
        if (!Objects.equals(oldSnapshot, newSnapshot)) {
            taskChangeHistoryService.recordTaskChange(
                    task,
                    ownerId,
                    TaskHistoryActionType.TASK_UPDATED,
                    null,
                    oldSnapshot,
                    newSnapshot,
                    null);
        }
        if (oldStatus != task.getStatus()) {
            taskChangeHistoryService.recordTaskChange(
                    task,
                    ownerId,
                    TaskHistoryActionType.TASK_STATUS_CHANGED,
                    "status",
                    String.valueOf(oldStatus),
                    String.valueOf(task.getStatus()),
                    null);
        }
        if (!Objects.equals(oldSnapshot, newSnapshot) || oldStatus != task.getStatus()) {
            taskIntegrationPublisher.publishTaskChanged(
                    task,
                    ownerId,
                    integrationActionForStatusChange(oldStatus, task.getStatus()),
                    null);
        }

        return mapToResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse plan(
            UUID id,
            UUID ownerId,
            TaskPlanningRequest request) {

        Task task = findTask(id, ownerId);
        TaskStatus oldStatus = task.getStatus();
        String oldSnapshot = taskSnapshot(task);

        taskLifecyclePolicy.validatePlanningEditable(task);
        taskLifecyclePolicy.validateTransition(
                oldStatus,
                TaskStatus.PLANNED);

        validateOptionalPlanningWindow(
                request.getPlannedStartAt(),
                request.getPlannedEndAt());

        PriorityLevel priority = request.getPriority() == null
                ? task.getPriority()
                : request.getPriority();
        LocalDate deadline = request.getDeadline() == null
                ? task.getDeadline()
                : request.getDeadline();
        Integer estimatedMinutes = request.getEstimatedMinutes() == null
                ? task.getEstimatedMinutes()
                : request.getEstimatedMinutes();
        BigDecimal estimatedCost = request.getEstimatedCost() == null
                ? task.getEstimatedCost()
                : request.getEstimatedCost();
        Category category = request.getCategoryId() == null
                ? task.getCategory()
                : resolveCategory(ownerId, request.getCategoryId());

        ensureNameAvailable(
                task.getName(),
                ownerId,
                id,
                TaskTimeKey.of(
                        deadline,
                        request.getPlannedStartAt(),
                        request.getPlannedEndAt(),
                        task.getScheduledStartAt(),
                        task.getScheduledEndAt()));

        task.updateDetails(
                task.getName(),
                task.getDescription(),
                priority,
                deadline,
                estimatedMinutes,
                estimatedCost,
                category);
        if (request.getCurrency() != null) {
            task.setCurrency(request.getCurrency());
        }
        if (request.hasFinanceAccountId()) {
            task.setFinanceAccountId(request.getFinanceAccountId());
        }
        applyMonthlyIncomeFields(
                task,
                request.getMonthlyIncomeGroupId(),
                request.getMonthlyIncomeAccountId(),
                request.getMonthlyIncomeCurrency(),
                request.getMonthlyIncomePeriod(),
                request.getMonthlyIncomeBase(),
                request.getMonthlyIncomeBonus(),
                request.getMonthlyIncomeDeduction());
        task.planWindow(
                request.getPlannedStartAt(),
                request.getPlannedEndAt());
        taskLifecyclePolicy.validatePlanReady(task);
        task.transitionTo(TaskStatus.PLANNED);
        task.setUpdatedBy(ownerId);

        task = taskRepository.save(task);
        String newSnapshot = taskSnapshot(task);
        taskChangeHistoryService.recordTaskChange(
                task,
                ownerId,
                TaskHistoryActionType.TASK_PLANNED,
                null,
                oldSnapshot,
                newSnapshot,
                request.getReason());
        recordStatusChangeIfNeeded(
                task,
                ownerId,
                oldStatus,
                request.getReason());
        taskIntegrationPublisher.publishTaskChanged(
                task,
                ownerId,
                TaskIntegrationAction.TASK_PLANNED,
                request.getReason());

        return mapToResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse updateProgress(
            UUID id,
            UUID ownerId,
            UpdateTaskProgressRequest request) {

        Task task = findTask(id, ownerId);
        taskLifecyclePolicy.validateProgressEditable(task);

        Integer oldProgress = task.getProgress();
        task.updateProgress(request.getProgress());
        task.setUpdatedBy(ownerId);
        task = taskRepository.save(task);

        if (!Objects.equals(oldProgress, task.getProgress())) {
            taskChangeHistoryService.recordTaskChange(
                    task,
                    ownerId,
                    TaskHistoryActionType.TASK_PROGRESS_UPDATED,
                    "progress",
                    String.valueOf(oldProgress),
                    String.valueOf(task.getProgress()),
                    request.getReason());
            taskIntegrationPublisher.publishTaskChanged(
                    task,
                    ownerId,
                    TaskIntegrationAction.TASK_PROGRESS_UPDATED,
                    request.getReason());
        }

        return mapToResponse(task);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponse> search(
            UUID ownerId,
            String keyword,
            Pageable pageable) {

        return taskRepository
                .findByOwnerIdAndNameContainingIgnoreCase(
                        ownerId,
                        keyword,
                        pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponse> search(
            UUID ownerId,
            String keyword,
            TaskStatus status,
            PriorityLevel priority,
            UUID categoryId,
            LocalDate deadlineFrom,
            LocalDate deadlineTo,
            Pageable pageable) {

        return taskRepository
                .searchByOwnerAndFilters(
                        ownerId,
                        keyword,
                        status,
                        priority,
                        categoryId,
                        deadlineFrom,
                        deadlineTo,
                        pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public void archive(
            UUID id,
            UUID ownerId) {

        Task task = findTask(id, ownerId);

        TaskStatus oldStatus = task.getStatus();
        taskLifecyclePolicy.validateTransition(
                oldStatus,
                TaskStatus.ARCHIVED);
        cancelActiveTimelinePlacements(
                task,
                ownerId,
                "Task archived");
        task.archive();
        task.setScheduledWindow(null, null);
        task.setUpdatedBy(ownerId);

        taskRepository.save(task);
        taskChangeHistoryService.recordTaskChange(
                task,
                ownerId,
                TaskHistoryActionType.TASK_ARCHIVED,
                "status",
                String.valueOf(oldStatus),
                String.valueOf(task.getStatus()),
                null);
        taskIntegrationPublisher.publishTaskChanged(
                task,
                ownerId,
                TaskIntegrationAction.TASK_ARCHIVED,
                null);
    }

    @Override
    @Transactional
    public void restore(
            UUID id,
            UUID ownerId) {

        Task task = findTask(id, ownerId);

        TaskStatus oldStatus = task.getStatus();
        taskLifecyclePolicy.validateTransition(
                oldStatus,
                TaskStatus.PLANNED);
        task.restore();
        task.setUpdatedBy(ownerId);

        taskRepository.save(task);
        taskChangeHistoryService.recordTaskChange(
                task,
                ownerId,
                TaskHistoryActionType.TASK_RESTORED,
                "status",
                String.valueOf(oldStatus),
                String.valueOf(task.getStatus()),
                null);
        taskIntegrationPublisher.publishTaskChanged(
                task,
                ownerId,
                TaskIntegrationAction.TASK_RESTORED,
                null);
    }

    @Override
    @Transactional
    public TaskResponse pause(
            UUID id,
            UUID ownerId,
            TaskLifecycleActionRequest request) {

        return transitionLifecycle(
                id,
                ownerId,
                TaskStatus.ON_HOLD,
                reasonOf(request),
                TaskIntegrationAction.TASK_PAUSED);
    }

    @Override
    @Transactional
    public TaskResponse resume(
            UUID id,
            UUID ownerId,
            TaskLifecycleActionRequest request) {

        return transitionLifecycle(
                id,
                ownerId,
                TaskStatus.IN_PROGRESS,
                reasonOf(request),
                TaskIntegrationAction.TASK_RESUMED);
    }

    @Override
    @Transactional
    public TaskResponse complete(
            UUID id,
            UUID ownerId,
            TaskLifecycleActionRequest request) {

        Task task = findTask(id, ownerId);
        TaskStatus oldStatus = task.getStatus();
        String reason = reasonOf(request);
        Integer oldProgress = task.getProgress();

        taskLifecyclePolicy.validateTransition(
                oldStatus,
                TaskStatus.COMPLETED);
        task.updateProgress(100);
        task.markAsCompleted();
        task.setUpdatedBy(ownerId);

        task = taskRepository.save(task);
        if (!Objects.equals(oldProgress, task.getProgress())) {
            taskChangeHistoryService.recordTaskChange(
                    task,
                    ownerId,
                    TaskHistoryActionType.TASK_PROGRESS_UPDATED,
                    "progress",
                    String.valueOf(oldProgress),
                    String.valueOf(task.getProgress()),
                    reason);
        }
        recordStatusChangeIfNeeded(
                task,
                ownerId,
                oldStatus,
                reason);
        taskIntegrationPublisher.publishTaskChanged(
                task,
                ownerId,
                TaskIntegrationAction.TASK_COMPLETED,
                reason);
        return mapToResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse cancel(
            UUID id,
            UUID ownerId,
            TaskLifecycleActionRequest request) {

        Task task = findTask(id, ownerId);
        TaskStatus oldStatus = task.getStatus();
        String reason = reasonOf(request);

        taskLifecyclePolicy.validateTransition(
                oldStatus,
                TaskStatus.CANCELLED);
        cancelActiveTimelinePlacements(
                task,
                ownerId,
                reason);
        task.cancel();
        task.setScheduledWindow(null, null);
        task.setUpdatedBy(ownerId);

        task = taskRepository.save(task);
        recordStatusChangeIfNeeded(
                task,
                ownerId,
                oldStatus,
                reason);
        taskIntegrationPublisher.publishTaskChanged(
                task,
                ownerId,
                TaskIntegrationAction.TASK_CANCELLED,
                reason);

        return mapToResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse reopen(
            UUID id,
            UUID ownerId,
            TaskLifecycleActionRequest request) {

        return transitionLifecycle(
                id,
                ownerId,
                TaskStatus.PLANNED,
                reasonOf(request),
                TaskIntegrationAction.TASK_REOPENED);
    }

    @Override
    @Transactional
    public void delete(
            UUID id,
            UUID ownerId) {

        Task task = taskRepository
                .findByIdAndOwnerId(id, ownerId)
                .orElseThrow(TaskExceptions::taskNotFound);

        taskLifecyclePolicy.validateDeleteAllowed(task);
        softDelete(task, ownerId, null);
    }

    @Override
    @Transactional
    public void deleteFinanceLinkedTask(
            UUID id,
            UUID ownerId) {

        Task task = taskRepository
                .findByIdAndOwnerId(id, ownerId)
                .orElseThrow(TaskExceptions::taskNotFound);

        String reason = "Linked finance transaction was voided";
        cancelActiveTimelinePlacements(task, ownerId, reason);
        softDelete(task, ownerId, reason);
    }

    private void softDelete(
            Task task,
            UUID ownerId,
            String reason) {

        taskChangeHistoryService.recordTaskChange(
                task,
                ownerId,
                TaskHistoryActionType.TASK_DELETED,
                null,
                taskSnapshot(task),
                null,
                reason);
        taskIntegrationPublisher.publishTaskChanged(
                task,
                ownerId,
                TaskIntegrationAction.TASK_DELETED,
                reason);
        OffsetDateTime deletedAt = OffsetDateTime.now();
        task.setDeletedAt(deletedAt);
        task.setUpdatedAt(deletedAt);
        taskRepository.save(task);
    }

    @Override
    @Transactional
    public TaskResponse duplicate(
            UUID id,
            UUID ownerId) {

        Task source = taskRepository
                .findByIdAndOwnerId(id, ownerId)
                .orElseThrow(TaskExceptions::taskNotFound);

        String copyName = source.getName() + " (Copy)";

        Task copy = Task.builder()
                .ownerId(ownerId)
                .userId(ownerId)
                .name(copyName)
                .description(source.getDescription())
                .note(source.getNote())
                .currency(source.getCurrency())
                .priority(source.getPriority())
                .deadline(source.getDeadline())
                .estimatedMinutes(source.getEstimatedMinutes())
                .estimatedCost(source.getEstimatedCost())
                .financeAccountId(source.getFinanceAccountId())
                .category(source.getCategory())
                .status(TaskStatus.DRAFT)
                .createdBy(ownerId)
                .updatedBy(ownerId)
                .build();

        ensureNameAvailable(
                copyName,
                ownerId,
                null,
                TaskTimeKey.from(copy));

        copy = taskRepository.save(copy);
        taskChangeHistoryService.recordTaskChange(
                copy,
                ownerId,
                TaskHistoryActionType.TASK_CREATED,
                null,
                null,
                taskSnapshot(copy),
                "Duplicated from task " + source.getId());
        taskIntegrationPublisher.publishTaskChanged(
                copy,
                ownerId,
                TaskIntegrationAction.TASK_DUPLICATED,
                "Duplicated from task " + source.getId());

        return mapToResponse(copy);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getById(
            UUID id,
            UUID ownerId) {

        Task task = findTask(id, ownerId);

        return mapToResponse(task);
    }

    private TaskResponse transitionLifecycle(
            UUID id,
            UUID ownerId,
            TaskStatus targetStatus,
            String reason,
            TaskIntegrationAction integrationAction) {

        Task task = findTask(id, ownerId);
        TaskStatus oldStatus = task.getStatus();

        taskLifecyclePolicy.validateTransition(
                oldStatus,
                targetStatus);
        task.transitionTo(targetStatus);
        task.setUpdatedBy(ownerId);

        task = taskRepository.save(task);
        recordStatusChangeIfNeeded(
                task,
                ownerId,
                oldStatus,
                reason);
        taskIntegrationPublisher.publishTaskChanged(
                task,
                ownerId,
                integrationAction,
                reason);

        return mapToResponse(task);
    }

    private TaskIntegrationAction integrationActionForStatusChange(
            TaskStatus oldStatus,
            TaskStatus newStatus) {

        if (oldStatus == newStatus) {
            return TaskIntegrationAction.TASK_UPDATED;
        }
        return switch (newStatus) {
            case COMPLETED -> TaskIntegrationAction.TASK_COMPLETED;
            case CANCELLED -> TaskIntegrationAction.TASK_CANCELLED;
            case ARCHIVED -> TaskIntegrationAction.TASK_ARCHIVED;
            case ON_HOLD -> TaskIntegrationAction.TASK_PAUSED;
            case IN_PROGRESS -> TaskIntegrationAction.TASK_RESUMED;
            case PLANNED -> oldStatus == TaskStatus.COMPLETED
                    || oldStatus == TaskStatus.CANCELLED
                    || oldStatus == TaskStatus.ARCHIVED
                    ? TaskIntegrationAction.TASK_REOPENED
                    : TaskIntegrationAction.TASK_UPDATED;
            default -> TaskIntegrationAction.TASK_UPDATED;
        };
    }

    private Task findTask(
            UUID id,
            UUID ownerId) {

        return taskRepository
                .findByIdAndOwnerId(id, ownerId)
                .orElseThrow(TaskExceptions::taskNotFound);
    }

    private void cancelActiveTimelinePlacements(
            Task task,
            UUID ownerId,
            String reason) {

        List<TimelinePlacement> activePlacements = timelinePlacementRepository
                .findByOwnerIdAndTaskIdAndStatus(
                        ownerId,
                        task.getId(),
                        TimelinePlacementStatus.ACTIVE);
        if (activePlacements == null || activePlacements.isEmpty()) {
            return;
        }

        for (TimelinePlacement placement : activePlacements) {
            String oldSnapshot = timelineSnapshot(placement);
            placement.cancel(reason, ownerId);
            timelinePlacementRepository.save(placement);
            taskChangeHistoryService.recordTimelineChange(
                    task,
                    placement,
                    ownerId,
                    TaskHistoryActionType.TIMELINE_CANCELLED,
                    oldSnapshot,
                    timelineSnapshot(placement),
                    reason);
        }
    }

    private void recordStatusChangeIfNeeded(
            Task task,
            UUID ownerId,
            TaskStatus oldStatus,
            String reason) {

        if (oldStatus == task.getStatus()) {
            return;
        }

        taskChangeHistoryService.recordTaskChange(
                task,
                ownerId,
                TaskHistoryActionType.TASK_STATUS_CHANGED,
                "status",
                String.valueOf(oldStatus),
                String.valueOf(task.getStatus()),
                reason);
    }

    private String reasonOf(TaskLifecycleActionRequest request) {
        return request == null
                ? null
                : request.getReason();
    }

    private void ensureNameAvailable(
            String name,
            UUID ownerId,
            UUID currentTaskId,
            TaskTimeKey candidateTime) {

        List<Task> tasksWithSameName = taskRepository
                .findAllByNameAndOwnerId(name, ownerId);

        for (Task existingTask : tasksWithSameName) {
            if (currentTaskId != null
                    && Objects.equals(existingTask.getId(), currentTaskId)) {
                continue;
            }

            if (candidateTime.equals(TaskTimeKey.from(existingTask))) {
                throw TaskExceptions.taskNameAlreadyExists();
            }
        }
    }

    /**
     * Time identity used by the task-name uniqueness rule. A duplicate name is
     * valid when any of the deadline, planned window, or scheduled window is
     * different. Null values are compared intentionally, so two untimed tasks
     * with the same name still conflict.
     */
    private record TaskTimeKey(
            LocalDate deadline,
            Instant plannedStartAt,
            Instant plannedEndAt,
            Instant scheduledStartAt,
            Instant scheduledEndAt) {

        private static TaskTimeKey of(
                LocalDate deadline,
                OffsetDateTime plannedStartAt,
                OffsetDateTime plannedEndAt,
                OffsetDateTime scheduledStartAt,
                OffsetDateTime scheduledEndAt) {
            return new TaskTimeKey(
                    deadline,
                    toInstant(plannedStartAt),
                    toInstant(plannedEndAt),
                    toInstant(scheduledStartAt),
                    toInstant(scheduledEndAt));
        }

        private static TaskTimeKey from(Task task) {
            return of(
                    task.getDeadline(),
                    task.getPlannedStartAt(),
                    task.getPlannedEndAt(),
                    task.getScheduledStartAt(),
                    task.getScheduledEndAt());
        }

        private static Instant toInstant(OffsetDateTime value) {
            return value == null ? null : value.toInstant();
        }
    }

    private Category resolveCategory(
            UUID ownerId,
            UUID categoryId) {

        if (categoryId == null) {
            return null;
        }

        return categoryRepository
                .findVisibleByIdAndOwnerId(categoryId, ownerId)
                .orElseThrow(TaskExceptions::categoryNotFound);
    }

    private boolean isPlanningChangeRequested(
            Task task,
            UpdateTaskRequest request) {

        UUID currentCategoryId = task.getCategory() == null
                ? null
                : task.getCategory().getId();
        return !Objects.equals(task.getPriority(), request.getPriority())
                || !Objects.equals(task.getDeadline(), request.getDeadline())
                || !Objects.equals(task.getEstimatedMinutes(), request.getEstimatedMinutes())
                || !Objects.equals(task.getEstimatedCost(), request.getEstimatedCost())
                || !Objects.equals(task.getFinanceAccountId(), request.getFinanceAccountId())
                || !Objects.equals(currentCategoryId, request.getCategoryId())
                || !Objects.equals(task.getPlannedStartAt(), request.getPlannedStartAt())
                || !Objects.equals(task.getPlannedEndAt(), request.getPlannedEndAt());
    }

    private void applyMonthlyIncomeFields(
            Task task,
            UUID groupId,
            UUID accountId,
            String currency,
            String period,
            BigDecimal base,
            BigDecimal bonus,
            BigDecimal deduction) {

        // Null means that the caller is updating an unrelated task area. Do
        // not erase an existing salary plan during a normal task edit.
        if (groupId == null) {
            return;
        }
        task.setMonthlyIncomeGroupId(groupId);
        task.setMonthlyIncomeAccountId(accountId);
        task.setMonthlyIncomeCurrency(currency);
        task.setMonthlyIncomePeriod(period);
        task.setMonthlyIncomeBase(base);
        task.setMonthlyIncomeBonus(bonus);
        task.setMonthlyIncomeDeduction(deduction);
    }

    private void lockMonthlyIncomeGroup(UUID taskId, UUID ownerId) {
        taskRepository.lockMonthlyIncomeGroupForTask(taskId, ownerId);
    }

    private void validateOptionalPlanningWindow(
            java.time.OffsetDateTime plannedStartAt,
            java.time.OffsetDateTime plannedEndAt) {

        if (plannedStartAt != null || plannedEndAt != null) {
            taskLifecyclePolicy.validateTimelineWindow(
                    plannedStartAt,
                    plannedEndAt);
        }
    }

    private boolean isPlanningLocked(TaskStatus status) {
        return status == TaskStatus.COMPLETED
                || status == TaskStatus.CANCELLED
                || status == TaskStatus.ARCHIVED;
    }

    private String taskSnapshot(Task task) {
        UUID categoryId = task.getCategory() == null
                ? null
                : task.getCategory().getId();
        return "name=" + task.getName()
                + ";note=" + task.getNote()
                + ";currency=" + task.getCurrency()
                + ";status=" + task.getStatus()
                + ";priority=" + task.getPriority()
                + ";deadline=" + task.getDeadline()
                + ";plannedStartAt=" + task.getPlannedStartAt()
                + ";plannedEndAt=" + task.getPlannedEndAt()
                + ";scheduledStartAt=" + task.getScheduledStartAt()
                + ";scheduledEndAt=" + task.getScheduledEndAt()
                + ";progress=" + task.getProgress()
                + ";estimatedMinutes=" + task.getEstimatedMinutes()
                + ";estimatedCost=" + task.getEstimatedCost()
                + ";financeAccountId=" + task.getFinanceAccountId()
                + ";monthlyIncomeGroupId=" + task.getMonthlyIncomeGroupId()
                + ";monthlyIncomeAccountId=" + task.getMonthlyIncomeAccountId()
                + ";monthlyIncomeCurrency=" + task.getMonthlyIncomeCurrency()
                + ";monthlyIncomePeriod=" + task.getMonthlyIncomePeriod()
                + ";monthlyIncomeBase=" + task.getMonthlyIncomeBase()
                + ";monthlyIncomeBonus=" + task.getMonthlyIncomeBonus()
                + ";monthlyIncomeDeduction=" + task.getMonthlyIncomeDeduction()
                + ";categoryId=" + categoryId;
    }

    private String timelineSnapshot(TimelinePlacement placement) {
        return "placementId=" + placement.getId()
                + ";status=" + placement.getStatus()
                + ";startAt=" + placement.getStartAt()
                + ";endAt=" + placement.getEndAt()
                + ";timezone=" + placement.getTimezone()
                + ";source=" + placement.getSource();
    }

    private TaskResponse mapToResponse(
            Task task) {

        TaskResponse response = new TaskResponse();

        response.setId(task.getId());
        response.setOwnerId(task.getOwnerId());
        response.setUserId(task.getUserId());
        response.setName(task.getName());
        response.setDescription(
                task.getDescription());
        response.setNote(
                task.getNote());
        response.setCurrency(
                task.getCurrency());
        response.setStatus(task.getStatus());
        response.setPriority(
                task.getPriority());
        response.setDeadline(
                task.getDeadline());
        response.setPlannedStartAt(
                task.getPlannedStartAt());
        response.setPlannedEndAt(
                task.getPlannedEndAt());
        response.setScheduledStartAt(
                task.getScheduledStartAt());
        response.setScheduledEndAt(
                task.getScheduledEndAt());
        response.setCompletedAt(
                task.getCompletedAt());
        response.setCancelledAt(
                task.getCancelledAt());
        response.setArchivedAt(
                task.getArchivedAt());
        response.setProgress(
                task.getProgress());
        response.setEstimatedMinutes(
                task.getEstimatedMinutes());
        response.setEstimatedCost(
                task.getEstimatedCost());
        response.setFinanceAccountId(
                task.getFinanceAccountId());
        response.setMonthlyIncomeGroupId(
                task.getMonthlyIncomeGroupId());
        response.setMonthlyIncomeAccountId(
                task.getMonthlyIncomeAccountId());
        response.setMonthlyIncomeCurrency(
                task.getMonthlyIncomeCurrency());
        response.setMonthlyIncomePeriod(
                task.getMonthlyIncomePeriod());
        response.setMonthlyIncomeBase(
                task.getMonthlyIncomeBase());
        response.setMonthlyIncomeBonus(
                task.getMonthlyIncomeBonus());
        response.setMonthlyIncomeDeduction(
                task.getMonthlyIncomeDeduction());

        if (task.getCategory() != null) {

            response.setCategoryId(
                    task.getCategory().getId());

            response.setCategoryName(
                    task.getCategory().getName());
        }

        Set<UUID> tagIds = task.getTaskTags().stream()
                .map(taskTag -> taskTag.getId() == null ? null : taskTag.getId().getTagId())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        response.setTagIds(tagIds);

        response.setCreatedBy(
                task.getCreatedBy());
        response.setUpdatedBy(
                task.getUpdatedBy());
        response.setCreatedAt(
                task.getCreatedAt());

        response.setUpdatedAt(
                task.getUpdatedAt());

        return response;
    }
}
