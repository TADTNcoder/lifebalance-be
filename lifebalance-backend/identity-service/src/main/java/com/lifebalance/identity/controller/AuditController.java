package com.lifebalance.identity.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lifebalance.common.web.PageableLimits;
import com.lifebalance.identity.config.OpenApiConfig;
import com.lifebalance.identity.model.AuditLog;
import com.lifebalance.identity.model.enums.AuditAction;
import com.lifebalance.identity.service.AuditLogService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

@Tag(name = "Audit", description = "Audit Log APIs")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping({"/audit-logs", "/api/audit-logs"})
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'audit:read')")
    public Page<AuditLog> getAll(Pageable pageable) {
        return auditLogService.getAll(PageableLimits.normalize(pageable));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'audit:read')")
    public Page<AuditLog> getByUser(
            @PathVariable UUID userId,
            Pageable pageable) {

        return auditLogService.getByUser(
                userId,
                PageableLimits.normalize(pageable));
    }

    @GetMapping("/action/{action}")
    @PreAuthorize("@permissionEvaluationService.hasPermission(authentication, 'audit:read')")
    public Page<AuditLog> getByAction(
            @PathVariable AuditAction action,
            Pageable pageable) {

        return auditLogService.getByAction(
                action,
                PageableLimits.normalize(pageable));
    }
}
