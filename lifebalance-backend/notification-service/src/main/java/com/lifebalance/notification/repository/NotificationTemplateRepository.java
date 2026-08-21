package com.lifebalance.notification.repository;

import com.lifebalance.notification.domain.NotificationChannel;
import com.lifebalance.notification.domain.NotificationEventType;
import com.lifebalance.notification.domain.NotificationTemplate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID> {

    Optional<NotificationTemplate> findByIdAndOwnerId(UUID id, UUID ownerId);

    Optional<NotificationTemplate> findByOwnerIdAndTemplateKeyAndChannel(
            UUID ownerId,
            String templateKey,
            NotificationChannel channel
    );

    boolean existsByOwnerIdAndTemplateKeyAndChannel(UUID ownerId, String templateKey, NotificationChannel channel);

    @Query("""
            SELECT template
            FROM NotificationTemplate template
            WHERE template.ownerId = :ownerId
              AND (:eventType IS NULL OR template.eventType = :eventType)
              AND (:channel IS NULL OR template.channel = :channel)
              AND (:enabled IS NULL OR template.enabled = :enabled)
            ORDER BY template.templateKey ASC, template.channel ASC
            """)
    Page<NotificationTemplate> search(
            @Param("ownerId") UUID ownerId,
            @Param("eventType") NotificationEventType eventType,
            @Param("channel") NotificationChannel channel,
            @Param("enabled") Boolean enabled,
            Pageable pageable
    );
}
