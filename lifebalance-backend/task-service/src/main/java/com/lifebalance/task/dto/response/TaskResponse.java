package com.lifebalance.task.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.lifebalance.task.model.enums.DayOfWeekType;
import com.lifebalance.task.model.enums.PriorityLevel;
import com.lifebalance.task.model.enums.TaskStatus;

import lombok.*;

@Getter
@Setter
public class TaskResponse {

    private UUID id;

    private String taskName;

    private String description;

    private TaskStatus status;

    private PriorityLevel priorityLevel;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private DayOfWeekType dayOfWeek;

    private String note;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
