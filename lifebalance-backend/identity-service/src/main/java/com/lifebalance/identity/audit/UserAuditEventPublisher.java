package com.lifebalance.identity.audit;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.lifebalance.identity.model.User;
import com.lifebalance.identity.model.enums.AuditAction;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserAuditEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final CurrentAuditActorResolver actorResolver;
    private final CurrentAuditRequestMetadataResolver requestMetadataResolver;
    private final UserAuditSnapshotMapper userAuditSnapshotMapper;

    public void publishUserAudit(
            AuditAction action,
            User oldUser,
            User newUser,
            String details
    ) {
        User subject = newUser == null ? oldUser : newUser;
        applicationEventPublisher.publishEvent(new UserAuditEvent(
                action,
                subject == null ? null : subject.getId(),
                subject == null ? null : subject.getKeycloakId(),
                actorResolver.resolve(),
                requestMetadataResolver.resolve(),
                oldUser == null ? null : userAuditSnapshotMapper.toJson(oldUser),
                newUser == null ? null : userAuditSnapshotMapper.toJson(newUser),
                details
        ));
    }
}
