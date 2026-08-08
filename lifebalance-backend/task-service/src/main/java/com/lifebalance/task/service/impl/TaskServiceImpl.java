package com.lifebalance.task.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifebalance.task.dto.request.CreateTaskRequest;
import com.lifebalance.task.dto.response.TaskResponse;
import com.lifebalance.task.model.Category;
import com.lifebalance.task.model.Task;
import com.lifebalance.task.model.enums.TaskStatus;
import com.lifebalance.task.repository.CategoryRepository;
import com.lifebalance.task.repository.TaskRepository;
import com.lifebalance.task.service.TaskService;
import java.util.UUID;
import com.lifebalance.task.dto.request.UpdateTaskRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public TaskResponse create(CreateTaskRequest request) {
        ensureNameAvailable(request.getName(), null);
        Category category = resolveCategory(request.getCategoryId());

        Task task = Task.builder()
                .userId(request.getUserId())
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

    @Transactional
    @Override
    public TaskResponse update(UUID id, UpdateTaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found "));

        ensureNameAvailable(request.getName(), id);
        Category category = resolveCategory(request.getCategoryId());

        task.updateDetails(
                request.getName(),
                request.getDescription(),
                request.getPriority(),
                request.getDeadline(),
                request.getEstimatedMinutes(),
                request.getEstimatedCost(),
                category
        );
        if (request.getProgress() != null) {
            task.updateProgress(request.getProgress());
        }
        task.transitionTo(request.getStatus());

        task = taskRepository.save(task);
        return mapToResponse(task);
    }

    @Override
    public Page<TaskResponse> search(
            String keyword,
            Pageable pageable) {

        return taskRepository
                .findByNameContainingIgnoreCase(keyword, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    @Override
    public void archive(UUID id) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.archive();

        taskRepository.save(task);
    }

    @Transactional
    @Override
    public void restore(UUID id) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.restore();

        taskRepository.save(task);
    }

    @Transactional
    @Override
    public void delete(UUID id) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        taskRepository.delete(task);
    }

    @Transactional
    @Override
    public TaskResponse duplicate(UUID id) {

        Task source = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        Task copy = Task.builder()
                .userId(source.getUserId())
                .name(source.getName() + " (Copy)")
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
    public TaskResponse getById(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        return mapToResponse(task);
    }

    private TaskResponse mapToResponse(Task task) {

        TaskResponse response = new TaskResponse();

        response.setId(task.getId());
        response.setUserId(task.getUserId());
        response.setName(task.getName());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus());
        response.setPriority(task.getPriority());
        response.setDeadline(task.getDeadline());
        response.setProgress(task.getProgress());
        response.setEstimatedMinutes(task.getEstimatedMinutes());
        response.setEstimatedCost(task.getEstimatedCost());
        if (task.getCategory() != null) {
            response.setCategoryId(task.getCategory().getId());
            response.setCategoryName(task.getCategory().getName());
        }
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());

        return response;
    }

    private void ensureNameAvailable(String name, UUID currentTaskId) {
        taskRepository.findByName(name)
                .ifPresent(existingTask -> {
                    if (currentTaskId == null || !existingTask.getId().equals(currentTaskId)) {
                        throw new RuntimeException("Task name already exists");
                    }
                });
    }

    private Category resolveCategory(UUID categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }
}
