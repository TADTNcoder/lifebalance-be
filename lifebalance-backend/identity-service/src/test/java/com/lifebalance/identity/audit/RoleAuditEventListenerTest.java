package com.lifebalance.identity.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lifebalance.identity.model.enums.AuditAction;
import com.lifebalance.identity.model.enums.AuditEntityName;
import com.lifebalance.identity.model.enums.AuditStatus;
import com.lifebalance.identity.service.AuditLogCommand;
import com.lifebalance.identity.service.AuditLogService;

@ExtendWith(MockitoExtension.class)
class RoleAuditEventListenerTest {

    @Mock
    private AuditLogService auditLogService;

    @Test
    void shouldPersistRoleAuditCommandFromEvent() {
        UUID roleId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        RoleAuditEvent event = new RoleAuditEvent(
                AuditAction.UPDATE_ROLE,
                roleId,
                new AuditActor(actorId, "kc-admin", "admin"),
                new AuditRequestMetadata("10.0.0.1", "JUnit"),
                "old-role",
                "new-role",
                "Role updated"
        );
        RoleAuditEventListener listener = new RoleAuditEventListener(auditLogService);

        listener.handle(event);

        ArgumentCaptor<AuditLogCommand> commandCaptor = ArgumentCaptor.forClass(AuditLogCommand.class);
        verify(auditLogService).saveAudit(commandCaptor.capture());
        AuditLogCommand command = commandCaptor.getValue();
        assertThat(command.entityName()).isEqualTo(AuditEntityName.ROLE);
        assertThat(command.entityId()).isEqualTo(roleId.toString());
        assertThat(command.actorId()).isEqualTo(actorId);
        assertThat(command.actorKeycloakId()).isEqualTo("kc-admin");
        assertThat(command.actorUsername()).isEqualTo("admin");
        assertThat(command.action()).isEqualTo(AuditAction.UPDATE_ROLE);
        assertThat(command.status()).isEqualTo(AuditStatus.SUCCESS);
        assertThat(command.ipAddress()).isEqualTo("10.0.0.1");
        assertThat(command.userAgent()).isEqualTo("JUnit");
        assertThat(command.oldValue()).isEqualTo("old-role");
        assertThat(command.newValue()).isEqualTo("new-role");
    }
}
