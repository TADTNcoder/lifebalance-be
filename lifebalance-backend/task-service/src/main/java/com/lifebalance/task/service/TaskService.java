package com.lifebalance.task.service;

import com.lifebalance.task.dto.request.CreateTaskRequest;
import com.lifebalance.task.dto.response.TaskResponse;

public interface TaskService {

    TaskResponse create(CreateTaskRequest request);
}
