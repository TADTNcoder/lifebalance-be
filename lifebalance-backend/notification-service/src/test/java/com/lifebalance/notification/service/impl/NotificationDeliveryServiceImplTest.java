package com.lifebalance.notification.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lifebalance.notification.domain.NotificationChannel;
import com.lifebalance.notification.domain.NotificationDeliveryStatus;
import com.lifebalance.notification.domain.NotificationEventType;
import com.lifebalance.notification.domain.NotificationHistoryActionType;
import com.lifebalance.notification.domain.NotificationRecord;
import com.lifebalance.notification.dto.MarkDeliveryFailedRequest;
import com.lifebalance.notification.dto.NotificationResponse;
import com.lifebalance.notification.dto.RetryDeliveryRequest;
import com.lifebalance.notification.repository.NotificationRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationDeliveryServiceImplTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID NOTIFICATION_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationDeliveryAttemptRecorder deliveryAttemptRecorder;

    @Mock
    private NotificationHistoryRecorder historyRecorder;

    @Test
    void markFailedRecordsAttemptAndHistory() {
        NotificationRecord notification = externalNotification();
        when(notificationRepository.findByIdAndOwnerIdForUpdate(NOTIFICATION_ID, OWNER_ID))
                .thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        NotificationResponse response = createService()
                .markFailed(OWNER_ID, NOTIFICATION_ID, new MarkDeliveryFailedRequest("Provider timeout"));

        assertThat(response.deliveryStatus()).isEqualTo(NotificationDeliveryStatus.FAILED);
        assertThat(response.failureReason()).isEqualTo("Provider timeout");
        verify(deliveryAttemptRecorder).record(
                notification,
                NotificationDeliveryStatus.FAILED,
                null,
                "Provider timeout"
        );
        verify(historyRecorder).record(
                eq(OWNER_ID),
                eq(OWNER_ID),
                eq(NotificationHistoryActionType.NOTIFICATION_FAILED),
                eq(notification),
                org.mockito.ArgumentMatchers.contains("deliveryStatus=PENDING"),
                org.mockito.ArgumentMatchers.contains("deliveryStatus=FAILED"),
                eq(null)
        );
    }

    @Test
    void retryMovesFailedNotificationBackToPending() {
        NotificationRecord notification = externalNotification();
        notification.markFailed(OWNER_ID, "Provider timeout");
        when(notificationRepository.findByIdAndOwnerIdForUpdate(NOTIFICATION_ID, OWNER_ID))
                .thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        NotificationResponse response = createService()
                .retry(OWNER_ID, NOTIFICATION_ID, new RetryDeliveryRequest("Retry after provider recovery"));

        assertThat(response.deliveryStatus()).isEqualTo(NotificationDeliveryStatus.PENDING);
        assertThat(response.retryCount()).isEqualTo(1);
        verify(deliveryAttemptRecorder).record(notification, NotificationDeliveryStatus.PENDING, null, null);
        verify(historyRecorder).record(
                eq(OWNER_ID),
                eq(OWNER_ID),
                eq(NotificationHistoryActionType.NOTIFICATION_RETRIED),
                eq(notification),
                org.mockito.ArgumentMatchers.contains("deliveryStatus=FAILED"),
                org.mockito.ArgumentMatchers.contains("deliveryStatus=PENDING"),
                eq("Retry after provider recovery")
        );
    }

    private NotificationDeliveryServiceImpl createService() {
        return new NotificationDeliveryServiceImpl(
                notificationRepository,
                deliveryAttemptRecorder,
                historyRecorder,
                new NotificationMapper()
        );
    }

    private static NotificationRecord externalNotification() {
        NotificationRecord notification = NotificationRecord.create(
                OWNER_ID,
                OWNER_ID,
                OWNER_ID,
                NotificationEventType.TASK_REMINDER,
                NotificationChannel.EMAIL,
                null,
                "Task starts soon",
                "Focus block starts at 10:00.",
                null,
                null,
                "Reminder requested by user",
                null,
                true
        );
        ReflectionTestUtils.setField(notification, "id", NOTIFICATION_ID);
        return notification;
    }
}
