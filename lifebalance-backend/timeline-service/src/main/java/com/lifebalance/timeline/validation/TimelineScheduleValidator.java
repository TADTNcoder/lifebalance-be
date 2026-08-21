package com.lifebalance.timeline.validation;

import com.lifebalance.timeline.domain.TimelineTask;
import com.lifebalance.timeline.error.TimelineExceptions;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Component;

@Component
public class TimelineScheduleValidator {

    public void validateWindow(OffsetDateTime startAt, OffsetDateTime endAt) {
        if (startAt == null || endAt == null || !startAt.isBefore(endAt)) {
            throw TimelineExceptions.invalidWindow();
        }
    }

    public void validateTaskEligibility(TimelineTask task) {
        if (task == null) {
            throw TimelineExceptions.taskNotFound(null);
        }
        if (!task.isTimelineEligible()) {
            throw TimelineExceptions.taskNotEligible(
                    task.getId(),
                    "Task must be in DRAFT, PLANNED or SCHEDULED status and have Time Capital or positive estimated minutes.");
        }
    }

    public void validateTaskWindow(TimelineTask task, OffsetDateTime startAt, OffsetDateTime endAt) {
        validateWindow(startAt, endAt);
        if (task.getDeadline() != null && startAt.toLocalDate().isAfter(task.getDeadline())) {
            throw TimelineExceptions.deadlineViolation(task.getId(), startAt);
        }
        if (task.getCycleStartAt() != null
                && (startAt.isBefore(task.getCycleStartAt()) || endAt.isAfter(task.getCycleEndAt()))) {
            throw TimelineExceptions.cycleViolation(task.getId(), startAt, endAt);
        }
    }
}
