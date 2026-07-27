package com.lifebalance.identity.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lifebalance.identity.model.AuditLog;
import com.lifebalance.identity.model.enums.AuditAction;
import com.lifebalance.identity.service.AuditLogService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Tag(name = "Audit", description = "Audit Log APIs")
@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogService auditLogService;

    @GetMapping
    public Page<AuditLog> getAll(Pageable pageable) {
        return auditLogService.getAll(pageable);
    }

    @GetMapping("/user/{userId}")
    public Page<AuditLog> getByUser(
            @PathVariable UUID userId,
            Pageable pageable) {

        return auditLogService.getByUser(
                userId,
                pageable);
    }

    @GetMapping("/action/{action}")
    public Page<AuditLog> getByAction(
            @PathVariable AuditAction action,
            Pageable pageable) {

        return auditLogService.getByAction(
                action,
                pageable);
    }
}
