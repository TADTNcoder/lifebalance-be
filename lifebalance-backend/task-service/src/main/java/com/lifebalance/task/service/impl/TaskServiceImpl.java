package com.lifebalance.task.service.impl;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifebalance.task.dto.request.CreateTaskRequest;
import com.lifebalance.task.dto.request.UpdateTaskRequest;
import com.lifebalance.task.dto.response.TaskResponse;
import com.lifebalance.task.error.TaskExceptions;
import com.lifebalance.task.model.Category;
import com.lifebalance.task.model.Task;
import com.lifebalance.task.model.enums.TaskStatus;
import com.lifebalance.task.repository.CategoryRepository;
import com.lifebalance.task.repository.TaskRepository;
import com.lifebalance.task.service.TaskService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;

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

        Task task = Task.builder()
                .ownerId(ownerId)
                .userId(ownerId)
                .name(request.getName())
                .description(request.getDescription())
                .priority(request.getPriority())
                .deadline(request.getDeadline())
                .estimatedMinutes(request.getEstimatedMinutes())
                .estimatedCost(request.getEstimatedCost())
                .category(category)
                .status(TaskStatus.DRAFT)
                .build();

        task = taskRepository.save(task);

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

        ensureNameAvailable(
                request.getName(),
                ownerId,
                id);

        Category category = resolveCategory(
                request.getCategoryId());

        task.updateDetails(
                request.getName(),
                request.getDescription(),
                request.getPriority(),
                request.getDeadline(),
                request.getEstimatedMinutes(),
                request.getEstimatedCost(),
                category);

        if (request.getProgress() != null) {
            task.updateProgress(
                    request.getProgress());
        }

        if (request.getStatus() != null) {
            task.transitionTo(
                    request.getStatus());
        }

        task = taskRepository.save(task);

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
    @Transactional
    public void archive(
            UUID id,
            UUID ownerId) {

        Task task = taskRepository
                .findByIdAndOwnerId(id, ownerId)
                .orElseThrow(TaskExceptions::taskNotFound);

        task.archive();

        taskRepository.save(task);
    }

    @Override
    @Transactional
    public void restore(
            UUID id,
            UUID ownerId) {

        Task task = taskRepository
                .findByIdAndOwnerId(id, ownerId)
                .orElseThrow(TaskExceptions::taskNotFound);

        task.restore();

        taskRepository.save(task);
    }

    @Override
    @Transactional
    public void delete(
            UUID id,
            UUID ownerId) {

        Task task = taskRepository
                .findByIdAndOwnerId(id, ownerId)
                .orElseThrow(TaskExceptions::taskNotFound);

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
                .build();

        copy = taskRepository.save(copy);

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

        response.setCreatedAt(
                task.getCreatedAt());

        response.setUpdatedAt(
                task.getUpdatedAt());

        return response;
    }
}
