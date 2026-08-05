package com.lifebalance.task.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifebalance.task.dto.request.CreateTaskRequest;
import com.lifebalance.task.dto.response.TaskResponse;
import com.lifebalance.task.model.Task;
import com.lifebalance.task.model.enums.TaskStatus;
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

    @Override
    @Transactional
    public TaskResponse create(CreateTaskRequest request) {
        if (taskRepository.findByTaskName(request.getTaskName()).isPresent()) {
            throw new RuntimeException("Task name already exists");
        }
        Task task = Task.builder()
                .taskName(request.getTaskName())
                .description(request.getDescription())
                .priorityLevel(request.getPriorityLevel())
                .status(TaskStatus.TODO)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .dayOfWeek(request.getDayOfWeek())
                .note(request.getNote())
                .build();

        task = taskRepository.save(task);
        return mapToResponse(task);

    }

    @Transactional
    @Override
    public TaskResponse update(UUID id, UpdateTaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found "));
        taskRepository.findByTaskName(request.getTaskName())
                .ifPresent(exitingTask -> {
                    if (!exitingTask.getId().equals(id)) {
                        throw new RuntimeException("Task name already exists");
                    }
                });
        task.setTaskName(request.getTaskName());
        task.setDescription(request.getDescription());
        task.setPriorityLevel(request.getPriorityLevel());
        task.setStatus(request.getStatus());
        task.setStartDate(request.getStartDate());
        task.setEndDate(request.getEndDate());
        task.setStartTime(request.getStartTime());
        task.setEndTime(request.getEndTime());
        task.setDayOfWeek(request.getDayOfWeek());
        task.setNote(request.getNote());

        task = taskRepository.save(task);
        return mapToResponse(task);
    }

    @Override
    public Page<TaskResponse> search(
            String keyword,
            Pageable pageable) {

        return taskRepository
                .findByTaskNameContainingIgnoreCase(keyword, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    @Override
    public void archive(UUID id) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setStatus(TaskStatus.ARCHIVED);

        taskRepository.save(task);
    }

    @Transactional
    @Override
    public void restore(UUID id) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setStatus(TaskStatus.TODO);

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
                .taskName(source.getTaskName() + " (Copy)")
                .description(source.getDescription())
                .priorityLevel(source.getPriorityLevel())
                .status(TaskStatus.TODO)
                .startDate(source.getStartDate())
                .endDate(source.getEndDate())
                .startTime(source.getStartTime())
                .endTime(source.getEndTime())
                .dayOfWeek(source.getDayOfWeek())
                .note(source.getNote())
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
        response.setTaskName(task.getTaskName());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus());
        response.setPriorityLevel(task.getPriorityLevel());
        response.setStartDate(task.getStartDate());
        response.setEndDate(task.getEndDate());
        response.setStartTime(task.getStartTime());
        response.setEndTime(task.getEndTime());
        response.setDayOfWeek(task.getDayOfWeek());
        response.setNote(task.getNote());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());

        return response;
    }
}
