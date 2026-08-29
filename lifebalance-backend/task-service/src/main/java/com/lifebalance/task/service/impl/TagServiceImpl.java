package com.lifebalance.task.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifebalance.task.dto.request.CreateTagRequest;
import com.lifebalance.task.dto.request.UpdateTagRequest;
import com.lifebalance.task.dto.response.TagResponse;
import com.lifebalance.task.error.TaskExceptions;
import com.lifebalance.task.model.Tag;
import com.lifebalance.task.repository.TagRepository;
import com.lifebalance.task.service.TagService;
import com.lifebalance.task.util.SlugGenerator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private static final Pattern LEADING_HASHES = Pattern.compile("^#+\\s*");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private final TagRepository tagRepository;

    @Override
    @Transactional
    public TagResponse create(UUID userId, CreateTagRequest request) {
        Objects.requireNonNull(userId, "User id is required");

        String normalizedName = normalizeName(request.getName());
        String slug = SlugGenerator.from(normalizedName);

        if (tagRepository.existsByUserIdAndName(userId, normalizedName)
                || tagRepository.existsByUserIdAndSlug(userId, slug)) {
            throw TaskExceptions.tagNameAlreadyExists();
        }

        Tag tag = Tag.builder()
                .userId(userId)
                .name(normalizedName)
                .slug(slug)
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
        String normalizedName = normalizeName(request.getName());
        String slug = SlugGenerator.from(normalizedName);

        boolean nameBelongsToAnotherTag = tagRepository.findByUserIdAndName(userId, normalizedName)
                .filter(existing -> !existing.getId().equals(id))
                .isPresent();
        boolean slugBelongsToAnotherTag = tagRepository.findByUserIdAndSlug(userId, slug)
                .filter(existing -> !existing.getId().equals(id))
                .isPresent();

        if (nameBelongsToAnotherTag || slugBelongsToAnotherTag) {
            throw TaskExceptions.tagNameAlreadyExists();
        }

        tag.setName(normalizedName);
        tag.setSlug(slug);
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
                .orElseThrow(TaskExceptions::tagNotFound);
    }

    private String normalizeName(String name) {
        String normalized = name == null ? "" : name.trim();
        normalized = LEADING_HASHES.matcher(normalized).replaceFirst("");
        return WHITESPACE.matcher(normalized.trim()).replaceAll(" ");
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
