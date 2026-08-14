package com.lifebalance.task.service.impl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifebalance.task.dto.response.TagResponse;
import com.lifebalance.task.model.Tag;
import com.lifebalance.task.model.Task;
import com.lifebalance.task.model.TaskTag;
import com.lifebalance.task.model.TaskTagId;
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

    @Override
    @Transactional
    public void assignTag(
            UUID taskId,
            UUID tagId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found"));

        if (taskTagRepository.existsByTaskIdAndTagId(
                taskId,
                tagId)) {

            throw new RuntimeException("Tag already assigned to task");
        }

        TaskTag taskTag = TaskTag.builder()
                .id(new TaskTagId(taskId, tagId))
                .task(task)
                .tag(tag)
                .assignedAt(OffsetDateTime.now())
                .build();

        taskTagRepository.save(taskTag);
    }

    @Override
    @Transactional
    public void removeTag(
            UUID taskId,
            UUID tagId) {

        if (!taskRepository.existsById(taskId)) {
            throw new RuntimeException("Task not found");
        }

        if (!tagRepository.existsById(tagId)) {
            throw new RuntimeException("Tag not found");
        }

        if (!taskTagRepository.existsByTaskIdAndTagId(
                taskId,
                tagId)) {

            throw new RuntimeException("Tag is not assigned to task");
        }

        taskTagRepository.deleteByTaskIdAndTagId(
                taskId,
                tagId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> getTags(UUID taskId) {

        if (!taskRepository.existsById(taskId)) {
            throw new RuntimeException("Task not found");
        }

        return taskTagRepository.findByTaskId(taskId)
                .stream()
                .map(TaskTag::getTag)
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