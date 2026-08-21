package com.lifebalance.notification.repository;

import com.lifebalance.notification.domain.NotificationChannel;
import com.lifebalance.notification.domain.NotificationEventType;
import com.lifebalance.notification.domain.NotificationPreference;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, UUID> {

    Optional<NotificationPreference> findByIdAndOwnerId(UUID id, UUID ownerId);

    Optional<NotificationPreference> findByOwnerIdAndEventTypeAndChannel(
            UUID ownerId,
            NotificationEventType eventType,
            NotificationChannel channel
    );

    List<NotificationPreference> findByOwnerIdOrderByEventTypeAscChannelAsc(UUID ownerId);
}
