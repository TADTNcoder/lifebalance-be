package com.lifebalance.task.service;

import com.lifebalance.task.dto.request.CreateTaskRequest;
import com.lifebalance.task.dto.response.TaskResponse;
import java.util.UUID;
import com.lifebalance.task.dto.request.UpdateTaskRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {

    TaskResponse create(CreateTaskRequest request);

    TaskResponse update(UUID id, UpdateTaskRequest request);

    Page<TaskResponse> search(
            String keyword,
            Pageable pageable);

    void archive(UUID id);
}
