package com.lifebalance.task.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lifebalance.task.dto.request.CreateTagRequest;
import com.lifebalance.task.dto.request.UpdateTagRequest;
import com.lifebalance.task.model.Tag;
import com.lifebalance.task.repository.TagRepository;

@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    private TagServiceImpl tagService;

    @BeforeEach
    void setUp() {
        tagService = new TagServiceImpl(tagRepository);
    }

    @Test
    void createScopesNameCheckAndSavedTagToOwner() {
        UUID userId = UUID.randomUUID();
        CreateTagRequest request = new CreateTagRequest();
        request.setName("Work");
        request.setDescription("Work tasks");

        when(tagRepository.existsByUserIdAndName(userId, "Work")).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> {
            Tag saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        tagService.create(userId, request);

        ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository).existsByUserIdAndName(userId, "Work");
        verify(tagRepository).save(tagCaptor.capture());
        assertThat(tagCaptor.getValue().getUserId()).isEqualTo(userId);
        assertThat(tagCaptor.getValue().getName()).isEqualTo("Work");
    }

    @Test
    void getAllReadsOnlyCurrentUsersTags() {
        UUID userId = UUID.randomUUID();
        Tag tag = Tag.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .name("Health")
                .build();
        when(tagRepository.findByUserIdOrderByNameAsc(userId)).thenReturn(List.of(tag));

        assertThat(tagService.getAll(userId)).hasSize(1);

        verify(tagRepository).findByUserIdOrderByNameAsc(userId);
    }

    @Test
    void updateLoadsAndChecksDuplicatesWithinOwnerScope() {
        UUID userId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();
        Tag tag = Tag.builder()
                .id(tagId)
                .userId(userId)
                .name("Old")
                .build();
        UpdateTagRequest request = new UpdateTagRequest();
        request.setName("New");
        request.setDescription("Updated");

        when(tagRepository.findByIdAndUserId(tagId, userId)).thenReturn(Optional.of(tag));
        when(tagRepository.findByUserIdAndName(userId, "New")).thenReturn(Optional.empty());
        when(tagRepository.save(tag)).thenReturn(tag);

        tagService.update(userId, tagId, request);

        verify(tagRepository).findByIdAndUserId(tagId, userId);
        verify(tagRepository).findByUserIdAndName(userId, "New");
        assertThat(tag.getName()).isEqualTo("New");
        assertThat(tag.getDescription()).isEqualTo("Updated");
    }

    @Test
    void deleteRemovesOnlyOwnedTag() {
        UUID userId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();
        Tag tag = Tag.builder()
                .id(tagId)
                .userId(userId)
                .name("Focus")
                .build();
        when(tagRepository.findByIdAndUserId(tagId, userId)).thenReturn(Optional.of(tag));

        tagService.delete(userId, tagId);

        verify(tagRepository).findByIdAndUserId(tagId, userId);
        verify(tagRepository).delete(tag);
    }
}
