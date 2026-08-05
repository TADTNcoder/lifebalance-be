package com.lifebalance.task.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

import com.lifebalance.task.model.enums.DayOfWeekType;
import com.lifebalance.task.model.enums.PriorityLevel;
import com.lifebalance.task.model.enums.TaskStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTaskRequest {
    @NotBlank
    private String taskName;

    private String description;

    @NotNull
    private PriorityLevel priorityLevel;

    @NotNull
    private TaskStatus status;

    @NotNull
    private LocalDate startDate;

    private LocalDate endDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private DayOfWeekType dayOfWeek;

    private String note;
}
