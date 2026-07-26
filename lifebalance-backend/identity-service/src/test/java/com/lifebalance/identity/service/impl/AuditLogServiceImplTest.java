package com.lifebalance.identity.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lifebalance.identity.model.AuditLog;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.model.enums.AuditAction;
import com.lifebalance.identity.model.enums.AuditEntityName;
import com.lifebalance.identity.model.enums.AuditStatus;
import com.lifebalance.identity.repository.AuditLogRepository;
import com.lifebalance.identity.service.AuditLogCommand;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Test
    void shouldSaveAuditLogSuccessfullyWithDefaultValues() {
        User user = new User();
        UUID userId = UUID.randomUUID();
        user.setId(userId);
        user.setKeycloakId("kc-123");
        AuditLogServiceImpl auditLogService = new AuditLogServiceImpl(auditLogRepository);

        auditLogService.saveAudit(user, AuditAction.LOGIN, AuditStatus.SUCCESS, null, null, "User logged in");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog saved = captor.getValue();

        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getKeycloakId()).isEqualTo("kc-123");
        assertThat(saved.getAction()).isEqualTo(AuditAction.LOGIN);
        assertThat(saved.getStatus()).isEqualTo(AuditStatus.SUCCESS);
        assertThat(saved.getIpAddress()).isEqualTo("unknown");
        assertThat(saved.getUserAgent()).isEqualTo("unknown");
        assertThat(saved.getDetails()).isEqualTo("User logged in");
    }

    @Test
    void shouldSaveGenericAuditLogCommand() {
        UUID actorId = UUID.randomUUID();
        AuditLogServiceImpl service = new AuditLogServiceImpl(auditLogRepository);

        service.saveAudit(new AuditLogCommand(
                AuditEntityName.ROLE,
                "role-1",
                actorId,
                "kc-admin",
                "admin",
                null,
                null,
                AuditAction.CREATE_ROLE,
                AuditStatus.SUCCESS,
                "127.0.0.1",
                "JUnit",
                null,
                "new-role",
                "Role created"
        ));

        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());
        AuditLog auditLog = auditLogCaptor.getValue();
        assertThat(auditLog.getEntityName()).isEqualTo(AuditEntityName.ROLE);
        assertThat(auditLog.getEntityId()).isEqualTo("role-1");
        assertThat(auditLog.getActorId()).isEqualTo(actorId);
        assertThat(auditLog.getActorKeycloakId()).isEqualTo("kc-admin");
        assertThat(auditLog.getActorUsername()).isEqualTo("admin");
        assertThat(auditLog.getAction()).isEqualTo(AuditAction.CREATE_ROLE);
        assertThat(auditLog.getStatus()).isEqualTo(AuditStatus.SUCCESS);
        assertThat(auditLog.getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(auditLog.getUserAgent()).isEqualTo("JUnit");
        assertThat(auditLog.getNewValue()).isEqualTo("new-role");
        assertThat(auditLog.getDetails()).isEqualTo("Role created");
    }
}
