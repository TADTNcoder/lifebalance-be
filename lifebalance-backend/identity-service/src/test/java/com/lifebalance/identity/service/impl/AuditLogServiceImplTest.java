package com.lifebalance.identity.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lifebalance.identity.model.AuditLog;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.model.enums.AuditAction;
import com.lifebalance.identity.model.enums.AuditStatus;
import com.lifebalance.identity.repository.AuditLogRepository;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    @Test
    void shouldSaveAuditLogSuccessfullyWithDefaultValues() {
        User user = new User();
        UUID userId = UUID.randomUUID();
        user.setId(userId);
        user.setKeycloakId("kc-123");

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
}