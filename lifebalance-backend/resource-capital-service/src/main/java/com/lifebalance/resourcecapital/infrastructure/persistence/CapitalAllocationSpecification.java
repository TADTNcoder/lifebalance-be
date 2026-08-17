package com.lifebalance.resourcecapital.infrastructure.persistence;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationStatus;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;
import com.lifebalance.resourcecapital.domain.capitalallocation.CapitalAllocation;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class CapitalAllocationSpecification {

    private CapitalAllocationSpecification() {
    }

    public static Specification<CapitalAllocation> filter(
            UUID userId,
            UUID capitalCycleId,
            AllocationTargetType targetType,
            UUID targetId,
            CapitalKind capitalType,
            AllocationStatus status
    ) {
        UUID ownerId = Objects.requireNonNull(userId, "Allocation owner id is required.");
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("userId"), ownerId));

            if (capitalCycleId != null) {
                predicates.add(criteriaBuilder.equal(root.get("capitalCycle").get("id"), capitalCycleId));
            }
            if (targetType != null) {
                predicates.add(criteriaBuilder.equal(root.get("targetType"), targetType));
            }
            if (targetId != null) {
                predicates.add(criteriaBuilder.equal(root.get("targetId"), targetId));
            }
            if (capitalType != null) {
                predicates.add(criteriaBuilder.equal(root.get("capitalType"), capitalType));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
