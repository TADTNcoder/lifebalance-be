package com.lifebalance.task.dto.request;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import com.lifebalance.task.model.enums.DayOfWeekType;
import com.lifebalance.task.model.enums.PriorityLevel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
public class CreateTaskRequest {

    @NotBlank
    private String taskName;

    private String description;

    @NotNull
    private PriorityLevel priorityLevel;

    @NotNull
    private LocalDate startDate;

    private LocalDate endDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private DayOfWeekType dayOfWeek;

    private String note;
}