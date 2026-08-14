package com.lifebalance.task.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifebalance.task.dto.request.CreateTagRequest;
import com.lifebalance.task.dto.request.UpdateTagRequest;
import com.lifebalance.task.dto.response.TagResponse;
import com.lifebalance.task.model.Tag;
import com.lifebalance.task.repository.TagRepository;
import com.lifebalance.task.service.TagService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @Override
    @Transactional
    public TagResponse create(UUID userId, CreateTagRequest request) {
        Objects.requireNonNull(userId, "User id is required");

        if (tagRepository.existsByUserIdAndName(userId, request.getName())) {
            throw new RuntimeException("Tag name already exists");
        }

        Tag tag = Tag.builder()
                .userId(userId)
                .name(request.getName())
                .description(request.getDescription())
                .build();

        return mapToResponse(tagRepository.save(tag));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> getAll(UUID userId) {
        Objects.requireNonNull(userId, "User id is required");

        return tagRepository.findByUserIdOrderByNameAsc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TagResponse getById(UUID userId, UUID id) {
        Objects.requireNonNull(userId, "User id is required");

        return mapToResponse(getTagOrThrow(userId, id));
    }

    @Override
    @Transactional
    public TagResponse update(
            UUID userId,
            UUID id,
            UpdateTagRequest request) {
        Objects.requireNonNull(userId, "User id is required");

        Tag tag = getTagOrThrow(userId, id);

        tagRepository.findByUserIdAndName(userId, request.getName())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new RuntimeException("Tag name already exists");
                    }
                });

        tag.setName(request.getName());
        tag.setDescription(request.getDescription());

        return mapToResponse(tagRepository.save(tag));
    }

    @Override
    @Transactional
    public void delete(UUID userId, UUID id) {
        Objects.requireNonNull(userId, "User id is required");

        Tag tag = getTagOrThrow(userId, id);

        tagRepository.delete(tag);
    }

    private Tag getTagOrThrow(UUID userId, UUID id) {

        return tagRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Tag not found"));
    }

    private TagResponse mapToResponse(Tag tag) {

        TagResponse response = new TagResponse();

        response.setId(tag.getId());
        response.setName(tag.getName());
        response.setDescription(tag.getDescription());
        response.setCreatedAt(tag.getCreatedAt());
        response.setUpdatedAt(tag.getUpdatedAt());

        return response;
    }
}
