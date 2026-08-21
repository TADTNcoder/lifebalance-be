package com.lifebalance.notification.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lifebalance.common.error.AppException;
import com.lifebalance.notification.config.NotificationProperties;
import com.lifebalance.notification.domain.NotificationChannel;
import com.lifebalance.notification.domain.NotificationDeliveryStatus;
import com.lifebalance.notification.domain.NotificationEventType;
import com.lifebalance.notification.domain.NotificationHistoryActionType;
import com.lifebalance.notification.domain.NotificationPreference;
import com.lifebalance.notification.domain.NotificationRecord;
import com.lifebalance.notification.domain.NotificationStatus;
import com.lifebalance.notification.dto.CreateNotificationRequest;
import com.lifebalance.notification.dto.NotificationResponse;
import com.lifebalance.notification.error.NotificationErrorCode;
import com.lifebalance.notification.repository.NotificationPreferenceRepository;
import com.lifebalance.notification.repository.NotificationRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID NOTIFICATION_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationPreferenceRepository preferenceRepository;

    @Mock
    private NotificationDeliveryAttemptRecorder deliveryAttemptRecorder;

    @Mock
    private NotificationHistoryRecorder historyRecorder;

    @Test
    void createInAppNotificationMarksDeliverySentAndRecordsHistory() {
        when(preferenceRepository.findByOwnerIdAndEventTypeAndChannel(
                OWNER_ID,
                NotificationEventType.TASK_REMINDER,
                NotificationChannel.IN_APP
        )).thenReturn(Optional.empty());
        when(notificationRepository.save(any(NotificationRecord.class))).thenAnswer(invocation -> {
            NotificationRecord notification = invocation.getArgument(0);
            ReflectionTestUtils.setField(notification, "id", NOTIFICATION_ID);
            return notification;
        });

        List<NotificationResponse> responses = createService().create(OWNER_ID, request(Set.of(NotificationChannel.IN_APP)));

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().status()).isEqualTo(NotificationStatus.UNREAD);
        assertThat(responses.getFirst().deliveryStatus()).isEqualTo(NotificationDeliveryStatus.SENT);
        ArgumentCaptor<NotificationRecord> captor = ArgumentCaptor.forClass(NotificationRecord.class);
        verify(notificationRepository).save(captor.capture());
        verify(deliveryAttemptRecorder).record(
                captor.getValue(),
                NotificationDeliveryStatus.SENT,
                null,
                null
        );
        verify(historyRecorder).record(
                eq(OWNER_ID),
                eq(OWNER_ID),
                eq(NotificationHistoryActionType.NOTIFICATION_CREATED),
                eq(captor.getValue()),
                isNull(),
                org.mockito.ArgumentMatchers.contains("deliveryStatus=SENT"),
                eq("User opted in")
        );
        verify(historyRecorder).record(
                eq(OWNER_ID),
                eq(OWNER_ID),
                eq(NotificationHistoryActionType.NOTIFICATION_SENT),
                eq(captor.getValue()),
                isNull(),
                org.mockito.ArgumentMatchers.contains("deliveryStatus=SENT"),
                eq("User opted in")
        );
    }

    @Test
    void createExternalNotificationQueuesDelivery() {
        when(preferenceRepository.findByOwnerIdAndEventTypeAndChannel(
                OWNER_ID,
                NotificationEventType.TASK_REMINDER,
                NotificationChannel.EMAIL
        )).thenReturn(Optional.empty());
        when(notificationRepository.save(any(NotificationRecord.class))).thenAnswer(invocation -> {
            NotificationRecord notification = invocation.getArgument(0);
            ReflectionTestUtils.setField(notification, "id", NOTIFICATION_ID);
            return notification;
        });

        List<NotificationResponse> responses = createService().create(OWNER_ID, request(Set.of(NotificationChannel.EMAIL)));

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().deliveryStatus()).isEqualTo(NotificationDeliveryStatus.PENDING);
        verify(deliveryAttemptRecorder).record(
                any(NotificationRecord.class),
                eq(NotificationDeliveryStatus.PENDING),
                isNull(),
                isNull()
        );
        verify(historyRecorder).record(
                eq(OWNER_ID),
                eq(OWNER_ID),
                eq(NotificationHistoryActionType.NOTIFICATION_QUEUED),
                any(NotificationRecord.class),
                isNull(),
                org.mockito.ArgumentMatchers.contains("deliveryStatus=PENDING"),
                eq("User opted in")
        );
    }

    @Test
    void createSkipsDeliveryWhenPreferenceDisabled() {
        NotificationPreference disabledPreference = NotificationPreference.create(
                OWNER_ID,
                OWNER_ID,
                NotificationEventType.TASK_REMINDER,
                NotificationChannel.PUSH,
                false,
                null,
                null,
                null
        );
        when(preferenceRepository.findByOwnerIdAndEventTypeAndChannel(
                OWNER_ID,
                NotificationEventType.TASK_REMINDER,
                NotificationChannel.PUSH
        )).thenReturn(Optional.of(disabledPreference));
        when(notificationRepository.save(any(NotificationRecord.class))).thenAnswer(invocation -> {
            NotificationRecord notification = invocation.getArgument(0);
            ReflectionTestUtils.setField(notification, "id", NOTIFICATION_ID);
            return notification;
        });

        List<NotificationResponse> responses = createService().create(OWNER_ID, request(Set.of(NotificationChannel.PUSH)));

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().status()).isEqualTo(NotificationStatus.ARCHIVED);
        assertThat(responses.getFirst().deliveryStatus()).isEqualTo(NotificationDeliveryStatus.SKIPPED);
        verify(historyRecorder).record(
                eq(OWNER_ID),
                eq(OWNER_ID),
                eq(NotificationHistoryActionType.NOTIFICATION_SKIPPED),
                any(NotificationRecord.class),
                isNull(),
                org.mockito.ArgumentMatchers.contains("deliveryStatus=SKIPPED"),
                eq("User opted in")
        );
    }

    @Test
    void createRejectsWithoutPolicyApprovalBeforeSaving() {
        assertThatThrownBy(() -> createService().create(OWNER_ID, requestWithoutPolicyApproval()))
                .isInstanceOf(AppException.class)
                .extracting("code")
                .isEqualTo(NotificationErrorCode.NOTIFICATION_POLICY_NOT_APPROVED);

        verify(notificationRepository, never()).save(any());
        verify(deliveryAttemptRecorder, never()).record(any(), any(), any(), any());
        verify(historyRecorder, never()).record(any(), any(), any(), any(), any(), any(), any());
    }

    private NotificationServiceImpl createService() {
        NotificationProperties properties = new NotificationProperties();
        properties.setDefaultChannel(NotificationChannel.IN_APP);
        return new NotificationServiceImpl(
                notificationRepository,
                preferenceRepository,
                deliveryAttemptRecorder,
                historyRecorder,
                new NotificationMapper(),
                properties
        );
    }

    private static CreateNotificationRequest request(Set<NotificationChannel> channels) {
        return new CreateNotificationRequest(
                NotificationEventType.TASK_REMINDER,
                channels,
                null,
                "Task starts soon",
                "Focus block starts at 10:00.",
                null,
                null,
                "Reminder requested by user",
                true,
                null,
                "User opted in"
        );
    }

    private static CreateNotificationRequest requestWithoutPolicyApproval() {
        return new CreateNotificationRequest(
                NotificationEventType.TASK_REMINDER,
                Set.of(NotificationChannel.IN_APP),
                null,
                "Task starts soon",
                "Focus block starts at 10:00.",
                null,
                null,
                "Reminder requested by user",
                false,
                null,
                "User opted in"
        );
    }
}
