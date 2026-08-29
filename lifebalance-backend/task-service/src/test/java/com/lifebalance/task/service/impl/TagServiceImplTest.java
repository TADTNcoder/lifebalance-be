package com.lifebalance.task.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

import com.lifebalance.common.error.AppException;
import com.lifebalance.task.dto.request.CreateTagRequest;
import com.lifebalance.task.dto.request.UpdateTagRequest;
import com.lifebalance.task.error.TaskErrorCode;
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
        when(tagRepository.existsByUserIdAndSlug(userId, "work")).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> {
            Tag saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        tagService.create(userId, request);

        ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository).existsByUserIdAndName(userId, "Work");
        verify(tagRepository).existsByUserIdAndSlug(userId, "work");
        verify(tagRepository).save(tagCaptor.capture());
        assertThat(tagCaptor.getValue().getUserId()).isEqualTo(userId);
        assertThat(tagCaptor.getValue().getName()).isEqualTo("Work");
        assertThat(tagCaptor.getValue().getSlug()).isEqualTo("work");
    }

    @Test
    void createNormalizesLeadingHashAndWhitespaceBeforeSaving() {
        UUID userId = UUID.randomUUID();
        CreateTagRequest request = new CreateTagRequest();
        request.setName("  #Tập   trung  ");
        request.setDescription("Deep work");

        when(tagRepository.existsByUserIdAndName(userId, "Tập trung")).thenReturn(false);
        when(tagRepository.existsByUserIdAndSlug(userId, "tap-trung")).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> {
            Tag saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        tagService.create(userId, request);

        ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository).save(tagCaptor.capture());
        assertThat(tagCaptor.getValue().getName()).isEqualTo("Tập trung");
        assertThat(tagCaptor.getValue().getSlug()).isEqualTo("tap-trung");
    }

    @Test
    void createRejectsANameThatProducesAnExistingSlug() {
        UUID userId = UUID.randomUUID();
        CreateTagRequest request = new CreateTagRequest();
        request.setName("#Tập-trung");

        when(tagRepository.existsByUserIdAndName(userId, "Tập-trung")).thenReturn(false);
        when(tagRepository.existsByUserIdAndSlug(userId, "tap-trung")).thenReturn(true);

        assertThatThrownBy(() -> tagService.create(userId, request))
                .isInstanceOfSatisfying(AppException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(TaskErrorCode.TAG_NAME_ALREADY_EXISTS));

        verify(tagRepository, never()).save(any(Tag.class));
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
        when(tagRepository.findByUserIdAndSlug(userId, "new")).thenReturn(Optional.empty());
        when(tagRepository.save(tag)).thenReturn(tag);

        tagService.update(userId, tagId, request);

        verify(tagRepository).findByIdAndUserId(tagId, userId);
        verify(tagRepository).findByUserIdAndName(userId, "New");
        verify(tagRepository).findByUserIdAndSlug(userId, "new");
        assertThat(tag.getName()).isEqualTo("New");
        assertThat(tag.getSlug()).isEqualTo("new");
        assertThat(tag.getDescription()).isEqualTo("Updated");
    }

    @Test
    void updateRejectsSlugOwnedByAnotherTag() {
        UUID userId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();
        Tag tag = Tag.builder()
                .id(tagId)
                .userId(userId)
                .name("Old")
                .slug("old")
                .build();
        Tag conflictingTag = Tag.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .name("Tập trung")
                .slug("tap-trung")
                .build();
        UpdateTagRequest request = new UpdateTagRequest();
        request.setName("#Tập-trung");

        when(tagRepository.findByIdAndUserId(tagId, userId)).thenReturn(Optional.of(tag));
        when(tagRepository.findByUserIdAndName(userId, "Tập-trung")).thenReturn(Optional.empty());
        when(tagRepository.findByUserIdAndSlug(userId, "tap-trung"))
                .thenReturn(Optional.of(conflictingTag));

        assertThatThrownBy(() -> tagService.update(userId, tagId, request))
                .isInstanceOfSatisfying(AppException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(TaskErrorCode.TAG_NAME_ALREADY_EXISTS));

        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void updateAllowsNormalizedNameAndSlugOwnedByCurrentTag() {
        UUID userId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();
        Tag tag = Tag.builder()
                .id(tagId)
                .userId(userId)
                .name("Tập trung")
                .slug("tap-trung")
                .build();
        UpdateTagRequest request = new UpdateTagRequest();
        request.setName(" #Tập   trung ");

        when(tagRepository.findByIdAndUserId(tagId, userId)).thenReturn(Optional.of(tag));
        when(tagRepository.findByUserIdAndName(userId, "Tập trung")).thenReturn(Optional.of(tag));
        when(tagRepository.findByUserIdAndSlug(userId, "tap-trung")).thenReturn(Optional.of(tag));
        when(tagRepository.save(tag)).thenReturn(tag);

        tagService.update(userId, tagId, request);

        assertThat(tag.getName()).isEqualTo("Tập trung");
        assertThat(tag.getSlug()).isEqualTo("tap-trung");
        verify(tagRepository).save(tag);
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
