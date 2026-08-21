package com.lifebalance.task.service.impl;

import com.lifebalance.common.error.AppException;
import com.lifebalance.task.dto.request.RescheduleTimelinePlacementRequest;
import com.lifebalance.task.dto.request.ScheduleTimelinePlacementRequest;
import com.lifebalance.task.error.TaskErrorCode;
import com.lifebalance.task.history.TaskChangeHistoryService;
import com.lifebalance.task.integration.TaskIntegrationPublisher;
import com.lifebalance.task.model.Task;
import com.lifebalance.task.model.TimelinePlacement;
import com.lifebalance.task.model.enums.TaskStatus;
import com.lifebalance.task.model.enums.TimelinePlacementStatus;
import com.lifebalance.task.repository.TaskRepository;
import com.lifebalance.task.repository.TimelinePlacementRepository;
import com.lifebalance.task.validation.TaskLifecyclePolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimelinePlacementServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TimelinePlacementRepository timelinePlacementRepository;

    @Mock
    private TaskLifecyclePolicy taskLifecyclePolicy;

    @Mock
    private TaskChangeHistoryService taskChangeHistoryService;

    @Mock
    private TaskIntegrationPublisher taskIntegrationPublisher;

    @InjectMocks
    private TimelinePlacementServiceImpl timelinePlacementService;

    private final UUID ownerId = UUID.randomUUID();
    private final UUID taskId = UUID.randomUUID();
    private final UUID placementId = UUID.randomUUID();
    private final OffsetDateTime startAt = OffsetDateTime.parse("2026-08-21T09:00:00+07:00");
    private final OffsetDateTime endAt = OffsetDateTime.parse("2026-08-21T10:00:00+07:00");

    private Task task;

    @BeforeEach
    void setUp() {
        task = Task.builder()
                .id(taskId)
                .ownerId(ownerId)
                .userId(ownerId)
                .name("Design timeline")
                .status(TaskStatus.PLANNED)
                .estimatedMinutes(60)
                .build();
    }

    @Test
    void scheduleCreatesPlacementAndMarksTaskScheduled() {
        ScheduleTimelinePlacementRequest request = new ScheduleTimelinePlacementRequest();
        request.setTaskId(taskId);
        request.setStartAt(startAt);
        request.setEndAt(endAt);
        request.setTimezone("Asia/Bangkok");
        request.setReason("Focus block");

        when(taskRepository.findByIdAndOwnerId(taskId, ownerId)).thenReturn(Optional.of(task));
        when(timelinePlacementRepository.existsOverlappingPlacement(
                eq(ownerId),
                eq(TimelinePlacementStatus.ACTIVE),
                eq(null),
                eq(startAt),
                eq(endAt))).thenReturn(false);
        when(timelinePlacementRepository.save(any(TimelinePlacement.class)))
                .thenAnswer(invocation -> {
                    TimelinePlacement placement = invocation.getArgument(0);
                    placement.setId(placementId);
                    return placement;
                });

        var response = timelinePlacementService.schedule(ownerId, request);

        assertThat(response.getId()).isEqualTo(placementId);
        assertThat(response.getTaskId()).isEqualTo(taskId);
        assertThat(response.getStatus()).isEqualTo(TimelinePlacementStatus.ACTIVE);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.SCHEDULED);
        assertThat(task.getScheduledStartAt()).isEqualTo(startAt);
        verify(taskRepository).save(task);
        verify(taskChangeHistoryService).recordTimelineChange(
                eq(task),
                any(TimelinePlacement.class),
                eq(ownerId),
                any(),
                eq(null),
                any(),
                eq("Focus block"));
    }

    @Test
    void scheduleRejectsConflictWithoutChangingTaskState() {
        ScheduleTimelinePlacementRequest request = new ScheduleTimelinePlacementRequest();
        request.setTaskId(taskId);
        request.setStartAt(startAt);
        request.setEndAt(endAt);

        when(taskRepository.findByIdAndOwnerId(taskId, ownerId)).thenReturn(Optional.of(task));
        when(timelinePlacementRepository.existsOverlappingPlacement(
                eq(ownerId),
                eq(TimelinePlacementStatus.ACTIVE),
                eq(null),
                eq(startAt),
                eq(endAt))).thenReturn(true);

        assertThatThrownBy(() -> timelinePlacementService.schedule(ownerId, request))
                .isInstanceOf(AppException.class)
                .extracting("code")
                .isEqualTo(TaskErrorCode.TASK_TIMELINE_CONFLICT);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.PLANNED);
        assertThat(task.getScheduledStartAt()).isNull();
        verify(taskRepository, never()).save(any(Task.class));
        verify(timelinePlacementRepository, never()).save(any(TimelinePlacement.class));
    }

    @Test
    void moveUpdatesPlacementSourceAndWritesHistory() {
        TimelinePlacement placement = TimelinePlacement.builder()
                .id(placementId)
                .ownerId(ownerId)
                .userId(ownerId)
                .task(task)
                .startAt(startAt)
                .endAt(endAt)
                .timezone("Asia/Bangkok")
                .source("MANUAL")
                .status(TimelinePlacementStatus.ACTIVE)
                .build();
        RescheduleTimelinePlacementRequest request = new RescheduleTimelinePlacementRequest();
        request.setStartAt(startAt.plusHours(1));
        request.setEndAt(endAt.plusHours(1));
        request.setTimezone("Asia/Bangkok");

        task.setStatus(TaskStatus.SCHEDULED);
        when(timelinePlacementRepository.findByIdAndOwnerId(placementId, ownerId))
                .thenReturn(Optional.of(placement));
        when(timelinePlacementRepository.existsOverlappingPlacement(
                eq(ownerId),
                eq(TimelinePlacementStatus.ACTIVE),
                eq(placementId),
                eq(request.getStartAt()),
                eq(request.getEndAt()))).thenReturn(false);
        when(timelinePlacementRepository.save(placement)).thenReturn(placement);

        var response = timelinePlacementService.move(ownerId, placementId, request);

        assertThat(response.getSource()).isEqualTo("DRAG_DROP");
        assertThat(response.getStartAt()).isEqualTo(request.getStartAt());
        verify(taskChangeHistoryService).recordTimelineChange(
                eq(task),
                eq(placement),
                eq(ownerId),
                any(),
                any(),
                any(),
                eq(null));
    }
}
