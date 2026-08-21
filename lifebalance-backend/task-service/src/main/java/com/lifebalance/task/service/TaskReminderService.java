package com.lifebalance.task.service;

import com.lifebalance.task.dto.request.ReminderRequest;
import com.lifebalance.task.dto.request.TaskLifecycleActionRequest;
import com.lifebalance.task.dto.response.ReminderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface TaskReminderService {

    ReminderResponse create(
            UUID ownerId,
            ReminderRequest request);

    ReminderResponse update(
            UUID ownerId,
            UUID reminderId,
            ReminderRequest request);

    void cancel(
            UUID ownerId,
            UUID reminderId,
            TaskLifecycleActionRequest request);

    Page<ReminderResponse> getByTask(
            UUID ownerId,
            UUID taskId,
            Pageable pageable);

    Page<ReminderResponse> getUpcoming(
            UUID ownerId,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable);
}
