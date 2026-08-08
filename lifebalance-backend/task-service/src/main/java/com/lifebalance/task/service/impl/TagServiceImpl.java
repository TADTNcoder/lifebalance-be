package com.lifebalance.task.service.impl;

import java.util.List;
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
    public TagResponse create(CreateTagRequest request) {

        if (tagRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tag name already exists");
        }

        Tag tag = Tag.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        return mapToResponse(tagRepository.save(tag));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> getAll() {

        return tagRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TagResponse getById(UUID id) {

        return mapToResponse(getTagOrThrow(id));
    }

    @Override
    @Transactional
    public TagResponse update(
            UUID id,
            UpdateTagRequest request) {

        Tag tag = getTagOrThrow(id);

        tagRepository.findByName(request.getName())
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
    public void delete(UUID id) {

        Tag tag = getTagOrThrow(id);

        tagRepository.delete(tag);
    }

    private Tag getTagOrThrow(UUID id) {

        return tagRepository.findById(id)
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