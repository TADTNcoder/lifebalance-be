package com.lifebalance.identity.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.lifebalance.identity.model.enums.AuditAction;
import com.lifebalance.identity.model.enums.AuditStatus;

import lombok.Data;

@Data
public class AuditResponse {

    private UUID id;

    private UUID userId;

    private String keycloakId;

    private AuditAction action;

    private AuditStatus status;

    private String ipAddress;

    private String userAgent;

    private String details;

    private OffsetDateTime createdAt;
}
