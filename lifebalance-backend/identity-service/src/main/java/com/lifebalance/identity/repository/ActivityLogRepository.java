package com.lifebalance.identity.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lifebalance.identity.model.ActivityLog;
import com.lifebalance.identity.model.enums.ActivityCategory;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {

    @Query(value = """
            SELECT logEntry
            FROM ActivityLog logEntry
            LEFT JOIN FETCH logEntry.actor
            WHERE (:actorId IS NULL OR logEntry.actor.id = :actorId)
              AND (:category IS NULL OR logEntry.category = :category)
              AND (COALESCE(:action, '') = '' OR lower(logEntry.action) = :action)
              AND (COALESCE(:entityType, '') = '' OR lower(logEntry.entityType) = :entityType)
              AND (COALESCE(:entityId, '') = '' OR lower(logEntry.entityId) = :entityId)
              AND logEntry.occurredAt >= COALESCE(:occurredFrom, logEntry.occurredAt)
              AND logEntry.occurredAt <= COALESCE(:occurredTo, logEntry.occurredAt)
              AND (:keyword IS NULL
                   OR lower(logEntry.summary) LIKE :keyword
                   OR lower(logEntry.details) LIKE :keyword)
            """,
            countQuery = """
            SELECT count(logEntry)
            FROM ActivityLog logEntry
            WHERE (:actorId IS NULL OR logEntry.actor.id = :actorId)
              AND (:category IS NULL OR logEntry.category = :category)
              AND (COALESCE(:action, '') = '' OR lower(logEntry.action) = :action)
              AND (COALESCE(:entityType, '') = '' OR lower(logEntry.entityType) = :entityType)
              AND (COALESCE(:entityId, '') = '' OR lower(logEntry.entityId) = :entityId)
              AND logEntry.occurredAt >= COALESCE(:occurredFrom, logEntry.occurredAt)
              AND logEntry.occurredAt <= COALESCE(:occurredTo, logEntry.occurredAt)
              AND (:keyword IS NULL
                   OR lower(logEntry.summary) LIKE :keyword
                   OR lower(logEntry.details) LIKE :keyword)
            """)
    Page<ActivityLog> search(
            @Param("actorId") UUID actorId,
            @Param("category") ActivityCategory category,
            @Param("action") String action,
            @Param("entityType") String entityType,
            @Param("entityId") String entityId,
            @Param("occurredFrom") OffsetDateTime occurredFrom,
            @Param("occurredTo") OffsetDateTime occurredTo,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
            SELECT logEntry.category, count(logEntry)
            FROM ActivityLog logEntry
            WHERE logEntry.occurredAt >= COALESCE(:occurredFrom, logEntry.occurredAt)
              AND logEntry.occurredAt <= COALESCE(:occurredTo, logEntry.occurredAt)
            GROUP BY logEntry.category
            """)
    List<Object[]> countByCategory(
            @Param("occurredFrom") OffsetDateTime occurredFrom,
            @Param("occurredTo") OffsetDateTime occurredTo
    );
}
