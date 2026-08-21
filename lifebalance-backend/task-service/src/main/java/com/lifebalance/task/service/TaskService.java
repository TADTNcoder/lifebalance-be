package com.lifebalance.task.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.lifebalance.task.dto.request.CreateTaskRequest;
import com.lifebalance.task.dto.request.TaskLifecycleActionRequest;
import com.lifebalance.task.dto.request.TaskPlanningRequest;
import com.lifebalance.task.dto.request.UpdateTaskRequest;
import com.lifebalance.task.dto.request.UpdateTaskProgressRequest;
import com.lifebalance.task.dto.response.TaskResponse;
import com.lifebalance.task.model.enums.PriorityLevel;
import com.lifebalance.task.model.enums.TaskStatus;

public interface TaskService {

        TaskResponse create(
                        UUID ownerId,
                        CreateTaskRequest request);

        TaskResponse update(
                        UUID id,
                        UUID ownerId,
                        UpdateTaskRequest request);

        TaskResponse plan(
                        UUID id,
                        UUID ownerId,
                        TaskPlanningRequest request);

        TaskResponse updateProgress(
                        UUID id,
                        UUID ownerId,
                        UpdateTaskProgressRequest request);

        Page<TaskResponse> search(
                        UUID ownerId,
                        String keyword,
                        Pageable pageable);

        Page<TaskResponse> search(
                        UUID ownerId,
                        String keyword,
                        TaskStatus status,
                        PriorityLevel priority,
                        UUID categoryId,
                        LocalDate deadlineFrom,
                        LocalDate deadlineTo,
                        Pageable pageable);

        void archive(
                        UUID id,
                        UUID ownerId);

        void restore(
                        UUID id,
                        UUID ownerId);

        TaskResponse pause(
                        UUID id,
                        UUID ownerId,
                        TaskLifecycleActionRequest request);

        TaskResponse resume(
                        UUID id,
                        UUID ownerId,
                        TaskLifecycleActionRequest request);

        TaskResponse complete(
                        UUID id,
                        UUID ownerId,
                        TaskLifecycleActionRequest request);

        TaskResponse cancel(
                        UUID id,
                        UUID ownerId,
                        TaskLifecycleActionRequest request);

        TaskResponse reopen(
                        UUID id,
                        UUID ownerId,
                        TaskLifecycleActionRequest request);

        void delete(
                        UUID id,
                        UUID ownerId);

        TaskResponse duplicate(
                        UUID id,
                        UUID ownerId);

        TaskResponse getById(
                        UUID id,
                        UUID ownerId);
}
