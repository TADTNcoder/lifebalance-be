package com.lifebalance.task.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;

import com.lifebalance.task.dto.request.CreateTagRequest;
import com.lifebalance.task.dto.request.UpdateTagRequest;
import com.lifebalance.task.dto.response.TagResponse;
import com.lifebalance.task.service.TagService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping
    public TagResponse create(
            @Valid @RequestBody CreateTagRequest request) {

        return tagService.create(request);
    }

    @GetMapping
    public List<TagResponse> getAll() {

        return tagService.getAll();
    }

    @GetMapping("/{id}")
    public TagResponse getById(
            @PathVariable UUID id) {

        return tagService.getById(id);
    }

    @PutMapping("/{id}")
    public TagResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTagRequest request) {

        return tagService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable UUID id) {

        tagService.delete(id);
    }
}