package com.lifebalance.task.controller;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

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
            @Valid @RequestBody CreateCategoryRequest request) {

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
            @Valid @RequestBody UpdateCategoryRequest request) {

        return categoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable UUID id) {

        categoryService.delete(id);
    }
}
