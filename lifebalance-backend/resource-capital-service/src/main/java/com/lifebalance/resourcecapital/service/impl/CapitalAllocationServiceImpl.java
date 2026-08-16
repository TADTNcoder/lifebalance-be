package com.lifebalance.resourcecapital.service.impl;

import com.lifebalance.common.web.PageableLimits;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationStatus;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;
import com.lifebalance.resourcecapital.domain.capitalallocation.CapitalAllocation;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.AllocationNotFoundException;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.InvalidAllocationAmountException;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.InvalidAllocationTargetException;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.CapitalCycleNotFoundException;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.InvalidCapitalCycleStateException;
import com.lifebalance.resourcecapital.domain.capitalreallocation.CapitalReallocation;
import com.lifebalance.resourcecapital.domain.capitalrelease.CapitalRelease;
import com.lifebalance.resourcecapital.dto.AllocateCapitalRequest;
import com.lifebalance.resourcecapital.dto.AllocationResponse;
import com.lifebalance.resourcecapital.dto.CapitalAllocationReleaseRequest;
import com.lifebalance.resourcecapital.dto.CapitalAllocationResponse;
import com.lifebalance.resourcecapital.dto.CapitalReallocationRequest;
import com.lifebalance.resourcecapital.dto.CreateCapitalAllocationRequest;
import com.lifebalance.resourcecapital.dto.ReallocateCapitalRequest;
import com.lifebalance.resourcecapital.dto.ReleaseCapitalRequest;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalAllocationSpecification;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalAllocationRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalCycleRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalReallocationRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalReleaseRepository;
import com.lifebalance.resourcecapital.service.AllocationService;
import com.lifebalance.resourcecapital.service.CapitalAllocationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Service
public class CapitalAllocationServiceImpl implements CapitalAllocationService {

    private static final Sort DEFAULT_SORT = Sort.by(
            Sort.Order.desc("createdAt"),
            Sort.Order.desc("id")
    );
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final AllocationService allocationService;
    private final CapitalCycleRepository capitalCycleRepository;
    private final CapitalAllocationRepository capitalAllocationRepository;
    private final CapitalReallocationRepository capitalReallocationRepository;
    private final CapitalReleaseRepository capitalReleaseRepository;

    public CapitalAllocationServiceImpl(
            AllocationService allocationService,
            CapitalCycleRepository capitalCycleRepository,
            CapitalAllocationRepository capitalAllocationRepository,
            CapitalReallocationRepository capitalReallocationRepository,
            CapitalReleaseRepository capitalReleaseRepository
    ) {
        this.allocationService = allocationService;
        this.capitalCycleRepository = capitalCycleRepository;
        this.capitalAllocationRepository = capitalAllocationRepository;
        this.capitalReallocationRepository = capitalReallocationRepository;
        this.capitalReleaseRepository = capitalReleaseRepository;
    }

    @Transactional
    @Override
    public AllocationResponse allocateCapital(UUID ownerId, CreateCapitalAllocationRequest request) {
        Objects.requireNonNull(request, "Create capital allocation request is required.");
        CapitalCycle cycle = findActiveOwnedCycle(ownerId, request.capitalCycleId(), "allocate capital");
        AllocationTarget target = resolveTarget(
                request.targetType(),
                request.targetId(),
                request.taskId(),
                request.taskCatalogId()
        );

        return allocationService.allocateCapital(
                ownerId,
                cycle.getId(),
                new AllocateCapitalRequest(
                        request.capitalType(),
                        target.targetType(),
                        target.targetId(),
                        request.amount(),
                        request.allowOverAllocation(),
                        request.reason()
                )
        );
    }

