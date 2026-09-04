package com.lifebalance.identity.repository;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lifebalance.identity.model.AuditLog;
import com.lifebalance.identity.model.enums.AuditAction;
import com.lifebalance.identity.model.enums.AuditEntityName;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    Page<AuditLog> findAll(Pageable pageable);

    Page<AuditLog> findByUserId(UUID userId, Pageable pageable);

    Page<AuditLog> findByAction(AuditAction action, Pageable pageable);

    Page<AuditLog> findByEntityNameAndEntityIdOrderByCreatedAtDesc(
            AuditEntityName entityName,
            String entityId,
            Pageable pageable
    );

    @Query("""
            SELECT auditLog
            FROM AuditLog auditLog
            WHERE (:actorId IS NULL OR auditLog.actorId = :actorId)
              AND (:userId IS NULL OR auditLog.userId = :userId)
              AND (:entityName IS NULL OR auditLog.entityName = :entityName)
              AND (:action IS NULL OR auditLog.action = :action)
              AND auditLog.createdAt >= COALESCE(:createdFrom, auditLog.createdAt)
              AND auditLog.createdAt <= COALESCE(:createdTo, auditLog.createdAt)
              AND (:keyword IS NULL
                   OR lower(auditLog.details) LIKE :keyword
                   OR lower(auditLog.actorUsername) LIKE :keyword
                   OR lower(auditLog.entityId) LIKE :keyword)
            """)
    Page<AuditLog> search(
            @Param("actorId") UUID actorId,
            @Param("userId") UUID userId,
            @Param("entityName") AuditEntityName entityName,
            @Param("action") AuditAction action,
            @Param("createdFrom") OffsetDateTime createdFrom,
            @Param("createdTo") OffsetDateTime createdTo,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
            SELECT auditLog.action, count(auditLog)
            FROM AuditLog auditLog
            WHERE auditLog.createdAt >= COALESCE(:createdFrom, auditLog.createdAt)
              AND auditLog.createdAt <= COALESCE(:createdTo, auditLog.createdAt)
            GROUP BY auditLog.action
            """)
    java.util.List<Object[]> countByAction(
            @Param("createdFrom") OffsetDateTime createdFrom,
            @Param("createdTo") OffsetDateTime createdTo
    );

    @Query("""
            SELECT auditLog.action, count(auditLog)
            FROM AuditLog auditLog
            WHERE auditLog.entityName IN :entityNames
              AND auditLog.action IN :actions
              AND auditLog.createdAt >= COALESCE(:createdFrom, auditLog.createdAt)
              AND auditLog.createdAt <= COALESCE(:createdTo, auditLog.createdAt)
            GROUP BY auditLog.action
            """)
    java.util.List<Object[]> countByActionForEntities(
            @Param("entityNames") Collection<AuditEntityName> entityNames,
            @Param("actions") Collection<AuditAction> actions,
            @Param("createdFrom") OffsetDateTime createdFrom,
            @Param("createdTo") OffsetDateTime createdTo
    );

    @Query("""
            SELECT auditLog.entityName, count(auditLog)
            FROM AuditLog auditLog
            WHERE auditLog.createdAt >= COALESCE(:createdFrom, auditLog.createdAt)
              AND auditLog.createdAt <= COALESCE(:createdTo, auditLog.createdAt)
            GROUP BY auditLog.entityName
            """)
    java.util.List<Object[]> countByEntityName(
            @Param("createdFrom") OffsetDateTime createdFrom,
            @Param("createdTo") OffsetDateTime createdTo
    );
}
