package com.lifebalance.notification.repository;

import com.lifebalance.notification.domain.NotificationDeliveryAttempt;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationDeliveryAttemptRepository extends JpaRepository<NotificationDeliveryAttempt, UUID> {

    @Query("""
            SELECT COUNT(attempt)
            FROM NotificationDeliveryAttempt attempt
            WHERE attempt.notification.id = :notificationId
            """)
    long countByNotificationId(@Param("notificationId") UUID notificationId);
}