    @Transactional
    @Override
    public AllocationResponse reallocateCapital(UUID ownerId, CapitalReallocationRequest request) {
        Objects.requireNonNull(request, "Capital reallocation request is required.");
        CapitalCycle cycle = findActiveOwnedCycle(ownerId, request.capitalCycleId(), "reallocate capital");
        CapitalKind capitalType = requireCapitalType(request.capitalType());
        AllocationTarget sourceTarget = resolveReallocationTarget(
                ownerId,
                cycle.getId(),
                capitalType,
                request.sourceAllocationId(),
                request.sourceTargetType(),
                request.sourceTargetId(),
                request.sourceTaskId(),
                "Source"
        );
        AllocationTarget destinationTarget = resolveReallocationTarget(
                ownerId,
                cycle.getId(),
                capitalType,
                request.destinationAllocationId(),
                request.destinationTargetType(),
                request.destinationTargetId(),
                request.destinationTaskId(),
                "Destination"
        );

        AllocationResponse response = allocationService.reallocateCapital(
                ownerId,
                cycle.getId(),
                new ReallocateCapitalRequest(
                        capitalType,
                        sourceTarget.targetType(),
                        sourceTarget.targetId(),
                        destinationTarget.targetType(),
                        destinationTarget.targetId(),
                        request.amount(),
                        request.reason()
                )
        );
        CapitalAllocation sourceAllocation = findTargetAllocation(
                ownerId,
                cycle.getId(),
                capitalType,
                sourceTarget.targetType(),
                sourceTarget.targetId()
        );
        CapitalAllocation destinationAllocation = findTargetAllocation(
                ownerId,
                cycle.getId(),
                capitalType,
                destinationTarget.targetType(),
                destinationTarget.targetId()
        );
        capitalReallocationRepository.saveAndFlush(CapitalReallocation.record(
                sourceAllocation,
                destinationAllocation,
                request.amount(),
                request.reason()
        ));
        return response;
    }

