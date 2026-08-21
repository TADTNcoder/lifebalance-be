package com.lifebalance.notification.service;

import com.lifebalance.notification.domain.NotificationChannel;
import com.lifebalance.notification.domain.NotificationEventType;
import com.lifebalance.notification.dto.CreateNotificationTemplateRequest;
import com.lifebalance.notification.dto.NotificationTemplateResponse;
import com.lifebalance.notification.dto.UpdateNotificationTemplateRequest;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationTemplateService {

    NotificationTemplateResponse create(UUID ownerId, CreateNotificationTemplateRequest request);

    NotificationTemplateResponse update(UUID ownerId, UUID templateId, UpdateNotificationTemplateRequest request);

    NotificationTemplateResponse archive(UUID ownerId, UUID templateId);

    NotificationTemplateResponse getById(UUID ownerId, UUID templateId);

    Page<NotificationTemplateResponse> search(
            UUID ownerId,
            NotificationEventType eventType,
            NotificationChannel channel,
            Boolean enabled,
            Pageable pageable
    );
}
