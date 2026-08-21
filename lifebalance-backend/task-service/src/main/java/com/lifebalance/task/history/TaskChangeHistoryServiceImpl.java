package com.lifebalance.task.history;

import com.lifebalance.task.model.Task;
import com.lifebalance.task.model.TaskChangeHistory;
import com.lifebalance.task.model.TimelinePlacement;
import com.lifebalance.task.model.enums.TaskHistoryActionType;
import com.lifebalance.task.repository.TaskChangeHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskChangeHistoryServiceImpl implements TaskChangeHistoryService {

    private final TaskChangeHistoryRepository taskChangeHistoryRepository;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void recordTaskChange(
            Task task,
            UUID actorId,
            TaskHistoryActionType actionType,
            String fieldName,
            String oldValue,
            String newValue,
            String reason) {

        taskChangeHistoryRepository.save(TaskChangeHistory.builder()
                .ownerId(task.getOwnerId())
                .actorId(actorId)
                .task(task)
                .actionType(actionType)
                .fieldName(fieldName)
                .oldValue(oldValue)
                .newValue(newValue)
                .reason(reason)
                .occurredAt(OffsetDateTime.now())
                .build());
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void recordTimelineChange(
            Task task,
            TimelinePlacement timelinePlacement,
            UUID actorId,
            TaskHistoryActionType actionType,
            String oldValue,
            String newValue,
            String reason) {

        taskChangeHistoryRepository.save(TaskChangeHistory.builder()
                .ownerId(task.getOwnerId())
                .actorId(actorId)
                .task(task)
                .timelinePlacement(timelinePlacement)
                .actionType(actionType)
                .fieldName("timeline")
                .oldValue(oldValue)
                .newValue(newValue)
                .reason(reason)
                .occurredAt(OffsetDateTime.now())
                .build());
    }
}
