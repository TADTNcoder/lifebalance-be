package com.lifebalance.task.service.impl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifebalance.task.dto.response.TagResponse;
import com.lifebalance.task.error.TaskExceptions;
import com.lifebalance.task.history.TaskChangeHistoryService;
import com.lifebalance.task.model.Tag;
import com.lifebalance.task.model.Task;
import com.lifebalance.task.model.TaskTag;
import com.lifebalance.task.model.TaskTagId;
import com.lifebalance.task.model.enums.TaskHistoryActionType;
import com.lifebalance.task.repository.TagRepository;
import com.lifebalance.task.repository.TaskRepository;
import com.lifebalance.task.repository.TaskTagRepository;
import com.lifebalance.task.service.TaskTagService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskTagServiceImpl implements TaskTagService {

    private final TaskRepository taskRepository;
    private final TagRepository tagRepository;
    private final TaskTagRepository taskTagRepository;
    private final TaskChangeHistoryService taskChangeHistoryService;

    @Override
    @Transactional
    public void assignTag(
            UUID ownerId,
            UUID taskId,
            UUID tagId) {

        Task task = taskRepository.findByIdAndOwnerId(
                taskId,
                ownerId)
                .orElseThrow(TaskExceptions::taskNotFound);

        Tag tag = tagRepository.findByIdAndUserId(
                tagId,
                ownerId)
                .orElseThrow(TaskExceptions::tagNotFound);

        if (taskTagRepository.existsByTaskIdAndTagId(
                taskId,
                tagId)) {

            throw TaskExceptions.taskTagAlreadyAssigned();
        }

        TaskTag taskTag = TaskTag.builder()
                .id(new TaskTagId(taskId, tagId))
                .task(task)
                .tag(tag)
                .assignedAt(OffsetDateTime.now())
                .build();

        taskTagRepository.save(taskTag);
        taskChangeHistoryService.recordTaskChange(
                task,
                ownerId,
                TaskHistoryActionType.TASK_TAG_ASSIGNED,
                "tag",
                null,
                String.valueOf(tagId),
                null);
    }

    @Override
    @Transactional
    public void removeTag(
            UUID ownerId,
            UUID taskId,
            UUID tagId) {

        Task task = taskRepository.findByIdAndOwnerId(
                taskId,
                ownerId)
                .orElseThrow(TaskExceptions::taskNotFound);

        Tag tag = tagRepository.findByIdAndUserId(
                tagId,
                ownerId)
                .orElseThrow(TaskExceptions::tagNotFound);

        if (!taskTagRepository.existsByTaskIdAndTagId(
                taskId,
                tagId)) {

            throw TaskExceptions.taskTagNotAssigned();
        }

        taskTagRepository.deleteByTaskIdAndTagId(
                taskId,
                tagId);
        taskChangeHistoryService.recordTaskChange(
                task,
                ownerId,
                TaskHistoryActionType.TASK_TAG_REMOVED,
                "tag",
                String.valueOf(tag.getId()),
                null,
                null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> getTags(
            UUID ownerId,
            UUID taskId) {

        if (taskRepository.findByIdAndOwnerId(
                taskId,
                ownerId).isEmpty()) {
            throw TaskExceptions.taskNotFound();
        }

        return tagRepository.findByTaskIdAndUserId(
                taskId,
                ownerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private TagResponse mapToResponse(Tag tag) {

        TagResponse response = new TagResponse();

        response.setId(tag.getId());
        response.setName(tag.getName());
        response.setDescription(tag.getDescription());
        response.setCreatedAt(tag.getCreatedAt());
        response.setUpdatedAt(tag.getUpdatedAt());

        return response;
    }
}
