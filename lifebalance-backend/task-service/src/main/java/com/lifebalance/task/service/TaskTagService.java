package com.lifebalance.task.service;

import java.util.List;
import java.util.UUID;

import com.lifebalance.task.dto.response.TagResponse;

public interface TaskTagService {

    void assignTag(
            UUID taskId,
            UUID tagId);

    void removeTag(
            UUID taskId,
            UUID tagId);

    List<TagResponse> getTags(
            UUID taskId);
}