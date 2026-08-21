package com.lifebalance.timeline.repository;

import com.lifebalance.timeline.domain.TimelinePlacement;
import com.lifebalance.timeline.domain.TimelinePlacementStatus;
import jakarta.persistence.LockModeType;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TimelinePlacementRepository extends JpaRepository<TimelinePlacement, UUID> {

    @EntityGraph(attributePaths = "task")
    Optional<TimelinePlacement> findByIdAndOwnerId(UUID id, UUID ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "task")
    @Query("""
            SELECT placement
            FROM TimelinePlacement placement
            WHERE placement.id = :placementId
              AND placement.ownerId = :ownerId
            """)
    Optional<TimelinePlacement> findByIdAndOwnerIdForUpdate(
            @Param("placementId") UUID placementId,
            @Param("ownerId") UUID ownerId
    );

    @EntityGraph(attributePaths = "task")
    @Query("""
            SELECT placement
            FROM TimelinePlacement placement
            WHERE placement.ownerId = :ownerId
              AND (:status IS NULL OR placement.status = :status)
              AND placement.task.taskStatus <> com.lifebalance.timeline.domain.TimelineTaskStatus.ARCHIVED
              AND placement.task.taskStatus <> com.lifebalance.timeline.domain.TimelineTaskStatus.CANCELLED
              AND placement.startAt < :to
              AND placement.endAt > :from
            ORDER BY placement.startAt ASC, placement.id ASC
            """)
    Page<TimelinePlacement> findTimeline(
            @Param("ownerId") UUID ownerId,
            @Param("status") TimelinePlacementStatus status,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "task")
    @Query("""
            SELECT placement
            FROM TimelinePlacement placement
            WHERE placement.ownerId = :ownerId
              AND placement.status = :status
              AND (:excludedPlacementId IS NULL OR placement.id <> :excludedPlacementId)
              AND placement.startAt < :endAt
              AND placement.endAt > :startAt
            ORDER BY placement.startAt ASC, placement.id ASC
            """)
    List<TimelinePlacement> findConflicts(
            @Param("ownerId") UUID ownerId,
            @Param("status") TimelinePlacementStatus status,
            @Param("excludedPlacementId") UUID excludedPlacementId,
            @Param("startAt") OffsetDateTime startAt,
            @Param("endAt") OffsetDateTime endAt
    );

    @EntityGraph(attributePaths = "task")
    @Query("""
            SELECT placement
            FROM TimelinePlacement placement
            WHERE placement.ownerId = :ownerId
              AND placement.task.id = :taskId
              AND placement.status = :status
            ORDER BY placement.startAt ASC, placement.id ASC
            """)
    List<TimelinePlacement> findByOwnerIdAndTaskIdAndStatus(
            @Param("ownerId") UUID ownerId,
            @Param("taskId") UUID taskId,
            @Param("status") TimelinePlacementStatus status
    );
}
