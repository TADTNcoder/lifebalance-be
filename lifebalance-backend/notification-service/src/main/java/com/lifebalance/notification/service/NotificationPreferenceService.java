package com.lifebalance.notification.service;

import com.lifebalance.notification.dto.NotificationPreferenceResponse;
import com.lifebalance.notification.dto.UpdateNotificationPreferenceRequest;
import java.util.List;
import java.util.UUID;

public interface NotificationPreferenceService {

    NotificationPreferenceResponse upsert(UUID ownerId, UpdateNotificationPreferenceRequest request);

    List<NotificationPreferenceResponse> getPreferences(UUID ownerId);
}
