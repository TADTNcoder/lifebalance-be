package com.lifebalance.task.service.impl;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifebalance.task.dto.request.CreateTaskRequest;
import com.lifebalance.task.dto.request.UpdateTaskRequest;
import com.lifebalance.task.dto.response.TaskResponse;
import com.lifebalance.task.error.TaskExceptions;
import com.lifebalance.task.history.TaskChangeHistoryService;
import com.lifebalance.task.model.Category;
import com.lifebalance.task.model.Task;
import com.lifebalance.task.model.enums.PriorityLevel;
import com.lifebalance.task.model.enums.TaskHistoryActionType;
import com.lifebalance.task.model.enums.TaskStatus;
import com.lifebalance.task.repository.CategoryRepository;
import com.lifebalance.task.repository.TaskRepository;
import com.lifebalance.task.service.TaskService;
import com.lifebalance.task.validation.TaskLifecyclePolicy;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final TaskLifecyclePolicy taskLifecyclePolicy;
    private final TaskChangeHistoryService taskChangeHistoryService;

    @Override
    @Transactional
    public TaskResponse create(
            UUID ownerId,
            CreateTaskRequest request) {

        ensureNameAvailable(
                request.getName(),
                ownerId,
                null);

        Category category = resolveCategory(
                request.getCategoryId());

        validateOptionalPlanningWindow(
                request.getPlannedStartAt(),
                request.getPlannedEndAt());

        Task task = Task.builder()
                .ownerId(ownerId)
                .userId(ownerId)
                .name(request.getName())
                .description(request.getDescription())
                .priority(request.getPriority())
                .deadline(request.getDeadline())
                .plannedStartAt(request.getPlannedStartAt())
                .plannedEndAt(request.getPlannedEndAt())
                .estimatedMinutes(request.getEstimatedMinutes())
                .estimatedCost(request.getEstimatedCost())
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

        return mapToResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse update(
            UUID id,
            UUID ownerId,
            UpdateTaskRequest request) {

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

        ensureNameAvailable(
                request.getName(),
                ownerId,
                id);

        Category category = resolveCategory(
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
            task.planWindow(
                    request.getPlannedStartAt(),
                    request.getPlannedEndAt());
        }

        if (request.getProgress() != null) {
            if (isPlanningLocked(oldStatus)) {
                taskLifecyclePolicy.validatePlanningEditable(task);
            }
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

        Task task = taskRepository
                .findByIdAndOwnerId(id, ownerId)
                .orElseThrow(TaskExceptions::taskNotFound);

        TaskStatus oldStatus = task.getStatus();
        taskLifecyclePolicy.validateTransition(
                oldStatus,
                TaskStatus.ARCHIVED);
        task.archive();
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
    }

    @Override
    @Transactional
    public void restore(
            UUID id,
            UUID ownerId) {

        Task task = taskRepository
                .findByIdAndOwnerId(id, ownerId)
                .orElseThrow(TaskExceptions::taskNotFound);

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
    }

    @Override
    @Transactional
    public void delete(
            UUID id,
            UUID ownerId) {

        Task task = taskRepository
                .findByIdAndOwnerId(id, ownerId)
                .orElseThrow(TaskExceptions::taskNotFound);

        taskChangeHistoryService.recordTaskChange(
                task,
                ownerId,
                TaskHistoryActionType.TASK_DELETED,
                null,
                taskSnapshot(task),
                null,
                null);
        taskRepository.delete(task);
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

        ensureNameAvailable(
                copyName,
                ownerId,
                null);

        Task copy = Task.builder()
                .ownerId(ownerId)
                .userId(ownerId)
                .name(copyName)
                .description(source.getDescription())
                .priority(source.getPriority())
                .deadline(source.getDeadline())
                .estimatedMinutes(source.getEstimatedMinutes())
                .estimatedCost(source.getEstimatedCost())
                .category(source.getCategory())
                .status(TaskStatus.DRAFT)
                .createdBy(ownerId)
                .updatedBy(ownerId)
                .build();

        copy = taskRepository.save(copy);
        taskChangeHistoryService.recordTaskChange(
                copy,
                ownerId,
                TaskHistoryActionType.TASK_CREATED,
                null,
                null,
                taskSnapshot(copy),
                "Duplicated from task " + source.getId());

        return mapToResponse(copy);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getById(
            UUID id,
            UUID ownerId) {

        Task task = taskRepository
                .findByIdAndOwnerId(id, ownerId)
                .orElseThrow(TaskExceptions::taskNotFound);

        return mapToResponse(task);
    }

    private void ensureNameAvailable(
            String name,
            UUID ownerId,
            UUID currentTaskId) {

        taskRepository
                .findByNameAndOwnerId(name, ownerId)
                .ifPresent(existingTask -> {

                    if (currentTaskId == null
                            || !existingTask.getId()
                                    .equals(currentTaskId)) {

                        throw TaskExceptions.taskNameAlreadyExists();
                    }
                });
    }

    private Category resolveCategory(
            UUID categoryId) {

        if (categoryId == null) {
            return null;
        }

        return categoryRepository
                .findById(categoryId)
                .orElseThrow(TaskExceptions::categoryNotFound);
    }

    private boolean isPlanningChangeRequested(
            Task task,
            UpdateTaskRequest request) {

        UUID currentCategoryId = task.getCategory() == null
                ? null
                : task.getCategory().getId();
        return !Objects.equals(task.getName(), request.getName())
                || !Objects.equals(task.getDescription(), request.getDescription())
                || !Objects.equals(task.getPriority(), request.getPriority())
                || !Objects.equals(task.getDeadline(), request.getDeadline())
                || !Objects.equals(task.getEstimatedMinutes(), request.getEstimatedMinutes())
                || !Objects.equals(task.getEstimatedCost(), request.getEstimatedCost())
                || !Objects.equals(currentCategoryId, request.getCategoryId())
                || !Objects.equals(task.getPlannedStartAt(), request.getPlannedStartAt())
                || !Objects.equals(task.getPlannedEndAt(), request.getPlannedEndAt());
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
                + ";categoryId=" + categoryId;
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

        if (task.getCategory() != null) {

            response.setCategoryId(
                    task.getCategory().getId());

            response.setCategoryName(
                    task.getCategory().getName());
        }

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
