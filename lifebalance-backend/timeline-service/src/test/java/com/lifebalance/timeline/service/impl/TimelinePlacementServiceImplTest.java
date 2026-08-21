package com.lifebalance.timeline.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lifebalance.common.error.AppException;
import com.lifebalance.timeline.domain.TimelineConflictPolicy;
import com.lifebalance.timeline.domain.TimelineHistoryActionType;
import com.lifebalance.timeline.domain.TimelinePlacement;
import com.lifebalance.timeline.domain.TimelinePlacementSource;
import com.lifebalance.timeline.domain.TimelinePlacementStatus;
import com.lifebalance.timeline.domain.TimelineTask;
import com.lifebalance.timeline.domain.TimelineTaskStatus;
import com.lifebalance.timeline.dto.CancelTimelinePlacementRequest;
import com.lifebalance.timeline.dto.ScheduleTimelinePlacementRequest;
import com.lifebalance.timeline.dto.TimelinePlacementResponse;
import com.lifebalance.timeline.error.TimelineErrorCode;
import com.lifebalance.timeline.repository.TimelinePlacementRepository;
import com.lifebalance.timeline.repository.TimelineTaskRepository;
import com.lifebalance.timeline.validation.TimelinePolicyProperties;
import com.lifebalance.timeline.validation.TimelineScheduleValidator;
import java.time.LocalDate;
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
class TimelinePlacementServiceImplTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TASK_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID OTHER_TASK_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID PLACEMENT_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID CONFLICTING_PLACEMENT_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final OffsetDateTime START_AT = OffsetDateTime.parse("2026-08-21T09:00:00Z");
    private static final OffsetDateTime END_AT = OffsetDateTime.parse("2026-08-21T10:00:00Z");

    @Mock
    private TimelineTaskRepository taskRepository;

    @Mock
    private TimelinePlacementRepository placementRepository;

    @Mock
    private TimelineHistoryRecorder historyRecorder;

    @Test
    void scheduleCreatesPlacementUpdatesTaskAndRecordsHistory() {
        TimelineTask task = eligibleTask(TASK_ID, "Deep work");
        when(taskRepository.findByIdAndOwnerIdForUpdate(TASK_ID, OWNER_ID)).thenReturn(Optional.of(task));
        when(placementRepository.findConflicts(
                OWNER_ID,
                TimelinePlacementStatus.ACTIVE,
                null,
                START_AT,
                END_AT
        )).thenReturn(List.of());
        when(placementRepository.save(any(TimelinePlacement.class))).thenAnswer(invocation -> {
            TimelinePlacement placement = invocation.getArgument(0);
            setId(placement, PLACEMENT_ID);
            return placement;
        });

        TimelinePlacementResponse response = createService(TimelineConflictPolicy.REJECT)
                .schedule(OWNER_ID, scheduleRequest(false, null));

        assertThat(response.id()).isEqualTo(PLACEMENT_ID);
        assertThat(response.taskId()).isEqualTo(TASK_ID);
        assertThat(response.status()).isEqualTo(TimelinePlacementStatus.ACTIVE);
        assertThat(response.conflicted()).isFalse();
        assertThat(task.getTaskStatus()).isEqualTo(TimelineTaskStatus.SCHEDULED);
        assertThat(task.getScheduledStartAt()).isEqualTo(START_AT);
        assertThat(task.getScheduledEndAt()).isEqualTo(END_AT);
        verify(taskRepository).save(task);
        verify(historyRecorder).record(
                eq(OWNER_ID),
                eq(OWNER_ID),
                eq(TimelineHistoryActionType.TIMELINE_SCHEDULED),
                any(TimelinePlacement.class),
                eq(task),
                eq(null),
                org.mockito.ArgumentMatchers.contains("taskId=" + TASK_ID),
                eq("Focus block")
        );
    }

    @Test
    void scheduleRejectsIneligibleTaskWithoutMutatingState() {
        TimelineTask task = TimelineTask.register(
                OWNER_ID,
                OWNER_ID,
                TASK_ID,
                "Missing estimate",
                TimelineTaskStatus.PLANNED,
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        when(taskRepository.findByIdAndOwnerIdForUpdate(TASK_ID, OWNER_ID)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> createService(TimelineConflictPolicy.REJECT)
                .schedule(OWNER_ID, scheduleRequest(false, null)))
                .isInstanceOf(AppException.class)
                .extracting("code")
                .isEqualTo(TimelineErrorCode.TIMELINE_TASK_NOT_ELIGIBLE);

        assertThat(task.getTaskStatus()).isEqualTo(TimelineTaskStatus.PLANNED);
        verify(taskRepository, never()).save(any());
        verify(placementRepository, never()).save(any());
        verify(historyRecorder, never()).record(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void scheduleRejectsConflictWhenPolicyRejects() {
        TimelineTask task = eligibleTask(TASK_ID, "Deep work");
        TimelinePlacement conflicting = activePlacement(CONFLICTING_PLACEMENT_ID, OTHER_TASK_ID);
        when(taskRepository.findByIdAndOwnerIdForUpdate(TASK_ID, OWNER_ID)).thenReturn(Optional.of(task));
        when(placementRepository.findConflicts(
                OWNER_ID,
                TimelinePlacementStatus.ACTIVE,
                null,
                START_AT,
                END_AT
        )).thenReturn(List.of(conflicting));

        assertThatThrownBy(() -> createService(TimelineConflictPolicy.REJECT)
                .schedule(OWNER_ID, scheduleRequest(false, null)))
                .isInstanceOf(AppException.class)
                .extracting("code")
                .isEqualTo(TimelineErrorCode.TIMELINE_CONFLICT);

        assertThat(task.getTaskStatus()).isEqualTo(TimelineTaskStatus.PLANNED);
        verify(taskRepository, never()).save(any());
        verify(placementRepository, never()).save(any());
        verify(historyRecorder, never()).record(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void scheduleAllowsConflictWhenPolicyRequiresAndUserConfirms() {
        TimelineTask task = eligibleTask(TASK_ID, "Deep work");
        TimelinePlacement conflicting = activePlacement(CONFLICTING_PLACEMENT_ID, OTHER_TASK_ID);
        when(taskRepository.findByIdAndOwnerIdForUpdate(TASK_ID, OWNER_ID)).thenReturn(Optional.of(task));
        when(placementRepository.findConflicts(
                OWNER_ID,
                TimelinePlacementStatus.ACTIVE,
                null,
                START_AT,
                END_AT
        )).thenReturn(List.of(conflicting));
        when(placementRepository.save(any(TimelinePlacement.class))).thenAnswer(invocation -> {
            TimelinePlacement placement = invocation.getArgument(0);
            setId(placement, PLACEMENT_ID);
            return placement;
        });

        TimelinePlacementResponse response = createService(TimelineConflictPolicy.ALLOW_WITH_CONFIRMATION)
                .schedule(OWNER_ID, scheduleRequest(true, "Client meeting overlaps intentionally"));

        assertThat(response.conflicted()).isTrue();
        assertThat(response.conflictConfirmed()).isTrue();
        assertThat(response.conflictReason()).isEqualTo("Client meeting overlaps intentionally");
        verify(historyRecorder).record(
                eq(OWNER_ID),
                eq(OWNER_ID),
                eq(TimelineHistoryActionType.TIMELINE_CONFLICT_CONFIRMED),
                any(TimelinePlacement.class),
                eq(task),
                eq(null),
                org.mockito.ArgumentMatchers.contains("conflictCount=1"),
                eq("Client meeting overlaps intentionally")
        );
    }

    @Test
    void cancelActivePlacementClearsScheduledTaskAndRecordsHistory() {
        TimelineTask task = eligibleTask(TASK_ID, "Deep work");
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
                "Focus block"
        );
        setId(placement, PLACEMENT_ID);

        when(placementRepository.findByIdAndOwnerIdForUpdate(PLACEMENT_ID, OWNER_ID))
                .thenReturn(Optional.of(placement));
        when(taskRepository.findByIdAndOwnerIdForUpdate(TASK_ID, OWNER_ID))
                .thenReturn(Optional.of(task));
        when(placementRepository.save(placement)).thenReturn(placement);

        TimelinePlacementResponse response = createService(TimelineConflictPolicy.REJECT)
                .cancel(OWNER_ID, PLACEMENT_ID, new CancelTimelinePlacementRequest("No longer needed"));

        assertThat(response.status()).isEqualTo(TimelinePlacementStatus.CANCELLED);
        assertThat(task.getTaskStatus()).isEqualTo(TimelineTaskStatus.PLANNED);
        assertThat(task.getScheduledStartAt()).isNull();
        assertThat(task.getScheduledEndAt()).isNull();
        verify(historyRecorder).record(
                eq(OWNER_ID),
                eq(OWNER_ID),
                eq(TimelineHistoryActionType.TIMELINE_CANCELLED),
                eq(placement),
                eq(task),
                org.mockito.ArgumentMatchers.contains("status=ACTIVE"),
                org.mockito.ArgumentMatchers.contains("status=CANCELLED"),
                eq("No longer needed")
        );
    }

    private TimelinePlacementServiceImpl createService(TimelineConflictPolicy policy) {
        TimelinePolicyProperties policyProperties = new TimelinePolicyProperties();
        policyProperties.setConflictPolicy(policy);
        return new TimelinePlacementServiceImpl(
                taskRepository,
                placementRepository,
                new TimelineScheduleValidator(),
                policyProperties,
                historyRecorder,
                new TimelineMapper()
        );
    }

    private static ScheduleTimelinePlacementRequest scheduleRequest(boolean conflictConfirmed, String conflictReason) {
        return new ScheduleTimelinePlacementRequest(
                TASK_ID,
                START_AT,
                END_AT,
                "UTC",
                conflictConfirmed,
                conflictReason,
                "Focus block"
        );
    }

    private static TimelineTask eligibleTask(UUID taskId, String title) {
        return TimelineTask.register(
                OWNER_ID,
                OWNER_ID,
                taskId,
                title,
                TimelineTaskStatus.PLANNED,
                false,
                60,
                LocalDate.parse("2026-08-22"),
                null,
                null,
                null,
                null,
                null
        );
    }

    private static TimelinePlacement activePlacement(UUID placementId, UUID taskId) {
        TimelineTask task = eligibleTask(taskId, "Existing block");
        TimelinePlacement placement = TimelinePlacement.schedule(
                OWNER_ID,
                OWNER_ID,
                task,
                START_AT.minusMinutes(15),
                END_AT.minusMinutes(15),
                "UTC",
                TimelinePlacementSource.MANUAL,
                TimelineConflictPolicy.REJECT,
                false,
                false,
                null,
                "Existing"
        );
        setId(placement, placementId);
        return placement;
    }

    private static void setId(Object target, UUID id) {
        ReflectionTestUtils.setField(target, "id", id);
    }
}
