package com.lifebalance.identity.audit;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.lifebalance.identity.model.enums.AuditAction;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RoleAuditEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final CurrentAuditActorResolver actorResolver;
    private final CurrentAuditRequestMetadataResolver requestMetadataResolver;

    public void publishRoleAudit(
            AuditAction action,
            UUID roleId,
            String oldValue,
            String newValue,
            String details
    ) {
        applicationEventPublisher.publishEvent(new RoleAuditEvent(
                action,
                roleId,
                actorResolver.resolve(),
                requestMetadataResolver.resolve(),
                oldValue,
                newValue,
                details
        ));
    }
}
