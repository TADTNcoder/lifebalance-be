package com.lifebalance.identity.dto;

import java.time.OffsetDateTime;
import java.util.Map;

import com.lifebalance.identity.model.enums.AccountStatus;
import com.lifebalance.identity.model.enums.ActivityCategory;
import com.lifebalance.identity.model.enums.AuditAction;
import com.lifebalance.identity.model.enums.SupportTicketCategory;
import com.lifebalance.identity.model.enums.SupportTicketPriority;
import com.lifebalance.identity.model.enums.SupportTicketStatus;

import lombok.Builder;

@Builder
public record AdministrationDashboardResponse(
        OffsetDateTime periodStart,
        OffsetDateTime periodEnd,
        Map<SupportTicketStatus, Long> ticketsByStatus,
        Map<SupportTicketPriority, Long> ticketsByPriority,
        Map<SupportTicketCategory, Long> ticketsByCategory,
        long openTicketCount,
        long unassignedTicketCount,
        Map<AccountStatus, Long> accountsByStatus,
        Map<ActivityCategory, Long> activityByCategory,
        Map<AuditAction, Long> auditByAction,
        long roleCount,
        long permissionCount,
        MaintenanceStatusResponse maintenanceStatus
) {
}
