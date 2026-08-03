package com.lifebalance.task.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import com.lifebalance.task.model.enums.DayOfWeekType;
import com.lifebalance.task.model.enums.PriorityLevel;
import com.lifebalance.task.model.enums.TaskStatus;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tasks", schema = "task")
@SQLDelete(sql = """
        UPDATE task.tasks
        SET deleted_at = CURRENT_TIMESTAMP,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = ?
        """)
@SQLRestriction("deleted_at IS NULL")
public class Task extends BaseAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "task_name", nullable = false)
    private String taskName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority_level", nullable = false)
    private PriorityLevel priorityLevel;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private DayOfWeekType dayOfWeek;

    @Column(columnDefinition = "TEXT")
    private String note;
}
