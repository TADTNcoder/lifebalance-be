package com.lifebalance.identity.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lifebalance.identity.model.SupportTicketHistory;

public interface SupportTicketHistoryRepository extends JpaRepository<SupportTicketHistory, UUID> {

    List<SupportTicketHistory> findByTicketIdOrderByCreatedAtAsc(UUID ticketId);
}
