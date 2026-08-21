package com.lifebalance.notification.repository;

import com.lifebalance.notification.domain.NotificationChannel;
import com.lifebalance.notification.domain.NotificationDeliveryStatus;
import com.lifebalance.notification.domain.NotificationEventType;
import com.lifebalance.notification.domain.NotificationRecord;
import com.lifebalance.notification.domain.NotificationStatus;
import jakarta.persistence.LockModeType;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<NotificationRecord, UUID> {

    Optional<NotificationRecord> findByIdAndOwnerId(UUID id, UUID ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT notification
            FROM NotificationRecord notification
            WHERE notification.id = :notificationId
              AND notification.ownerId = :ownerId
            """)
    Optional<NotificationRecord> findByIdAndOwnerIdForUpdate(
            @Param("notificationId") UUID notificationId,
            @Param("ownerId") UUID ownerId
    );

    @Query("""
            SELECT notification
            FROM NotificationRecord notification
            WHERE notification.ownerId = :ownerId
              AND (:status IS NULL OR notification.notificationStatus = :status)
              AND (:eventType IS NULL OR notification.eventType = :eventType)
              AND (:channel IS NULL OR notification.channel = :channel)
              AND (:deliveryStatus IS NULL OR notification.deliveryStatus = :deliveryStatus)
              AND (:referenceType IS NULL OR notification.referenceType = :referenceType)
              AND (:referenceId IS NULL OR notification.referenceId = :referenceId)
              AND (:from IS NULL OR notification.createdAt >= :from)
              AND (:to IS NULL OR notification.createdAt <= :to)
            ORDER BY notification.createdAt DESC, notification.id DESC
            """)
    Page<NotificationRecord> search(
            @Param("ownerId") UUID ownerId,
            @Param("status") NotificationStatus status,
            @Param("eventType") NotificationEventType eventType,
            @Param("channel") NotificationChannel channel,
            @Param("deliveryStatus") NotificationDeliveryStatus deliveryStatus,
            @Param("referenceType") String referenceType,
            @Param("referenceId") UUID referenceId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to,
            Pageable pageable
    );

    @Query("""
            SELECT notification
            FROM NotificationRecord notification
            WHERE notification.ownerId = :ownerId
              AND notification.deliveryStatus = com.lifebalance.notification.domain.NotificationDeliveryStatus.PENDING
              AND (:channel IS NULL OR notification.channel = :channel)
              AND (notification.scheduledAt IS NULL OR notification.scheduledAt <= :dueAt)
            ORDER BY notification.createdAt ASC, notification.id ASC
            """)
    Page<NotificationRecord> findPendingForOwner(
            @Param("ownerId") UUID ownerId,
            @Param("channel") NotificationChannel channel,
            @Param("dueAt") OffsetDateTime dueAt,
            Pageable pageable
    );

    long countByOwnerIdAndNotificationStatus(UUID ownerId, NotificationStatus notificationStatus);
}
