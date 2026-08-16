package com.lifebalance.task.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifebalance.task.dto.request.CreateCategoryRequest;
import com.lifebalance.task.dto.request.UpdateCategoryRequest;
import com.lifebalance.task.dto.response.CategoryResponse;
import com.lifebalance.task.error.TaskExceptions;
import com.lifebalance.task.model.Category;
import com.lifebalance.task.repository.CategoryRepository;
import com.lifebalance.task.service.CategoryService;
import com.lifebalance.task.util.SlugGenerator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {

        if (categoryRepository.existsByName(request.getName())) {
            throw TaskExceptions.categoryNameAlreadyExists();
        }

        String slug = resolveSlug(request.getSlug(), request.getName());
        if (categoryRepository.existsBySlug(slug)) {
            throw TaskExceptions.categorySlugAlreadyExists();
        }

        Category category = Category.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .color(request.getColor())
                .icon(request.getIcon())
                .build();

        category = categoryRepository.save(category);

        return mapToResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(UUID id) {

        return mapToResponse(getCategoryOrThrow(id));
    }

    @Override
    @Transactional
    public CategoryResponse update(
            UUID id,
            UpdateCategoryRequest request) {

        Category category = getCategoryOrThrow(id);

        categoryRepository.findByName(request.getName())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw TaskExceptions.categoryNameAlreadyExists();
                    }
                });

        String slug = resolveSlug(request.getSlug(), category.getSlug());
        categoryRepository.findBySlug(slug)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw TaskExceptions.categorySlugAlreadyExists();
                    }
                });

        category.setName(request.getName());
        category.setSlug(slug);
        category.setDescription(request.getDescription());
        category.setColor(request.getColor());
        category.setIcon(request.getIcon());

        category = categoryRepository.save(category);

        return mapToResponse(category);
    }

    @Override
    @Transactional
    public void delete(UUID id) {

        Category category = getCategoryOrThrow(id);

        categoryRepository.delete(category);
    }

    private Category getCategoryOrThrow(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(TaskExceptions::categoryNotFound);
    }

    private CategoryResponse mapToResponse(Category category) {

        CategoryResponse response = new CategoryResponse();

        response.setId(category.getId());
        response.setName(category.getName());
        response.setSlug(category.getSlug());
        response.setDescription(category.getDescription());
        response.setColor(category.getColor());
        response.setIcon(category.getIcon());
        response.setIsSystem(category.getIsSystem());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());

        return response;
    }

    private String resolveSlug(String requestedSlug, String fallbackValue) {
        if (requestedSlug == null || requestedSlug.isBlank()) {
            return SlugGenerator.from(fallbackValue);
        }
        return SlugGenerator.from(requestedSlug);
    }
}
