package com.lifebalance.finance.repository;

import com.lifebalance.finance.domain.FinanceCategory;
import com.lifebalance.finance.domain.FinanceCategoryStatus;
import com.lifebalance.finance.domain.FinanceCategoryType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FinanceCategoryRepository extends JpaRepository<FinanceCategory, UUID> {

    Optional<FinanceCategory> findByIdAndOwnerId(UUID id, UUID ownerId);

    boolean existsByOwnerIdAndCategoryTypeAndNameIgnoreCaseAndStatus(
            UUID ownerId,
            FinanceCategoryType categoryType,
            String name,
            FinanceCategoryStatus status);

    @Query("""
            select category
            from FinanceCategory category
            where category.ownerId = :ownerId
              and (:type is null or category.categoryType = :type)
              and (:status is null or category.status = :status)
            """)
    Page<FinanceCategory> search(
            @Param("ownerId") UUID ownerId,
            @Param("type") FinanceCategoryType type,
            @Param("status") FinanceCategoryStatus status,
            Pageable pageable);
}
