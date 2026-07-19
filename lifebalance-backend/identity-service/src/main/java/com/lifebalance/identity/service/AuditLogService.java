package com.lifebalance.identity.service;

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
}
