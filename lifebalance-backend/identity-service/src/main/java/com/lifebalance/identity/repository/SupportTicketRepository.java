package com.lifebalance.identity.repository;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lifebalance.identity.model.SupportTicket;
import com.lifebalance.identity.model.enums.SupportTicketCategory;
import com.lifebalance.identity.model.enums.SupportTicketPriority;
import com.lifebalance.identity.model.enums.SupportTicketStatus;

import jakarta.persistence.LockModeType;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT ticket
            FROM SupportTicket ticket
            JOIN FETCH ticket.requester
            LEFT JOIN FETCH ticket.assignee
            WHERE ticket.id = :id
            """)
    Optional<SupportTicket> findByIdForUpdate(@Param("id") UUID id);

    @Query("""
            SELECT ticket
            FROM SupportTicket ticket
            JOIN FETCH ticket.requester
            LEFT JOIN FETCH ticket.assignee
            WHERE ticket.id = :id
            """)
    Optional<SupportTicket> findDetailById(@Param("id") UUID id);

    @Query(value = """
            SELECT ticket
            FROM SupportTicket ticket
            JOIN FETCH ticket.requester
            LEFT JOIN FETCH ticket.assignee
            WHERE (:requesterId IS NULL OR ticket.requester.id = :requesterId)
              AND (:assigneeId IS NULL OR ticket.assignee.id = :assigneeId)
              AND (:status IS NULL OR ticket.status = :status)
              AND (:priority IS NULL OR ticket.priority = :priority)
              AND (:category IS NULL OR ticket.category = :category)
              AND (:createdFrom IS NULL OR ticket.createdAt >= :createdFrom)
              AND (:createdTo IS NULL OR ticket.createdAt <= :createdTo)
              AND (:keyword IS NULL
                   OR lower(ticket.ticketNumber) LIKE :keyword
                   OR lower(ticket.title) LIKE :keyword
                   OR lower(ticket.description) LIKE :keyword)
            """,
            countQuery = """
            SELECT count(ticket)
            FROM SupportTicket ticket
            WHERE (:requesterId IS NULL OR ticket.requester.id = :requesterId)
              AND (:assigneeId IS NULL OR ticket.assignee.id = :assigneeId)
              AND (:status IS NULL OR ticket.status = :status)
              AND (:priority IS NULL OR ticket.priority = :priority)
              AND (:category IS NULL OR ticket.category = :category)
              AND (:createdFrom IS NULL OR ticket.createdAt >= :createdFrom)
              AND (:createdTo IS NULL OR ticket.createdAt <= :createdTo)
              AND (:keyword IS NULL
                   OR lower(ticket.ticketNumber) LIKE :keyword
                   OR lower(ticket.title) LIKE :keyword
                   OR lower(ticket.description) LIKE :keyword)
            """)
    Page<SupportTicket> search(
            @Param("requesterId") UUID requesterId,
            @Param("assigneeId") UUID assigneeId,
            @Param("status") SupportTicketStatus status,
            @Param("priority") SupportTicketPriority priority,
            @Param("category") SupportTicketCategory category,
            @Param("createdFrom") OffsetDateTime createdFrom,
            @Param("createdTo") OffsetDateTime createdTo,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
            SELECT ticket.status, count(ticket)
            FROM SupportTicket ticket
            WHERE (:createdFrom IS NULL OR ticket.createdAt >= :createdFrom)
              AND (:createdTo IS NULL OR ticket.createdAt <= :createdTo)
            GROUP BY ticket.status
            """)
    List<Object[]> countByStatus(
            @Param("createdFrom") OffsetDateTime createdFrom,
            @Param("createdTo") OffsetDateTime createdTo
    );

    @Query("""
            SELECT ticket.priority, count(ticket)
            FROM SupportTicket ticket
            WHERE (:createdFrom IS NULL OR ticket.createdAt >= :createdFrom)
              AND (:createdTo IS NULL OR ticket.createdAt <= :createdTo)
            GROUP BY ticket.priority
            """)
    List<Object[]> countByPriority(
            @Param("createdFrom") OffsetDateTime createdFrom,
            @Param("createdTo") OffsetDateTime createdTo
    );

    @Query("""
            SELECT ticket.category, count(ticket)
            FROM SupportTicket ticket
            WHERE (:createdFrom IS NULL OR ticket.createdAt >= :createdFrom)
              AND (:createdTo IS NULL OR ticket.createdAt <= :createdTo)
            GROUP BY ticket.category
            """)
    List<Object[]> countByCategory(
            @Param("createdFrom") OffsetDateTime createdFrom,
            @Param("createdTo") OffsetDateTime createdTo
    );

    long countByStatusIn(Collection<SupportTicketStatus> statuses);

    long countByAssigneeIsNullAndStatusIn(Collection<SupportTicketStatus> statuses);

    boolean existsByTicketNumber(String ticketNumber);
}
