package com.lifebalance.identity.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.lifebalance.identity.model.AuditLog;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.model.enums.AuditAction;
import com.lifebalance.identity.model.enums.AuditEntityName;
import com.lifebalance.identity.model.enums.AuditStatus;
import com.lifebalance.identity.repository.AuditLogRepository;
import com.lifebalance.identity.service.AuditLogCommand;
import com.lifebalance.identity.service.AuditLogService;

import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAudit(AuditLogCommand command) {
        AuditLog auditLog = AuditLog.builder()
                .entityName(command.entityName())
                .entityId(command.entityId())
                .actorId(command.actorId())
                .actorKeycloakId(command.actorKeycloakId())
                .actorUsername(command.actorUsername())
                .userId(command.userId())
                .keycloakId(command.keycloakId())
                .action(command.action())
                .status(command.status())
                .ipAddress(command.ipAddress() == null ? "unknown" : command.ipAddress())
                .userAgent(command.userAgent() == null ? "unknown" : command.userAgent())
                .oldValue(command.oldValue())
                .newValue(command.newValue())
                .details(command.details())
                .build();

        auditLogRepository.save(auditLog);
    }

    @Override
    public List<AuditLog> getByUser(UUID userId) {
        return auditLogRepository.findByUserId(userId);
    }

    @Override
    public List<AuditLog> getByAction(AuditAction action) {
        return auditLogRepository.findByAction(action);
    }

    @Override
    public Page<AuditLog> getAll(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }
}
