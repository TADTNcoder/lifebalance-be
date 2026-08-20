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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lifebalance.task.dto.response.TagResponse;
import com.lifebalance.task.model.Tag;
import com.lifebalance.task.model.Task;
import com.lifebalance.task.model.TaskTag;
import com.lifebalance.task.repository.TagRepository;
import com.lifebalance.task.repository.TaskRepository;
import com.lifebalance.task.repository.TaskTagRepository;

@ExtendWith(MockitoExtension.class)
class TaskTagServiceImplTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TASK_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID TAG_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TaskTagRepository taskTagRepository;

    @InjectMocks
    private TaskTagServiceImpl taskTagService;

    @Test
    void assignTagUsesOwnerScopedTaskAndTagLookup() {
        Task task = task();
        Tag tag = tag();

        when(taskRepository.findByIdAndOwnerId(TASK_ID, OWNER_ID))
                .thenReturn(Optional.of(task));
        when(tagRepository.findByIdAndUserId(TAG_ID, OWNER_ID))
                .thenReturn(Optional.of(tag));
        when(taskTagRepository.existsByTaskIdAndTagId(TASK_ID, TAG_ID))
                .thenReturn(false);

        taskTagService.assignTag(OWNER_ID, TASK_ID, TAG_ID);

        ArgumentCaptor<TaskTag> captor = ArgumentCaptor.forClass(TaskTag.class);
        verify(taskTagRepository).save(captor.capture());
        assertThat(captor.getValue().getTask()).isSameAs(task);
        assertThat(captor.getValue().getTag()).isSameAs(tag);
    }

    @Test
    void assignTagRejectsTaskOutsideOwnerScope() {
        when(taskRepository.findByIdAndOwnerId(TASK_ID, OWNER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskTagService.assignTag(OWNER_ID, TASK_ID, TAG_ID))
                .hasMessage("Task not found");

        verify(tagRepository, never()).findByIdAndUserId(any(), any());
        verify(taskTagRepository, never()).save(any());
    }

    @Test
    void assignTagRejectsTagOutsideOwnerScope() {
        when(taskRepository.findByIdAndOwnerId(TASK_ID, OWNER_ID))
                .thenReturn(Optional.of(task()));
        when(tagRepository.findByIdAndUserId(TAG_ID, OWNER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskTagService.assignTag(OWNER_ID, TASK_ID, TAG_ID))
                .hasMessage("Tag not found");

        verify(taskTagRepository, never()).save(any());
    }

    @Test
    void getTagsUsesOwnerScopedTagQuery() {
        when(taskRepository.findByIdAndOwnerId(TASK_ID, OWNER_ID))
                .thenReturn(Optional.of(task()));
        when(tagRepository.findByTaskIdAndUserId(TASK_ID, OWNER_ID))
                .thenReturn(List.of(tag()));

        List<TagResponse> tags = taskTagService.getTags(OWNER_ID, TASK_ID);

        assertThat(tags)
                .singleElement()
                .extracting(TagResponse::getId)
                .isEqualTo(TAG_ID);
        verify(taskTagRepository, never()).findByTaskId(any());
    }

    @Test
    void removeTagRequiresTaskAndTagInOwnerScope() {
        when(taskRepository.findByIdAndOwnerId(TASK_ID, OWNER_ID))
                .thenReturn(Optional.of(task()));
        when(tagRepository.existsByIdAndUserId(TAG_ID, OWNER_ID))
                .thenReturn(false);

        assertThatThrownBy(() -> taskTagService.removeTag(OWNER_ID, TASK_ID, TAG_ID))
                .hasMessage("Tag not found");

        verify(taskTagRepository, never()).deleteByTaskIdAndTagId(any(), any());
    }

    private Task task() {
        Task task = Task.builder()
                .ownerId(OWNER_ID)
                .userId(OWNER_ID)
                .name("Planning")
                .build();
        task.setId(TASK_ID);
        return task;
    }

    private Tag tag() {
        Tag tag = Tag.builder()
                .userId(OWNER_ID)
                .name("Focus")
                .build();
        tag.setId(TAG_ID);
        return tag;
    }
}
