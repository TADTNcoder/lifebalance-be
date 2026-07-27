package com.lifebalance.identity.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.lifebalance.identity.model.AuditLog;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.model.enums.AuditAction;
import com.lifebalance.identity.model.enums.AuditStatus;

public interface AuditLogService {

    void saveAudit(
            User user,
            AuditAction action,
            AuditStatus status,
            String ipAddress,
            String userAgent,
            String details);

    void saveAudit(AuditLogCommand command);

    List<AuditLog> getByUser(UUID userId);

    List<AuditLog> getByAction(AuditAction action);

    Page<AuditLog> getAll(Pageable pageable);
}
