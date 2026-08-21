package com.lifebalance.task.repository;

import com.lifebalance.task.model.TaskReminder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface TaskReminderRepository extends JpaRepository<TaskReminder, UUID> {

    Optional<TaskReminder> findByIdAndOwnerId(UUID id, UUID ownerId);

    @Query("""
            SELECT reminder
            FROM TaskReminder reminder
            WHERE reminder.ownerId = :ownerId
              AND reminder.task.id = :taskId
            ORDER BY reminder.remindAt ASC, reminder.id ASC
            """)
    Page<TaskReminder> findByOwnerIdAndTaskIdOrderByRemindAtAsc(
            @Param("ownerId") UUID ownerId,
            @Param("taskId") UUID taskId,
            Pageable pageable);

    Page<TaskReminder> findByOwnerIdAndRemindAtBetweenOrderByRemindAtAsc(
            UUID ownerId,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable);
}
