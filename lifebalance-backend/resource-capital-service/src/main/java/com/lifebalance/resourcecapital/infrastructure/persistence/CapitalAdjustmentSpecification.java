package com.lifebalance.resourcecapital.infrastructure.persistence;

import com.lifebalance.resourcecapital.domain.capitaladjustment.AdjustmentType;
import com.lifebalance.resourcecapital.domain.capitaladjustment.CapitalAdjustment;
import com.lifebalance.resourcecapital.domain.capitaladjustment.CapitalType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class CapitalAdjustmentSpecification {

    private CapitalAdjustmentSpecification() {
    }

    public static Specification<CapitalAdjustment> filter(
            UUID userId,
            UUID capitalCycleId,
            CapitalType capitalType,
            AdjustmentType adjustmentType,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        
        UUID ownerId = Objects.requireNonNull(userId, "Adjustment owner id is required.");
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("userId"), ownerId));

            if (capitalCycleId != null) {
                predicates.add(criteriaBuilder.equal(root.get("capitalCycle").get("id"), capitalCycleId));
            }
            if (capitalType != null) {
                predicates.add(criteriaBuilder.equal(root.get("capitalType"), capitalType));
            }
            if (adjustmentType != null) {
                predicates.add(criteriaBuilder.equal(root.get("adjustmentType"), adjustmentType));
            }
            if (startDate != null && endDate != null) {
                predicates.add(criteriaBuilder.between(root.get("createdAt"), startDate, endDate));
            } else if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            } else if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
