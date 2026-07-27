package com.lifebalance.identity.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lifebalance.identity.model.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

}
