package com.lifebalance.resourcecapital.service.impl;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.CapitalCycleNotFoundException;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalHistory;
import com.lifebalance.resourcecapital.dto.CapitalHistoryResponse;
import com.lifebalance.resourcecapital.dto.HistoryFilterRequest;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalCycleRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalHistoryRepository;
import com.lifebalance.resourcecapital.service.CapitalHistoryService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
public class CapitalHistoryServiceImpl implements CapitalHistoryService {

    private static final Sort DEFAULT_SORT = Sort.by(
            Sort.Order.desc("createdAt"),
            Sort.Order.desc("id")
    );
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final CapitalCycleRepository capitalCycleRepository;
    private final CapitalHistoryRepository capitalHistoryRepository;

    public CapitalHistoryServiceImpl(
            CapitalCycleRepository capitalCycleRepository,
            CapitalHistoryRepository capitalHistoryRepository
    ) {
        this.capitalCycleRepository = capitalCycleRepository;
        this.capitalHistoryRepository = capitalHistoryRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<CapitalHistoryResponse> getHistoryByCycle(
            UUID ownerId,
            UUID cycleId,
            HistoryFilterRequest filter,
            Pageable pageable
    ) {
        CapitalCycle cycle = findOwnedCycle(ownerId, cycleId);
        validateRange(filter);
        return capitalHistoryRepository.findAll(specification(cycle.getId(), filter), pageableWithDefaultSort(pageable))
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<CapitalHistoryResponse> getHistoryByResource(
            UUID ownerId,
            UUID cycleId,
            CapitalKind capitalType,
            Pageable pageable
    ) {
        Objects.requireNonNull(capitalType, "Capital type is required.");
        CapitalCycle cycle = findOwnedCycle(ownerId, cycleId);
        HistoryFilterRequest filter = new HistoryFilterRequest(
                capitalType,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        return capitalHistoryRepository.findAll(specification(cycle.getId(), filter), pageableWithDefaultSort(pageable))
                .map(this::toResponse);
    }

    private CapitalCycle findOwnedCycle(UUID ownerId, UUID cycleId) {
        return capitalCycleRepository.findByIdAndOwnerId(cycleId, ownerId)
                .orElseThrow(() -> new CapitalCycleNotFoundException(cycleId));
    }

    private void validateRange(HistoryFilterRequest filter) {
        if (filter == null || filter.from() == null || filter.to() == null) {
            return;
        }
        if (!filter.from().isBefore(filter.to())) {
            throw new IllegalArgumentException("History filter from must be before to.");
        }
    }

    private Specification<CapitalHistory> specification(UUID cycleId, HistoryFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("capitalCycle").get("id"), cycleId));

            if (filter == null) {
                return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
            }

            if (filter.capitalType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("capitalType"), filter.capitalType()));
            }
            if (filter.actionType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("actionType"), filter.actionType()));
            }
            if (filter.from() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), filter.from()));
            }
            if (filter.to() != null) {
                predicates.add(criteriaBuilder.lessThan(root.get("createdAt"), filter.to()));
            }
            if (filter.referenceType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("referenceType"), filter.referenceType()));
            }
            if (filter.referenceId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("referenceId"), filter.referenceId()));
            }
            if (filter.actorType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("actorType"), filter.actorType()));
            }
            if (filter.actorId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("actorId"), filter.actorId()));
            }

            Predicate keywordPredicate = keywordPredicate(root, criteriaBuilder, filter.keyword());
            if (keywordPredicate != null) {
                predicates.add(keywordPredicate);
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Predicate keywordPredicate(
            jakarta.persistence.criteria.Root<CapitalHistory> root,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
            String keyword
    ) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }

        String normalizedKeyword = keyword.trim();
        String likeKeyword = "%" + normalizedKeyword.toLowerCase(Locale.ROOT) + "%";
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("reason")), likeKeyword));
        predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likeKeyword));
        parseUuid(normalizedKeyword)
                .map(referenceId -> criteriaBuilder.equal(root.get("referenceId"), referenceId))
                .ifPresent(predicates::add);

        return criteriaBuilder.or(predicates.toArray(Predicate[]::new));
    }

    private java.util.Optional<UUID> parseUuid(String value) {
        try {
            return java.util.Optional.of(UUID.fromString(value));
        } catch (IllegalArgumentException exception) {
            return java.util.Optional.empty();
        }
    }

    private Pageable pageableWithDefaultSort(Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            return PageRequest.of(0, DEFAULT_PAGE_SIZE, DEFAULT_SORT);
        }
        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), DEFAULT_SORT);
        }
        return pageable;
    }

    private CapitalHistoryResponse toResponse(CapitalHistory history) {
        return new CapitalHistoryResponse(
                history.getId(),
                history.getCapitalCycle().getId(),
                history.getCapitalType(),
                history.getActionType(),
                history.getAmount(),
                history.getBeforeAmount(),
                history.getAfterAmount(),
                history.getReason(),
                history.getDescription(),
                history.getReferenceType(),
                history.getReferenceId(),
                history.getActorType(),
                history.getActorId(),
                history.getCreatedAt()
        );
    }
}
