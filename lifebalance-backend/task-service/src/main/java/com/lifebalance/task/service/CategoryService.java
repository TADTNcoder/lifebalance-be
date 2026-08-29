package com.lifebalance.task.service;

import java.util.List;
import java.util.UUID;

import com.lifebalance.task.dto.request.CreateCategoryRequest;
import com.lifebalance.task.dto.request.UpdateCategoryRequest;
import com.lifebalance.task.dto.response.CategoryResponse;

public interface CategoryService {
    CategoryResponse create(UUID ownerId, CreateCategoryRequest request);

    List<CategoryResponse> getAll(UUID ownerId);

    CategoryResponse getById(UUID ownerId, UUID id);

    CategoryResponse update(UUID ownerId, UUID id, UpdateCategoryRequest request);

    void delete(UUID ownerId, UUID id);
}
