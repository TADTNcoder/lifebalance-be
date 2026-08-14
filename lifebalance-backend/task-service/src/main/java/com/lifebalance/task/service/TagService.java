package com.lifebalance.task.service;

import java.util.List;
import java.util.UUID;

import com.lifebalance.task.dto.request.CreateTagRequest;
import com.lifebalance.task.dto.request.UpdateTagRequest;
import com.lifebalance.task.dto.response.TagResponse;

public interface TagService {

    TagResponse create(UUID userId, CreateTagRequest request);

    List<TagResponse> getAll(UUID userId);

    TagResponse getById(UUID userId, UUID id);

    TagResponse update(UUID userId, UUID id, UpdateTagRequest request);

    void delete(UUID userId, UUID id);
}
