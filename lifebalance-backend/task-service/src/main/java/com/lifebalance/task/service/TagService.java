package com.lifebalance.task.service;

import java.util.List;
import java.util.UUID;

import com.lifebalance.task.dto.request.CreateTagRequest;
import com.lifebalance.task.dto.request.UpdateTagRequest;
import com.lifebalance.task.dto.response.TagResponse;

public interface TagService {

    TagResponse create(CreateTagRequest request);

    List<TagResponse> getAll();

    TagResponse getById(UUID id);

    TagResponse update(UUID id, UpdateTagRequest request);

    void delete(UUID id);
}