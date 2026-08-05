package com.lifebalance.task.service;

import java.util.List;
import java.util.UUID;

import com.lifebalance.task.dto.request.CreateCategoryRequest;
import com.lifebalance.task.dto.request.UpdateCategoryRequest;
import com.lifebalance.task.dto.response.CategoryResponse;

public interface CategoryService {
    CategoryResponse create(CreateCategoryRequest request);

    List<CategoryResponse> getAll();

    CategoryResponse getById(UUID id);

    CategoryResponse update(UUID id, UpdateCategoryRequest request);

    void delete(UUID id);
}
