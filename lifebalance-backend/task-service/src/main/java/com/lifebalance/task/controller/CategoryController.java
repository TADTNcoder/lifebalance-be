package com.lifebalance.task.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

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
    private final CategoryService categoryService;

    @PostMapping
    public CategoryResponse create(
            @Valid @RequestBody CreateCategoryRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false)
            KeycloakUserPrincipal currentUser) {
        return categoryService.create(resolveOwnerId(currentUser), request);
    }

    @GetMapping
    public List<CategoryResponse> getAll(
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false)
            KeycloakUserPrincipal currentUser) {
        return categoryService.getAll(resolveOwnerId(currentUser));
    }

    @GetMapping("/{id}")
    public CategoryResponse getById(
            @PathVariable UUID id,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false)
            KeycloakUserPrincipal currentUser) {
        return categoryService.getById(resolveOwnerId(currentUser), id);
    }

    @PutMapping("/{id}")
    public CategoryResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false)
            KeycloakUserPrincipal currentUser) {
        return categoryService.update(resolveOwnerId(currentUser), id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable UUID id,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false)
            KeycloakUserPrincipal currentUser) {
        categoryService.delete(resolveOwnerId(currentUser), id);
    }

    private static UUID resolveOwnerId(KeycloakUserPrincipal currentUser) {
        if (currentUser == null || currentUser.userId() == null) {
            throw new AuthenticationCredentialsNotFoundException(
                    "Authenticated internal user id is required.");
        }
        return currentUser.userId();
    }
}
