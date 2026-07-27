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
public class UserAuditEventListener {

    private static final Logger log = LoggerFactory.getLogger(UserAuditEventListener.class);

    private final AuditLogService auditLogService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(UserAuditEvent event) {
        try {
            AuditActor actor = event.actor();
            AuditRequestMetadata requestMetadata = event.requestMetadata();

            auditLogService.saveAudit(new AuditLogCommand(
                    AuditEntityName.USER,
                    event.userId() == null ? null : event.userId().toString(),
                    actor == null ? null : actor.id(),
                    actor == null ? null : actor.keycloakId(),
                    actor == null ? null : actor.username(),
                    event.userId(),
                    event.keycloakId(),
                    event.action(),
                    AuditStatus.SUCCESS,
                    requestMetadata == null ? "unknown" : requestMetadata.ipAddress(),
                    requestMetadata == null ? "unknown" : requestMetadata.userAgent(),
                    event.oldValue(),
                    event.newValue(),
                    event.details()
            ));
        } catch (RuntimeException exception) {
            log.warn("Failed to persist user audit event action={} userId={}", event.action(), event.userId(), exception);
        }
    }
}
