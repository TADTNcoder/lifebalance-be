package com.lifebalance.task.history;

import com.lifebalance.task.model.Task;
import com.lifebalance.task.model.TimelinePlacement;
import com.lifebalance.task.model.enums.TaskHistoryActionType;

import java.util.UUID;

public interface TaskChangeHistoryService {

    void recordTaskChange(
            Task task,
            UUID actorId,
            TaskHistoryActionType actionType,
            String fieldName,
            String oldValue,
            String newValue,
            String reason);

    void recordTimelineChange(
            Task task,
            TimelinePlacement timelinePlacement,
            UUID actorId,
            TaskHistoryActionType actionType,
            String oldValue,
            String newValue,
            String reason);
}
