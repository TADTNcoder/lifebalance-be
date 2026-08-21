package com.lifebalance.notification.service.impl;

import com.lifebalance.notification.domain.NotificationHistory;
import com.lifebalance.notification.domain.NotificationPreference;
import com.lifebalance.notification.domain.NotificationRecord;
import com.lifebalance.notification.domain.NotificationTemplate;
import com.lifebalance.notification.dto.NotificationHistoryResponse;
import com.lifebalance.notification.dto.NotificationPreferenceResponse;
import com.lifebalance.notification.dto.NotificationResponse;
import com.lifebalance.notification.dto.NotificationTemplateResponse;
import org.springframework.stereotype.Component;

@Component
class NotificationMapper {

    NotificationResponse toResponse(NotificationRecord notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getOwnerId(),
                notification.getRecipientId(),
                notification.getActorId(),
                notification.getEventType(),
                notification.getChannel(),
                notification.getPriority(),
                notification.getNotificationStatus(),
                notification.getDeliveryStatus(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getReferenceType(),
                notification.getReferenceId(),
                notification.getPurpose(),
                notification.getScheduledAt(),
                notification.getSentAt(),
                notification.getReadAt(),
                notification.getArchivedAt(),
                notification.getFailedAt(),
                notification.getFailureReason(),
                notification.getProviderMessageId(),
                notification.getRetryCount(),
                notification.getCreatedBy(),
                notification.getUpdatedBy(),
                notification.getCreatedAt(),
                notification.getUpdatedAt()
        );
    }

    NotificationPreferenceResponse toPreferenceResponse(NotificationPreference preference) {
        return new NotificationPreferenceResponse(
                preference.getId(),
                preference.getOwnerId(),
                preference.getEventType(),
                preference.getChannel(),
                preference.isEnabled(),
                preference.getQuietHoursStart(),
                preference.getQuietHoursEnd(),
                preference.getTimezone(),
                preference.getCreatedBy(),
                preference.getUpdatedBy(),
                preference.getCreatedAt(),
                preference.getUpdatedAt()
        );
    }

    NotificationTemplateResponse toTemplateResponse(NotificationTemplate template) {
        return new NotificationTemplateResponse(
                template.getId(),
                template.getOwnerId(),
                template.getTemplateKey(),
                template.getEventType(),
                template.getChannel(),
                template.getTitleTemplate(),
                template.getMessageTemplate(),
                template.isEnabled(),
                template.getCreatedBy(),
                template.getUpdatedBy(),
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }

    NotificationHistoryResponse toHistoryResponse(NotificationHistory history) {
        NotificationRecord notification = history.getNotification();
        return new NotificationHistoryResponse(
                history.getId(),
                history.getOwnerId(),
                history.getActorId(),
                history.getActionType(),
                notification == null ? null : notification.getId(),
                history.getOldValue(),
                history.getNewValue(),
                history.getReason(),
                history.getOccurredAt()
        );
    }

    String notificationSnapshot(NotificationRecord notification) {
        return "notificationId=" + notification.getId()
                + ";ownerId=" + notification.getOwnerId()
                + ";recipientId=" + notification.getRecipientId()
                + ";eventType=" + notification.getEventType()
                + ";channel=" + notification.getChannel()
                + ";status=" + notification.getNotificationStatus()
                + ";deliveryStatus=" + notification.getDeliveryStatus()
                + ";referenceType=" + notification.getReferenceType()
                + ";referenceId=" + notification.getReferenceId()
                + ";scheduledAt=" + notification.getScheduledAt()
                + ";sentAt=" + notification.getSentAt()
                + ";readAt=" + notification.getReadAt()
                + ";archivedAt=" + notification.getArchivedAt()
                + ";failedAt=" + notification.getFailedAt()
                + ";retryCount=" + notification.getRetryCount();
    }

    String preferenceSnapshot(NotificationPreference preference) {
        return "preferenceId=" + preference.getId()
                + ";ownerId=" + preference.getOwnerId()
                + ";eventType=" + preference.getEventType()
                + ";channel=" + preference.getChannel()
                + ";enabled=" + preference.isEnabled()
                + ";quietHoursStart=" + preference.getQuietHoursStart()
                + ";quietHoursEnd=" + preference.getQuietHoursEnd()
                + ";timezone=" + preference.getTimezone();
    }

    String templateSnapshot(NotificationTemplate template) {
        return "templateId=" + template.getId()
                + ";ownerId=" + template.getOwnerId()
                + ";templateKey=" + template.getTemplateKey()
                + ";eventType=" + template.getEventType()
                + ";channel=" + template.getChannel()
                + ";enabled=" + template.isEnabled();
    }
}
