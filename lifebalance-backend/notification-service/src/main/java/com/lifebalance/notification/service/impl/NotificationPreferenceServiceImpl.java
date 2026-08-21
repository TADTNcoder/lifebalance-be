package com.lifebalance.notification.service.impl;

import com.lifebalance.notification.domain.NotificationHistoryActionType;
import com.lifebalance.notification.domain.NotificationPreference;
import com.lifebalance.notification.dto.NotificationPreferenceResponse;
import com.lifebalance.notification.dto.UpdateNotificationPreferenceRequest;
import com.lifebalance.notification.error.NotificationExceptions;
import com.lifebalance.notification.repository.NotificationPreferenceRepository;
import com.lifebalance.notification.service.NotificationPreferenceService;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class NotificationPreferenceServiceImpl implements NotificationPreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;
    private final NotificationHistoryRecorder historyRecorder;
    private final NotificationMapper mapper;

    NotificationPreferenceServiceImpl(
            NotificationPreferenceRepository preferenceRepository,
            NotificationHistoryRecorder historyRecorder,
            NotificationMapper mapper
    ) {
        this.preferenceRepository = preferenceRepository;
        this.historyRecorder = historyRecorder;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public NotificationPreferenceResponse upsert(UUID ownerId, UpdateNotificationPreferenceRequest request) {
        Objects.requireNonNull(request, "Notification preference request is required.");
        if (request.enabled() == null) {
            throw NotificationExceptions.invalidRequest("enabled is required.");
        }

        NotificationPreference preference = preferenceRepository
                .findByOwnerIdAndEventTypeAndChannel(ownerId, request.eventType(), request.channel())
                .orElseGet(() -> NotificationPreference.create(
                        ownerId,
                        ownerId,
                        request.eventType(),
                        request.channel(),
                        request.enabled(),
                        request.quietHoursStart(),
                        request.quietHoursEnd(),
                        request.timezone()
                ));
        String oldSnapshot = preference.getId() == null ? null : mapper.preferenceSnapshot(preference);
        if (preference.getId() != null) {
            preference.update(
                    ownerId,
                    request.enabled(),
                    request.quietHoursStart(),
                    request.quietHoursEnd(),
                    request.timezone()
            );
        }
        preference = preferenceRepository.save(preference);
        historyRecorder.record(
                ownerId,
                ownerId,
                NotificationHistoryActionType.PREFERENCE_UPDATED,
                null,
                oldSnapshot,
                mapper.preferenceSnapshot(preference),
                null
        );
        return mapper.toPreferenceResponse(preference);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationPreferenceResponse> getPreferences(UUID ownerId) {
        return preferenceRepository.findByOwnerIdOrderByEventTypeAscChannelAsc(ownerId)
                .stream()
                .map(mapper::toPreferenceResponse)
                .toList();
    }
}
