package com.lifebalance.finance.repository;

import com.lifebalance.finance.domain.FinanceHistory;
import com.lifebalance.finance.domain.FinanceReferenceType;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinanceHistoryRepository extends JpaRepository<FinanceHistory, UUID> {

    Page<FinanceHistory> findByOwnerIdOrderByOccurredAtDesc(UUID ownerId, Pageable pageable);

    Page<FinanceHistory> findByOwnerIdAndReferenceTypeAndReferenceIdOrderByOccurredAtDesc(
            UUID ownerId,
            FinanceReferenceType referenceType,
            UUID referenceId,
            Pageable pageable);
}
