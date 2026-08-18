package com.lifebalance.resourcecapital.service.impl;

import com.lifebalance.common.web.PageableLimits;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capital.exception.CapitalNotSetupException;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationStatus;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;
import com.lifebalance.resourcecapital.domain.capitalallocation.CapitalAllocation;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.AllocationNotFoundException;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.InvalidAllocationAmountException;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.InvalidAllocationStateException;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.InvalidAllocationTargetException;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.CapitalCycleNotFoundException;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.InvalidCapitalCycleStateException;
import com.lifebalance.resourcecapital.domain.capitalreallocation.CapitalReallocation;
import com.lifebalance.resourcecapital.domain.capitalrelease.CapitalRelease;
import com.lifebalance.resourcecapital.domain.moneycapital.MoneyCapital;
import com.lifebalance.resourcecapital.domain.timecapital.TimeCapital;
import com.lifebalance.resourcecapital.dto.AllocateCapitalRequest;
import com.lifebalance.resourcecapital.dto.AllocateCapitalRequestDTO;
import com.lifebalance.resourcecapital.dto.AllocationResponse;
import com.lifebalance.resourcecapital.dto.AllocationResponseDTO;
import com.lifebalance.resourcecapital.dto.CapitalAllocationChangeRequest;
import com.lifebalance.resourcecapital.dto.CapitalAllocationReleaseRequest;
import com.lifebalance.resourcecapital.dto.CapitalAllocationResponse;
import com.lifebalance.resourcecapital.dto.CapitalReallocationRequest;
import com.lifebalance.resourcecapital.dto.ChangeCapitalAllocationRequestDTO;
import com.lifebalance.resourcecapital.dto.CreateCapitalAllocationRequest;
import com.lifebalance.resourcecapital.dto.ReallocateCapitalRequest;
import com.lifebalance.resourcecapital.dto.ReallocateCapitalRequestDTO;
import com.lifebalance.resourcecapital.dto.ReleaseCapitalRequest;
import com.lifebalance.resourcecapital.dto.ReleaseCapitalRequestDTO;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalAllocationSpecification;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalAllocationRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalCycleRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalReallocationRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalReleaseRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.MoneyCapitalRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.TimeCapitalRepository;
import com.lifebalance.resourcecapital.service.AllocationService;
import com.lifebalance.resourcecapital.service.CapitalAllocationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
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
    private final TimeCapitalRepository timeCapitalRepository;
    private final MoneyCapitalRepository moneyCapitalRepository;

    public CapitalAllocationServiceImpl(
            AllocationService allocationService,
            CapitalCycleRepository capitalCycleRepository,
            CapitalAllocationRepository capitalAllocationRepository,
            CapitalReallocationRepository capitalReallocationRepository,
            CapitalReleaseRepository capitalReleaseRepository,
            TimeCapitalRepository timeCapitalRepository,
            MoneyCapitalRepository moneyCapitalRepository
    ) {
        this.allocationService = allocationService;
        this.capitalCycleRepository = capitalCycleRepository;
        this.capitalAllocationRepository = capitalAllocationRepository;
        this.capitalReallocationRepository = capitalReallocationRepository;
        this.capitalReleaseRepository = capitalReleaseRepository;
        this.timeCapitalRepository = timeCapitalRepository;
        this.moneyCapitalRepository = moneyCapitalRepository;
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
                request.taskCatalogId(),
                null
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
                        request.overAllocationConfirmationKey(),
                        request.reason()
                )
        );
    }

    @Transactional
    @Override
    public AllocationResponseDTO allocateCapital(UUID ownerId, AllocateCapitalRequestDTO request) {
        Objects.requireNonNull(request, "Allocate capital request is required.");
        CapitalCycle cycle = findActiveOwnedCycle(ownerId, request.capitalCycleId(), "allocate capital");
        AllocationTarget target = resolveTarget(
                request.targetType(),
                request.targetId(),
                request.taskId(),
                request.taskCatalogId(),
                request.projectId()
        );

        AllocationResponse response = allocationService.allocateCapital(
                ownerId,
                cycle.getId(),
                new AllocateCapitalRequest(
                        request.capitalType(),
                        target.targetType(),
                        target.targetId(),
                        request.amount(),
                        request.overAllocationConfirmed(),
                        request.overAllocationConfirmationKey(),
                        request.reason()
                )
        );
        return AllocationResponseDTO.from(response);
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
                null,
                null,
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
                null,
                null,
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
    public AllocationResponseDTO reallocateCapital(UUID ownerId, ReallocateCapitalRequestDTO request) {
        Objects.requireNonNull(request, "Reallocate capital request is required.");
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
                request.sourceTaskCatalogId(),
                request.sourceProjectId(),
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
                request.destinationTaskCatalogId(),
                request.destinationProjectId(),
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
        return AllocationResponseDTO.from(response);
    }

    @Transactional
    @Override
    public AllocationResponse changeAllocation(
            UUID ownerId,
            UUID allocationId,
            CapitalAllocationChangeRequest request
    ) {
        Objects.requireNonNull(request, "Capital allocation change request is required.");
        CapitalAllocation allocation = findOwnedAllocationForUpdate(ownerId, allocationId);

        return changeAllocationAmount(
                ownerId,
                allocation,
                request.newAmount(),
                request.overAllocationConfirmed(),
                request.overAllocationConfirmationKey(),
                request.reason()
        );
    }

    @Transactional
    @Override
    public AllocationResponseDTO changeAllocation(UUID ownerId, ChangeCapitalAllocationRequestDTO request) {
        Objects.requireNonNull(request, "Change capital allocation request is required.");
        CapitalAllocation allocation = findOwnedAllocationForUpdate(ownerId, request.allocationId());

        AllocationResponse response = changeAllocationAmount(
                ownerId,
                allocation,
                request.newAmount(),
                request.overAllocationConfirmed(),
                request.overAllocationConfirmationKey(),
                request.reason()
        );
        return AllocationResponseDTO.from(response);
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

    @Transactional
    @Override
    public AllocationResponseDTO releaseCapital(UUID ownerId, ReleaseCapitalRequestDTO request) {
        Objects.requireNonNull(request, "Release capital request is required.");
        CapitalAllocation allocation = findOwnedAllocation(ownerId, request.allocationId());
        validateReleaseRequestMatchesAllocation(request, allocation);

        AllocationResponse response = releaseCapital(
                ownerId,
                allocation.getId(),
                new CapitalAllocationReleaseRequest(request.amount(), request.reason())
        );
        return AllocationResponseDTO.from(response);
    }

    @Transactional(readOnly = true)
    @Override
    public List<AllocationResponseDTO> getAllocationsByCycle(UUID ownerId, UUID cycleId, CapitalKind capitalType) {
        Objects.requireNonNull(ownerId, "Owner id is required.");
        CapitalKind requiredCapitalType = requireCapitalType(capitalType);
        findActiveOwnedCycle(ownerId, cycleId, "get allocations by cycle");
        BigDecimal plannedAmount = plannedAmount(cycleId, requiredCapitalType);
        BigDecimal totalAllocatedAmount = sumAllocated(ownerId, cycleId, requiredCapitalType);

        return capitalAllocationRepository.findByUserIdAndCapitalCycleId(ownerId, cycleId)
                .stream()
                .filter(allocation -> allocation.getCapitalType() == requiredCapitalType)
                .map(allocation -> toDto(allocation, plannedAmount, totalAllocatedAmount))
                .toList();
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
            UUID taskCatalogId,
            UUID projectId,
            String label
    ) {
        if (allocationId == null) {
            return resolveTarget(targetType, targetId, taskId, taskCatalogId, projectId);
        }

        CapitalAllocation allocation = findOwnedAllocation(ownerId, allocationId);
        if (!allocation.getCapitalCycle().getId().equals(cycleId)) {
            throw new InvalidAllocationTargetException(label + " allocation must belong to the requested cycle.");
        }
        if (allocation.getCapitalType() != capitalType) {
            throw new InvalidAllocationTargetException(label + " allocation capital type must match the request.");
        }

        AllocationTarget resolvedTarget = new AllocationTarget(allocation.getTargetType(), allocation.getTargetId());
        AllocationTarget explicitTarget = targetType == null
                && targetId == null
                && taskId == null
                && taskCatalogId == null
                && projectId == null
                ? null
                : resolveTarget(targetType, targetId, taskId, taskCatalogId, projectId);
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

    private CapitalAllocation findOwnedAllocationForUpdate(UUID ownerId, UUID allocationId) {
        if (allocationId == null) {
            throw new InvalidAllocationTargetException("Allocation id is required.");
        }
        return capitalAllocationRepository.findByIdAndUserIdForUpdate(allocationId, ownerId)
                .orElseThrow(() -> new AllocationNotFoundException(allocationId));
    }

    private AllocationResponse changeAllocationAmount(
            UUID ownerId,
            CapitalAllocation allocation,
            BigDecimal requestedNewAmount,
            boolean overAllocationConfirmed,
            String overAllocationConfirmationKey,
            String reason
    ) {
        findActiveOwnedCycle(ownerId, allocation.getCapitalCycle().getId(), "change allocation");
        ensureActiveAllocation(allocation, "change allocation");

        BigDecimal newAmount = normalizeNewAllocationAmount(allocation.getCapitalType(), requestedNewAmount);
        BigDecimal currentAmount = allocation.getAllocatedAmount()
                .setScale(CapitalAllocation.AMOUNT_SCALE, RoundingMode.UNNECESSARY);
        int comparison = newAmount.compareTo(currentAmount);
        if (comparison == 0) {
            return allocationSnapshot(ownerId, allocation);
        }

        if (comparison > 0) {
            BigDecimal delta = newAmount.subtract(currentAmount)
                    .setScale(CapitalAllocation.AMOUNT_SCALE, RoundingMode.UNNECESSARY);
            return allocationService.allocateCapital(
                    ownerId,
                    allocation.getCapitalCycle().getId(),
                    new AllocateCapitalRequest(
                            allocation.getCapitalType(),
                            allocation.getTargetType(),
                            allocation.getTargetId(),
                            delta,
                            overAllocationConfirmed,
                            overAllocationConfirmationKey,
                            reason
                    )
            );
        }

        BigDecimal delta = currentAmount.subtract(newAmount)
                .setScale(CapitalAllocation.AMOUNT_SCALE, RoundingMode.UNNECESSARY);
        return releaseCapital(
                ownerId,
                allocation.getId(),
                new CapitalAllocationReleaseRequest(delta, reason)
        );
    }

    private void ensureActiveAllocation(CapitalAllocation allocation, String action) {
        if (allocation.getStatus() != AllocationStatus.ACTIVE) {
            throw new InvalidAllocationStateException(allocation.getId(), allocation.getStatus(), action);
        }
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
            UUID taskCatalogId,
            UUID projectId
    ) {
        AllocationTarget shortcutTarget = resolveShortcutTarget(taskId, taskCatalogId, projectId);
        AllocationTargetType resolvedTargetType = targetType == null && shortcutTarget != null
                ? shortcutTarget.targetType()
                : requireTargetType(targetType);
        UUID resolvedTargetId = targetId;
        if (shortcutTarget != null) {
            if (resolvedTargetType != shortcutTarget.targetType()) {
                throw new InvalidAllocationTargetException(
                        "Shortcut target id does not match allocation target type."
                );
            }
            if (resolvedTargetId != null && !resolvedTargetId.equals(shortcutTarget.targetId())) {
                throw new InvalidAllocationTargetException(
                        "targetId and shortcut target id must reference the same allocation target."
                );
            }
            resolvedTargetId = shortcutTarget.targetId();
        }
        if (resolvedTargetId == null) {
            throw new InvalidAllocationTargetException("Allocation target id is required.");
        }
        return new AllocationTarget(resolvedTargetType, resolvedTargetId);
    }

    private AllocationTarget resolveShortcutTarget(UUID taskId, UUID taskCatalogId, UUID projectId) {
        AllocationTarget shortcutTarget = null;
        if (taskId != null) {
            shortcutTarget = new AllocationTarget(AllocationTargetType.TASK, taskId);
        }
        if (taskCatalogId != null) {
            shortcutTarget = mergeShortcutTarget(
                    shortcutTarget,
                    new AllocationTarget(AllocationTargetType.TASK_CATALOG, taskCatalogId)
            );
        }
        if (projectId != null) {
            shortcutTarget = mergeShortcutTarget(
                    shortcutTarget,
                    new AllocationTarget(AllocationTargetType.PROJECT, projectId)
            );
        }
        return shortcutTarget;
    }

    private AllocationTarget mergeShortcutTarget(AllocationTarget current, AllocationTarget candidate) {
        if (current == null) {
            return candidate;
        }
        if (current.equals(candidate)) {
            return current;
        }
        throw new InvalidAllocationTargetException("Only one allocation target shortcut id can be provided.");
    }

    private void validateReleaseRequestMatchesAllocation(
            ReleaseCapitalRequestDTO request,
            CapitalAllocation allocation
    ) {
        if (request.capitalCycleId() != null
                && !request.capitalCycleId().equals(allocation.getCapitalCycle().getId())) {
            throw new InvalidAllocationTargetException("Release allocation must belong to the requested cycle.");
        }
        if (request.capitalType() != null && request.capitalType() != allocation.getCapitalType()) {
            throw new InvalidAllocationTargetException("Release allocation capital type must match the request.");
        }

        boolean hasExplicitTarget = request.targetType() != null
                || request.targetId() != null
                || request.taskId() != null
                || request.taskCatalogId() != null
                || request.projectId() != null;
        if (!hasExplicitTarget) {
            return;
        }

        AllocationTarget explicitTarget = resolveTarget(
                request.targetType(),
                request.targetId(),
                request.taskId(),
                request.taskCatalogId(),
                request.projectId()
        );
        if (explicitTarget.targetType() != allocation.getTargetType()
                || !explicitTarget.targetId().equals(allocation.getTargetId())) {
            throw new InvalidAllocationTargetException("Release allocation target must match the request.");
        }
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
        return targetType;
    }

    private BigDecimal normalizeNewAllocationAmount(CapitalKind capitalType, BigDecimal amount) {
        if (amount == null) {
            throw new InvalidAllocationAmountException("New allocation amount is required.");
        }
        BigDecimal normalizedAmount;
        try {
            normalizedAmount = amount.setScale(CapitalAllocation.AMOUNT_SCALE, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new InvalidAllocationAmountException(
                    "New allocation amount scale must not exceed " + CapitalAllocation.AMOUNT_SCALE + "."
            );
        }
        if (normalizedAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidAllocationAmountException("New allocation amount must be zero or greater.");
        }
        if (capitalType == CapitalKind.TIME && normalizedAmount.stripTrailingZeros().scale() > 0) {
            throw new InvalidAllocationAmountException("Time allocation amount must be whole minutes.");
        }
        return normalizedAmount;
    }

    private BigDecimal plannedAmount(UUID cycleId, CapitalKind capitalType) {
        return switch (capitalType) {
            case TIME -> timeCapitalRepository.findByCapitalCycleId(cycleId)
                    .map(TimeCapital::getPlannedMinutes)
                    .map(this::money)
                    .orElseThrow(() -> new CapitalNotSetupException(cycleId, CapitalKind.TIME));
            case MONEY -> moneyCapitalRepository.findByCapitalCycleId(cycleId)
                    .map(MoneyCapital::getPlannedAmount)
                    .orElseThrow(() -> new CapitalNotSetupException(cycleId, CapitalKind.MONEY));
        };
    }

    private BigDecimal sumAllocated(UUID ownerId, UUID cycleId, CapitalKind capitalType) {
        BigDecimal total = capitalAllocationRepository.sumAllocatedAmount(ownerId, cycleId, capitalType);
        if (total == null) {
            return zero();
        }
        return total.setScale(CapitalAllocation.AMOUNT_SCALE, RoundingMode.UNNECESSARY);
    }

    private BigDecimal money(long amount) {
        return BigDecimal.valueOf(amount).setScale(CapitalAllocation.AMOUNT_SCALE, RoundingMode.UNNECESSARY);
    }

    private BigDecimal zero() {
        return BigDecimal.ZERO.setScale(CapitalAllocation.AMOUNT_SCALE, RoundingMode.UNNECESSARY);
    }

    private AllocationResponse allocationSnapshot(UUID ownerId, CapitalAllocation allocation) {
        BigDecimal plannedAmount = plannedAmount(
                allocation.getCapitalCycle().getId(),
                allocation.getCapitalType()
        );
        BigDecimal totalAllocatedAmount = sumAllocated(
                ownerId,
                allocation.getCapitalCycle().getId(),
                allocation.getCapitalType()
        );
        BigDecimal remainingAmount = plannedAmount.subtract(totalAllocatedAmount)
                .setScale(CapitalAllocation.AMOUNT_SCALE, RoundingMode.UNNECESSARY);
        return new AllocationResponse(
                allocation.getCapitalCycle().getId(),
                allocation.getCapitalType(),
                allocation.getTargetType(),
                allocation.getTargetId(),
                allocation.getAllocatedAmount(),
                plannedAmount,
                totalAllocatedAmount,
                remainingAmount,
                remainingAmount.compareTo(BigDecimal.ZERO) < 0,
                List.of()
        );
    }

    private AllocationResponseDTO toDto(
            CapitalAllocation allocation,
            BigDecimal plannedAmount,
            BigDecimal totalAllocatedAmount
    ) {
        BigDecimal remainingAmount = plannedAmount.subtract(totalAllocatedAmount)
                .setScale(CapitalAllocation.AMOUNT_SCALE, RoundingMode.UNNECESSARY);
        return new AllocationResponseDTO(
                allocation.getId(),
                allocation.getCapitalCycle().getId(),
                allocation.getCapitalType(),
                allocation.getTargetType(),
                allocation.getTargetId(),
                allocation.getAllocatedAmount(),
                allocation.getSpentAmount(),
                allocation.getReleasedAmount(),
                allocation.getStatus(),
                plannedAmount,
                totalAllocatedAmount,
                remainingAmount,
                Boolean.TRUE.equals(allocation.getIsOverAllocated()),
                Boolean.TRUE.equals(allocation.getOverAllocationConfirmed()),
                List.of(),
                allocation.getCreatedAt(),
                allocation.getUpdatedAt()
        );
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
