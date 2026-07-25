package com.lifebalance.identity.service.impl;

import org.springframework.stereotype.Service;

import com.lifebalance.identity.model.AuditLog;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.model.enums.AuditAction;
import com.lifebalance.identity.model.enums.AuditEntityName;
import com.lifebalance.identity.model.enums.AuditStatus;
import com.lifebalance.identity.repository.AuditLogRepository;
import com.lifebalance.identity.service.AuditLogService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void saveAudit(
            User user,
            AuditAction action,
            AuditStatus status,
            String ipAddress,
            String userAgent,
            String details) {

        AuditLog auditLog = AuditLog.builder()
                .entityName(AuditEntityName.AUTHENTICATION)
                .entityId(user.getId().toString())
                .actorId(user.getId())
                .actorKeycloakId(user.getKeycloakId())
                .actorUsername(user.getUsername())
                .userId(user.getId())
                .keycloakId(user.getKeycloakId())
                .action(action)
                .status(status)
                .ipAddress(ipAddress == null ? "unknown" : ipAddress)
                .userAgent(userAgent == null ? "unknown" : userAgent)
                .details(details)
                .build();

        auditLogRepository.save(auditLog);
    }

}
