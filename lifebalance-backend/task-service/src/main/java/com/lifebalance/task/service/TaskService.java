package com.lifebalance.task.service;

import com.lifebalance.task.dto.request.CreateTaskRequest;
import com.lifebalance.task.dto.response.TaskResponse;
import java.util.UUID;
import com.lifebalance.task.dto.request.UpdateTaskRequest;

public interface TaskService {

    TaskResponse create(CreateTaskRequest request);

    TaskResponse update(UUID id, UpdateTaskRequest request);
}
