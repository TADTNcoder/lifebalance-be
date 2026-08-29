package com.lifebalance.task.service.impl;

import java.util.List;
import java.util.Objects;
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
import org.springframework.security.access.AccessDeniedException;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryResponse create(UUID ownerId, CreateCategoryRequest request) {
        Objects.requireNonNull(ownerId, "Owner id is required");
        String name = request.getName().trim();

        if (categoryRepository.existsVisibleName(ownerId, name)) {
            throw TaskExceptions.categoryNameAlreadyExists();
        }

        String slug = resolveSlug(request.getSlug(), name);
        if (categoryRepository.existsVisibleSlug(ownerId, slug)) {
            throw TaskExceptions.categorySlugAlreadyExists();
        }

        Category category = Category.builder()
                .ownerId(ownerId)
                .name(name)
                .slug(slug)
                .description(request.getDescription())
                .color(request.getColor())
                .icon(request.getIcon())
                .build();

        category = categoryRepository.save(category);

        return mapToResponse(category, ownerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll(UUID ownerId) {
        Objects.requireNonNull(ownerId, "Owner id is required");

        return categoryRepository.findVisibleByOwnerId(ownerId)
                .stream()
                .map(category -> mapToResponse(category, ownerId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(UUID ownerId, UUID id) {
        Objects.requireNonNull(ownerId, "Owner id is required");

        return mapToResponse(getVisibleCategoryOrThrow(ownerId, id), ownerId);
    }

    @Override
    @Transactional
    public CategoryResponse update(
            UUID ownerId,
            UUID id,
            UpdateCategoryRequest request) {
        Objects.requireNonNull(ownerId, "Owner id is required");

        Category category = getCategoryForMutation(ownerId, id);
        String name = request.getName().trim();

        if (categoryRepository.existsVisibleNameExcludingId(ownerId, name, id)) {
            throw TaskExceptions.categoryNameAlreadyExists();
        }

        String slug = resolveSlug(request.getSlug(), category.getSlug());
        if (categoryRepository.existsVisibleSlugExcludingId(ownerId, slug, id)) {
            throw TaskExceptions.categorySlugAlreadyExists();
        }

        category.setName(name);
        category.setSlug(slug);
        category.setDescription(request.getDescription());
        category.setColor(request.getColor());
        category.setIcon(request.getIcon());

        category = categoryRepository.save(category);

        return mapToResponse(category, ownerId);
    }

    @Override
    @Transactional
    public void delete(UUID ownerId, UUID id) {
        Objects.requireNonNull(ownerId, "Owner id is required");

        Category category = getCategoryForMutation(ownerId, id);

        categoryRepository.delete(category);
    }

    private void rejectSystemCategoryMutation(Category category) {
        if (Boolean.TRUE.equals(category.getIsSystem())) {
            throw new AccessDeniedException("System categories cannot be modified.");
        }
    }

    private Category getVisibleCategoryOrThrow(UUID ownerId, UUID id) {
        return categoryRepository.findVisibleByIdAndOwnerId(id, ownerId)
                .orElseThrow(TaskExceptions::categoryNotFound);
    }

    private Category getCategoryForMutation(UUID ownerId, UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(TaskExceptions::categoryNotFound);
        rejectSystemCategoryMutation(category);
        if (category.getOwnerId() == null) {
            throw new AccessDeniedException("Only categories created by the current user can be modified.");
        }
        if (!ownerId.equals(category.getOwnerId())) {
            throw TaskExceptions.categoryNotFound();
        }
        return category;
    }

    private CategoryResponse mapToResponse(Category category, UUID ownerId) {

        CategoryResponse response = new CategoryResponse();

        response.setId(category.getId());
        response.setOwnerId(category.getOwnerId());
        response.setName(category.getName());
        response.setSlug(category.getSlug());
        response.setDescription(category.getDescription());
        response.setColor(category.getColor());
        response.setIcon(category.getIcon());
        response.setIsSystem(category.getIsSystem());
        response.setCanModify(
                !Boolean.TRUE.equals(category.getIsSystem())
                        && ownerId.equals(category.getOwnerId()));
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
