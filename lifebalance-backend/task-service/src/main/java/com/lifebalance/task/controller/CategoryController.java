package com.lifebalance.task.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;

import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import com.lifebalance.task.dto.request.CreateCategoryRequest;
import com.lifebalance.task.dto.request.UpdateCategoryRequest;
import com.lifebalance.task.dto.response.CategoryResponse;
import com.lifebalance.task.service.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private static final Set<String> CATEGORY_MANAGER_ROLES = Set.of("admin", "manager");

    private final CategoryService categoryService;

    @PostMapping
    public CategoryResponse create(
            @Valid @RequestBody CreateCategoryRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false)
            KeycloakUserPrincipal currentUser) {
        requireCategoryManager(currentUser);

        return categoryService.create(request);
    }

    @GetMapping
    public List<CategoryResponse> getAll() {

        return categoryService.getAll();
    }

    @GetMapping("/{id}")
    public CategoryResponse getById(
            @PathVariable UUID id) {

        return categoryService.getById(id);
    }

    @PutMapping("/{id}")
    public CategoryResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false)
            KeycloakUserPrincipal currentUser) {
        requireCategoryManager(currentUser);

        return categoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable UUID id,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false)
            KeycloakUserPrincipal currentUser) {
        requireCategoryManager(currentUser);

        categoryService.delete(id);
    }

    private static void requireCategoryManager(KeycloakUserPrincipal currentUser) {
        if (currentUser == null || currentUser.userId() == null) {
            throw new AccessDeniedException("Category management permission is required.");
        }
        boolean allowed = currentUser.roles().stream()
                .map(CategoryController::normalizeRole)
                .anyMatch(CATEGORY_MANAGER_ROLES::contains);
        if (!allowed) {
            throw new AccessDeniedException("Category management permission is required.");
        }
    }

    private static String normalizeRole(String role) {
        if (role == null) {
            return "";
        }
        String normalized = role.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("role_") || normalized.startsWith("role-")) {
            return normalized.substring(5);
        }
        return normalized;
    }
}
