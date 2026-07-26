package com.lifebalance.identity.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.lifebalance.identity.model.enums.AuditEntityName;
import com.lifebalance.identity.model.enums.AuditStatus;
import com.lifebalance.identity.service.AuditLogCommand;
import com.lifebalance.identity.service.AuditLogService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RoleAuditEventListener {

    private static final Logger log = LoggerFactory.getLogger(RoleAuditEventListener.class);

    private final AuditLogService auditLogService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(RoleAuditEvent event) {
        try {
            AuditActor actor = event.actor();
            AuditRequestMetadata requestMetadata = event.requestMetadata();

            auditLogService.saveAudit(new AuditLogCommand(
                    AuditEntityName.ROLE,
                    event.roleId() == null ? null : event.roleId().toString(),
                    actor == null ? null : actor.id(),
                    actor == null ? null : actor.keycloakId(),
                    actor == null ? null : actor.username(),
                    null,
                    null,
                    event.action(),
                    AuditStatus.SUCCESS,
                    requestMetadata == null ? "unknown" : requestMetadata.ipAddress(),
                    requestMetadata == null ? "unknown" : requestMetadata.userAgent(),
                    event.oldValue(),
                    event.newValue(),
                    event.details()
            ));
        } catch (RuntimeException exception) {
            log.warn("Failed to persist role audit event action={} roleId={}", event.action(), event.roleId(), exception);
        }
    }
}
