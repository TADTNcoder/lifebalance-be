package com.lifebalance.notification.repository;

import com.lifebalance.notification.domain.NotificationHistory;
import com.lifebalance.notification.domain.NotificationHistoryActionType;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, UUID> {

    @EntityGraph(attributePaths = "notification")
    @Query("""
            SELECT history
            FROM NotificationHistory history
            WHERE history.ownerId = :ownerId
              AND (:notificationId IS NULL OR history.notification.id = :notificationId)
              AND (:actionType IS NULL OR history.actionType = :actionType)
            ORDER BY history.occurredAt DESC, history.id DESC
            """)
    Page<NotificationHistory> search(
            @Param("ownerId") UUID ownerId,
            @Param("notificationId") UUID notificationId,
            @Param("actionType") NotificationHistoryActionType actionType,
            Pageable pageable
    );
}
