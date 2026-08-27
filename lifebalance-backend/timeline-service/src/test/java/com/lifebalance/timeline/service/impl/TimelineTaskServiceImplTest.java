package com.lifebalance.timeline.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lifebalance.timeline.domain.TimelineConflictPolicy;
import com.lifebalance.timeline.domain.TimelineHistoryActionType;
import com.lifebalance.timeline.domain.TimelinePlacement;
import com.lifebalance.timeline.domain.TimelinePlacementSource;
import com.lifebalance.timeline.domain.TimelinePlacementStatus;
import com.lifebalance.timeline.domain.TimelineTask;
import com.lifebalance.timeline.domain.TimelineTaskStatus;
import com.lifebalance.timeline.dto.TimelineTaskResponse;
import com.lifebalance.timeline.dto.UpsertTimelineTaskRequest;
import com.lifebalance.timeline.repository.TimelinePlacementRepository;
import com.lifebalance.timeline.repository.TimelineTaskRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TimelineTaskServiceImplTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TASK_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID PLACEMENT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final OffsetDateTime START_AT = OffsetDateTime.parse("2026-08-21T09:00:00Z");
    private static final OffsetDateTime END_AT = OffsetDateTime.parse("2026-08-21T10:00:00Z");

    @Mock
    private TimelineTaskRepository taskRepository;

    @Mock
    private TimelinePlacementRepository placementRepository;

    @Mock
    private TimelineHistoryRecorder historyRecorder;

    @Test
    void upsertCreatesTaskSnapshotAndRecordsHistory() {
        when(taskRepository.findByIdAndOwnerIdForUpdate(TASK_ID, OWNER_ID)).thenReturn(Optional.empty());
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.empty());
        when(taskRepository.save(any(TimelineTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TimelineTaskResponse response = createService().upsertTask(OWNER_ID, new UpsertTimelineTaskRequest(
                OWNER_ID,
                TASK_ID,
                "Design schedule",
                TimelineTaskStatus.PLANNED,
                false,
                45,
                null,
                null,
                null,
                null,
                null,
                null
        ));

        assertThat(response.taskId()).isEqualTo(TASK_ID);
        assertThat(response.timelineEligible()).isTrue();
        verify(historyRecorder).record(
                eq(OWNER_ID),
                eq(OWNER_ID),
                eq(TimelineHistoryActionType.TASK_SNAPSHOT_REGISTERED),
                eq(null),
                any(TimelineTask.class),
                eq(null),
                org.mockito.ArgumentMatchers.contains("taskId=" + TASK_ID),
                eq("Task snapshot registered")
        );
    }

    @Test
    void upsertArchivedTaskArchivesActivePlacementsAndRecordsHistory() {
        TimelineTask task = TimelineTask.register(
                OWNER_ID,
                OWNER_ID,
                TASK_ID,
                "Design schedule",
                TimelineTaskStatus.SCHEDULED,
                false,
                45,
                null,
                null,
                null,
                null,
                null,
                null
        );
        task.markScheduled(START_AT, END_AT, OWNER_ID);
        TimelinePlacement placement = TimelinePlacement.schedule(
                OWNER_ID,
                OWNER_ID,
                task,
                START_AT,
                END_AT,
                "UTC",
                TimelinePlacementSource.MANUAL,
                TimelineConflictPolicy.REJECT,
                false,
                false,
                null,
                "Focus"
        );
        ReflectionTestUtils.setField(placement, "id", PLACEMENT_ID);

        when(taskRepository.findByIdAndOwnerIdForUpdate(TASK_ID, OWNER_ID)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(placementRepository.findByOwnerIdAndTaskIdAndStatus(OWNER_ID, TASK_ID, TimelinePlacementStatus.ACTIVE))
                .thenReturn(List.of(placement));
        when(placementRepository.save(placement)).thenReturn(placement);

        TimelineTaskResponse response = createService().upsertTask(OWNER_ID, new UpsertTimelineTaskRequest(
                OWNER_ID,
                TASK_ID,
                "Design schedule",
                TimelineTaskStatus.ARCHIVED,
                false,
                45,
                null,
                null,
                null,
                null,
                null,
                null
        ));

        assertThat(response.taskStatus()).isEqualTo(TimelineTaskStatus.ARCHIVED);
        assertThat(placement.getStatus()).isEqualTo(TimelinePlacementStatus.ARCHIVED);
        verify(historyRecorder).record(
                eq(OWNER_ID),
                eq(OWNER_ID),
                eq(TimelineHistoryActionType.TIMELINE_ARCHIVED),
                eq(placement),
                eq(task),
                org.mockito.ArgumentMatchers.contains("status=ACTIVE"),
                org.mockito.ArgumentMatchers.contains("status=ARCHIVED"),
                eq("Task snapshot status changed to ARCHIVED")
        );
    }

    private TimelineTaskServiceImpl createService() {
        return new TimelineTaskServiceImpl(
                taskRepository,
                placementRepository,
                historyRecorder,
                new TimelineMapper()
        );
    }
}
