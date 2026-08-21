package com.lifebalance.notification.service.impl;

import com.lifebalance.notification.domain.NotificationChannel;
import com.lifebalance.notification.domain.NotificationEventType;
import com.lifebalance.notification.domain.NotificationHistoryActionType;
import com.lifebalance.notification.domain.NotificationTemplate;
import com.lifebalance.notification.dto.CreateNotificationTemplateRequest;
import com.lifebalance.notification.dto.NotificationTemplateResponse;
import com.lifebalance.notification.dto.UpdateNotificationTemplateRequest;
import com.lifebalance.notification.error.NotificationExceptions;
import com.lifebalance.notification.repository.NotificationTemplateRepository;
import com.lifebalance.notification.service.NotificationTemplateService;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class NotificationTemplateServiceImpl implements NotificationTemplateService {

    private final NotificationTemplateRepository templateRepository;
    private final NotificationHistoryRecorder historyRecorder;
    private final NotificationMapper mapper;

    NotificationTemplateServiceImpl(
            NotificationTemplateRepository templateRepository,
            NotificationHistoryRecorder historyRecorder,
            NotificationMapper mapper
    ) {
        this.templateRepository = templateRepository;
        this.historyRecorder = historyRecorder;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public NotificationTemplateResponse create(UUID ownerId, CreateNotificationTemplateRequest request) {
        Objects.requireNonNull(request, "Create notification template request is required.");
        String templateKey = normalizeTemplateKey(request.templateKey());
        if (templateRepository.existsByOwnerIdAndTemplateKeyAndChannel(ownerId, templateKey, request.channel())) {
            throw NotificationExceptions.templateAlreadyExists(templateKey);
        }
        NotificationTemplate template = NotificationTemplate.create(
                ownerId,
                ownerId,
                templateKey,
                request.eventType(),
                request.channel(),
                request.titleTemplate(),
                request.messageTemplate(),
                request.enabled() == null || request.enabled()
        );
        template = templateRepository.save(template);
        historyRecorder.record(
                ownerId,
                ownerId,
                NotificationHistoryActionType.TEMPLATE_CREATED,
                null,
                null,
                mapper.templateSnapshot(template),
                null
        );
        return mapper.toTemplateResponse(template);
    }

    @Override
    @Transactional
    public NotificationTemplateResponse update(UUID ownerId, UUID templateId, UpdateNotificationTemplateRequest request) {
        Objects.requireNonNull(request, "Update notification template request is required.");
        NotificationTemplate template = findOwned(ownerId, templateId);
        String oldSnapshot = mapper.templateSnapshot(template);
        template.update(
                ownerId,
                request.titleTemplate(),
                request.messageTemplate(),
                request.enabled() == null || request.enabled()
        );
        template = templateRepository.save(template);
        historyRecorder.record(
                ownerId,
                ownerId,
                NotificationHistoryActionType.TEMPLATE_UPDATED,
                null,
                oldSnapshot,
                mapper.templateSnapshot(template),
                null
        );
        return mapper.toTemplateResponse(template);
    }

    @Override
    @Transactional
    public NotificationTemplateResponse archive(UUID ownerId, UUID templateId) {
        NotificationTemplate template = findOwned(ownerId, templateId);
        String oldSnapshot = mapper.templateSnapshot(template);
        template.archive(ownerId);
        template = templateRepository.save(template);
        historyRecorder.record(
                ownerId,
                ownerId,
                NotificationHistoryActionType.TEMPLATE_ARCHIVED,
                null,
                oldSnapshot,
                mapper.templateSnapshot(template),
                null
        );
        return mapper.toTemplateResponse(template);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationTemplateResponse getById(UUID ownerId, UUID templateId) {
        return mapper.toTemplateResponse(findOwned(ownerId, templateId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationTemplateResponse> search(
            UUID ownerId,
            NotificationEventType eventType,
            NotificationChannel channel,
            Boolean enabled,
            Pageable pageable
    ) {
        return templateRepository.search(ownerId, eventType, channel, enabled, pageable)
                .map(mapper::toTemplateResponse);
    }

    private NotificationTemplate findOwned(UUID ownerId, UUID templateId) {
        return templateRepository.findByIdAndOwnerId(templateId, ownerId)
                .orElseThrow(() -> NotificationExceptions.templateNotFound(templateId));
    }

    private static String normalizeTemplateKey(String templateKey) {
        if (templateKey == null || templateKey.isBlank()) {
            throw NotificationExceptions.invalidRequest("templateKey is required.");
        }
        String normalized = templateKey.trim();
        if (normalized.length() > 120) {
            throw NotificationExceptions.textTooLong(120);
        }
        return normalized;
    }
}