    @Transactional
    @Override
    public AllocationResponse releaseCapital(
            UUID ownerId,
            UUID allocationId,
            CapitalAllocationReleaseRequest request
    ) {
        Objects.requireNonNull(request, "Capital allocation release request is required.");
        CapitalAllocation allocation = findOwnedAllocation(ownerId, allocationId);
        findActiveOwnedCycle(ownerId, allocation.getCapitalCycle().getId(), "release capital");

        AllocationResponse response = allocationService.releaseCapital(
                ownerId,
                allocation.getCapitalCycle().getId(),
                new ReleaseCapitalRequest(
                        allocation.getCapitalType(),
                        allocation.getTargetType(),
                        allocation.getTargetId(),
                        request.amount(),
                        request.reason()
                )
        );
        capitalReleaseRepository.saveAndFlush(CapitalRelease.record(
                allocation,
                request.amount(),
                request.reason()
        ));
        return response;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<CapitalAllocationResponse> getAllocations(
            UUID ownerId,
            UUID capitalCycleId,
            UUID taskId,
            CapitalKind capitalType,
            AllocationStatus status,
            Pageable pageable
    ) {
        Objects.requireNonNull(ownerId, "Owner id is required.");
        if (capitalCycleId != null) {
            findOwnedCycle(ownerId, capitalCycleId);
        }

        return capitalAllocationRepository.findAll(
                        CapitalAllocationSpecification.filter(
                                ownerId,
                                capitalCycleId,
                                taskId == null ? null : AllocationTargetType.TASK,
                                taskId,
                                capitalType,
                                status
                        ),
                        pageableWithDefaultSort(pageable)
                )
                .map(this::toResponse);
    }

    private CapitalCycle findActiveOwnedCycle(UUID ownerId, UUID cycleId, String action) {
        CapitalCycle cycle = findOwnedCycle(ownerId, cycleId);
        if (cycle.getStatus() != CapitalCycleStatus.ACTIVE) {
            throw new InvalidCapitalCycleStateException(
                    cycle.getId(),
                    cycle.getStatus(),
                    action,
                    "capital allocation APIs require an ACTIVE cycle"
            );
        }
        return cycle;
    }

    private CapitalCycle findOwnedCycle(UUID ownerId, UUID cycleId) {
        if (cycleId == null) {
            throw new InvalidAllocationTargetException("Capital cycle id is required.");
        }
        return capitalCycleRepository.findByIdAndOwnerId(cycleId, ownerId)
                .orElseThrow(() -> new CapitalCycleNotFoundException(cycleId));
    }

    private AllocationTarget resolveReallocationTarget(
            UUID ownerId,
            UUID cycleId,
            CapitalKind capitalType,
            UUID allocationId,
            AllocationTargetType targetType,
            UUID targetId,
            UUID taskId,
            String label
    ) {
        if (allocationId == null) {
            return resolveTarget(targetType, targetId, taskId, null);
        }

        CapitalAllocation allocation = findOwnedAllocation(ownerId, allocationId);
        if (!allocation.getCapitalCycle().getId().equals(cycleId)) {
            throw new InvalidAllocationTargetException(label + " allocation must belong to the requested cycle.");
        }
        if (allocation.getCapitalType() != capitalType) {
            throw new InvalidAllocationTargetException(label + " allocation capital type must match the request.");
        }

        AllocationTarget resolvedTarget = new AllocationTarget(allocation.getTargetType(), allocation.getTargetId());
        AllocationTarget explicitTarget = targetType == null && targetId == null && taskId == null
                ? null
                : resolveTarget(targetType, targetId, taskId, null);
        if (explicitTarget != null && !resolvedTarget.equals(explicitTarget)) {
            throw new InvalidAllocationTargetException(label + " allocation id does not match the requested target.");
        }
        return resolvedTarget;
    }

    private CapitalAllocation findOwnedAllocation(UUID ownerId, UUID allocationId) {
        if (allocationId == null) {
            throw new InvalidAllocationTargetException("Allocation id is required.");
        }
        return capitalAllocationRepository.findByIdAndUserId(allocationId, ownerId)
                .orElseThrow(() -> new AllocationNotFoundException(allocationId));
    }

    private CapitalAllocation findTargetAllocation(
            UUID ownerId,
            UUID cycleId,
            CapitalKind capitalType,
            AllocationTargetType targetType,
            UUID targetId
    ) {
        return capitalAllocationRepository.findByUserIdAndCapitalCycleIdAndTargetTypeAndTargetIdAndCapitalType(
                ownerId,
                cycleId,
                targetType,
                targetId,
                capitalType
        ).orElseThrow(() -> new AllocationNotFoundException(cycleId, capitalType, targetType, targetId));
    }

    private AllocationTarget resolveTarget(
            AllocationTargetType targetType,
            UUID targetId,
            UUID taskId,
            UUID taskCatalogId
    ) {
        if (taskCatalogId != null) {
            throw new InvalidAllocationTargetException("Task catalog allocation targets are not supported yet.");
        }
        AllocationTargetType resolvedTargetType = targetType == null && taskId != null
                ? AllocationTargetType.TASK
                : requireTargetType(targetType);
        UUID resolvedTargetId = targetId;
        if (resolvedTargetType == AllocationTargetType.TASK && taskId != null) {
            if (resolvedTargetId != null && !resolvedTargetId.equals(taskId)) {
                throw new InvalidAllocationTargetException("targetId and taskId must reference the same task.");
            }
            resolvedTargetId = taskId;
        }
        if (resolvedTargetId == null) {
            throw new InvalidAllocationTargetException("Allocation target id is required.");
        }
        return new AllocationTarget(resolvedTargetType, resolvedTargetId);
    }

    private CapitalKind requireCapitalType(CapitalKind capitalType) {
        if (capitalType == null) {
            throw new InvalidAllocationAmountException("Capital type is required.");
        }
        return capitalType;
    }

    private AllocationTargetType requireTargetType(AllocationTargetType targetType) {
        if (targetType == null) {
            throw new InvalidAllocationTargetException("Allocation target type is required.");
        }
        if (targetType != AllocationTargetType.TASK) {
            throw new InvalidAllocationTargetException("Only TASK allocation targets are supported.");
        }
        return targetType;
    }

    private Pageable pageableWithDefaultSort(Pageable pageable) {
        Pageable normalized = PageableLimits.normalize(pageable);
        if (normalized.isUnpaged()) {
            return PageRequest.of(0, DEFAULT_PAGE_SIZE, DEFAULT_SORT);
        }
        if (normalized.getSort().isUnsorted()) {
            return PageRequest.of(normalized.getPageNumber(), normalized.getPageSize(), DEFAULT_SORT);
        }
        return normalized;
    }

    private CapitalAllocationResponse toResponse(CapitalAllocation allocation) {
        UUID taskId = allocation.getTargetType() == AllocationTargetType.TASK ? allocation.getTargetId() : null;
        return new CapitalAllocationResponse(
                allocation.getId(),
                allocation.getCapitalCycle().getId(),
                allocation.getCapitalType(),
                allocation.getTargetType(),
                allocation.getTargetId(),
                taskId,
                allocation.getAllocatedAmount(),
                allocation.getSpentAmount(),
                allocation.getStatus(),
                allocation.getCreatedAt(),
                allocation.getUpdatedAt()
        );
    }

    private record AllocationTarget(AllocationTargetType targetType, UUID targetId) {
    }
}
