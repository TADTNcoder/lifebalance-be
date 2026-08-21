package com.lifebalance.timeline.repository;

import com.lifebalance.timeline.domain.TimelineTask;
import com.lifebalance.timeline.domain.TimelineTaskStatus;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TimelineTaskRepository extends JpaRepository<TimelineTask, UUID> {

    Optional<TimelineTask> findByIdAndOwnerId(UUID id, UUID ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT task
            FROM TimelineTask task
            WHERE task.id = :taskId
              AND task.ownerId = :ownerId
            """)
    Optional<TimelineTask> findByIdAndOwnerIdForUpdate(
            @Param("taskId") UUID taskId,
            @Param("ownerId") UUID ownerId
    );

    @Query("""
            SELECT task
            FROM TimelineTask task
            WHERE task.ownerId = :ownerId
              AND task.taskStatus IN :statuses
              AND (task.hasTimeCapital = true OR task.estimatedMinutes IS NOT NULL)
            ORDER BY task.deadline ASC NULLS LAST, task.updatedAt DESC, task.id ASC
            """)
    Page<TimelineTask> findEligibleTasks(
            @Param("ownerId") UUID ownerId,
            @Param("statuses") Iterable<TimelineTaskStatus> statuses,
            Pageable pageable
    );
}
