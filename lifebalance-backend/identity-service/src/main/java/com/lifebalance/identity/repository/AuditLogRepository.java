package com.lifebalance.identity.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.lifebalance.identity.model.AuditLog;
import com.lifebalance.identity.model.enums.AuditAction;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findByUserId(UUID userId);

    List<AuditLog> findByAction(AuditAction action);

    Page<AuditLog> findAll(Pageable pageable);
}
