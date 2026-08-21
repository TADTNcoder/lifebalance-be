package com.lifebalance.task.repository;

import com.lifebalance.task.model.TimelinePlacement;
import com.lifebalance.task.model.enums.TimelinePlacementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimelinePlacementRepository extends JpaRepository<TimelinePlacement, UUID> {

    Optional<TimelinePlacement> findByIdAndOwnerId(UUID id, UUID ownerId);

    @Query("""
            SELECT placement
            FROM TimelinePlacement placement
            WHERE placement.ownerId = :ownerId
              AND placement.status = :status
              AND placement.task.status <> com.lifebalance.task.model.enums.TaskStatus.ARCHIVED
              AND placement.task.status <> com.lifebalance.task.model.enums.TaskStatus.CANCELLED
              AND placement.startAt < :to
              AND placement.endAt > :from
            ORDER BY placement.startAt ASC, placement.id ASC
            """)
    Page<TimelinePlacement> findActiveTimeline(
            @Param("ownerId") UUID ownerId,
            @Param("status") TimelinePlacementStatus status,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to,
            Pageable pageable);

    @Query("""
            SELECT CASE WHEN COUNT(placement) > 0 THEN true ELSE false END
            FROM TimelinePlacement placement
            WHERE placement.ownerId = :ownerId
              AND placement.status = :status
              AND (:excludedPlacementId IS NULL OR placement.id <> :excludedPlacementId)
              AND placement.startAt < :endAt
              AND placement.endAt > :startAt
            """)
    boolean existsOverlappingPlacement(
            @Param("ownerId") UUID ownerId,
            @Param("status") TimelinePlacementStatus status,
            @Param("excludedPlacementId") UUID excludedPlacementId,
            @Param("startAt") OffsetDateTime startAt,
            @Param("endAt") OffsetDateTime endAt);

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
            @Param("status") TimelinePlacementStatus status);
}
