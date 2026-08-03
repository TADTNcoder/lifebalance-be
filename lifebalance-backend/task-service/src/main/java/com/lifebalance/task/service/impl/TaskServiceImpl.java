package com.lifebalance.task.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifebalance.task.dto.request.CreateTaskRequest;
import com.lifebalance.task.dto.response.TaskResponse;
import com.lifebalance.task.model.Task;
import com.lifebalance.task.model.enums.TaskStatus;
import com.lifebalance.task.repository.TaskRepository;
import com.lifebalance.task.service.TaskService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;

    @Override
    @Transactional
    public TaskResponse create(CreateTaskRequest request) {
        if (taskRepository.existsByTaskName(request.getTaskName())) {
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
