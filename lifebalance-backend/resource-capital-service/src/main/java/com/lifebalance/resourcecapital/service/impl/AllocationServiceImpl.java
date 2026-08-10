package com.lifebalance.resourcecapital.service.impl;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capital.exception.CapitalNotSetupException;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;
import com.lifebalance.resourcecapital.domain.capitalallocation.CapitalAllocation;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.AllocationNotFoundException;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.InsufficientAllocatedCapitalException;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.InvalidAllocationAmountException;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.InvalidAllocationTargetException;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.OverAllocationConfirmationRequiredException;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.OverAllocationNotAllowedException;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.CapitalCycleNotFoundException;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActionType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActorType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalHistory;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalReferenceType;
import com.lifebalance.resourcecapital.domain.moneycapital.MoneyCapital;
import com.lifebalance.resourcecapital.domain.timecapital.TimeCapital;
import com.lifebalance.resourcecapital.dto.AllocateCapitalRequest;
import com.lifebalance.resourcecapital.dto.AllocationResponse;
import com.lifebalance.resourcecapital.dto.ReallocateCapitalRequest;
import com.lifebalance.resourcecapital.dto.ReleaseCapitalRequest;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalAllocationRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalCycleRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalHistoryRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.MoneyCapitalRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.TimeCapitalRepository;
import com.lifebalance.resourcecapital.service.AllocationService;
import com.lifebalance.resourcecapital.service.AllocationTargetValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class AllocationServiceImpl implements AllocationService {

    private static final int MONEY_SCALE = 4;
    private static final int REASON_MAX_LENGTH = 1000;

    private final CapitalCycleRepository capitalCycleRepository;
    private final TimeCapitalRepository timeCapitalRepository;
    private final MoneyCapitalRepository moneyCapitalRepository;
    private final CapitalAllocationRepository capitalAllocationRepository;
    private final CapitalHistoryRepository capitalHistoryRepository;
    private final AllocationTargetValidator allocationTargetValidator;

    public AllocationServiceImpl(
            CapitalCycleRepository capitalCycleRepository,
            TimeCapitalRepository timeCapitalRepository,
            MoneyCapitalRepository moneyCapitalRepository,
            CapitalAllocationRepository capitalAllocationRepository,
            CapitalHistoryRepository capitalHistoryRepository,
            AllocationTargetValidator allocationTargetValidator
    ) {
        this.capitalCycleRepository = capitalCycleRepository;
        this.timeCapitalRepository = timeCapitalRepository;
        this.moneyCapitalRepository = moneyCapitalRepository;
        this.capitalAllocationRepository = capitalAllocationRepository;
        this.capitalHistoryRepository = capitalHistoryRepository;
        this.allocationTargetValidator = allocationTargetValidator;
    }

    @Transactional
    @Override
    public AllocationResponse allocateCapital(UUID ownerId, UUID cycleId, AllocateCapitalRequest request) {
        Objects.requireNonNull(request, "Allocate capital request is required.");
        CapitalKind capitalType = requireCapitalType(request.capitalType());
        AllocationTargetType targetType = requireTargetType(request.targetType());
        UUID targetId = requireTargetId(request.targetId(), "Allocation target id is required.");
        BigDecimal amount = normalizeAmount(capitalType, request.amount());
        String reason = optionalReason(request.reason());

        allocationTargetValidator.validateTarget(ownerId, targetType, targetId);
        CapitalCycle cycle = findAdjustableOwnedCycle(ownerId, cycleId);
        BigDecimal plannedAmount = lockCapitalAndGetPlannedAmount(cycleId, capitalType);

        Optional<CapitalAllocation> allocation = capitalAllocationRepository.findTargetForUpdate(
                cycleId,
                capitalType,
                targetType,
                targetId
        );
        BigDecimal targetBefore = allocation.map(CapitalAllocation::getAllocatedAmount).orElse(zero());
        BigDecimal targetAfter = targetBefore.add(amount);
        BigDecimal totalBefore = sumAllocated(cycleId, capitalType);
        BigDecimal totalAfter = totalBefore.add(amount);
        BigDecimal remainingAfter = plannedAmount.subtract(totalAfter).setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
        boolean overAllocated = remainingAfter.compareTo(BigDecimal.ZERO) < 0;

        if (overAllocated) {
            validateOverAllocationApproval(cycle, capitalType, plannedAmount, totalAfter, request.allowOverAllocation());
        }

        CapitalAllocation targetAllocation = allocation.orElseGet(() -> CapitalAllocation.create(
                cycle,
                capitalType,
                targetType,
                targetId,
                amount
        ));
        if (allocation.isPresent()) {
            targetAllocation.increase(amount);
        }
        capitalAllocationRepository.saveAndFlush(targetAllocation);

        List<UUID> historyIds = new ArrayList<>();
        historyIds.add(recordHistory(
                cycle,
                capitalType,
                CapitalActionType.ALLOCATE,
                amount,
                targetBefore,
                targetAfter,
                reason,
                null,
                targetType,
                targetId,
                ownerId
        ));
        if (overAllocated) {
            BigDecimal remainingBefore = plannedAmount.subtract(totalBefore).setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
            historyIds.add(recordHistory(
                    cycle,
                    capitalType,
                    CapitalActionType.OVER_ALLOCATION_APPROVED,
                    amount,
                    remainingBefore,
                    remainingAfter,
                    reason,
                    "Over-allocation approved for allocation target.",
                    targetType,
                    targetId,
                    ownerId
            ));
        }

        return response(
                cycleId,
                capitalType,
                targetType,
                targetId,
                targetAfter,
                plannedAmount,
                totalAfter,
                historyIds
        );
    }

    @Transactional
    @Override
    public AllocationResponse reallocateCapital(UUID ownerId, UUID cycleId, ReallocateCapitalRequest request) {
        Objects.requireNonNull(request, "Reallocate capital request is required.");
        CapitalKind capitalType = requireCapitalType(request.capitalType());
        AllocationTargetType sourceTargetType = requireTargetType(request.sourceTargetType());
        AllocationTargetType destinationTargetType = requireTargetType(request.destinationTargetType());
        UUID sourceTargetId = requireTargetId(request.sourceTargetId(), "Source allocation target id is required.");
        UUID destinationTargetId = requireTargetId(
                request.destinationTargetId(),
                "Destination allocation target id is required."
        );
        BigDecimal amount = normalizeAmount(capitalType, request.amount());
        String reason = optionalReason(request.reason());
        if (sourceTargetType == destinationTargetType && sourceTargetId.equals(destinationTargetId)) {
            throw new InvalidAllocationTargetException("Source and destination allocation targets must be different.");
        }

        allocationTargetValidator.validateTarget(ownerId, sourceTargetType, sourceTargetId);
        allocationTargetValidator.validateTarget(ownerId, destinationTargetType, destinationTargetId);
        CapitalCycle cycle = findAdjustableOwnedCycle(ownerId, cycleId);
        BigDecimal plannedAmount = lockCapitalAndGetPlannedAmount(cycleId, capitalType);

        List<CapitalAllocation> lockedAllocations = lockReallocationTargets(
                cycleId,
                capitalType,
                sourceTargetType,
                sourceTargetId,
                destinationTargetType,
                destinationTargetId
        );
        CapitalAllocation sourceAllocation = findLockedAllocation(
                lockedAllocations,
                sourceTargetType,
                sourceTargetId
        ).orElseThrow(() -> new AllocationNotFoundException(
                cycleId,
                capitalType,
                sourceTargetType,
                sourceTargetId
        ));
        Optional<CapitalAllocation> destinationAllocation = findLockedAllocation(
                lockedAllocations,
                destinationTargetType,
                destinationTargetId
        );

        BigDecimal sourceBefore = sourceAllocation.getAllocatedAmount();
        if (sourceBefore.compareTo(amount) < 0) {
            throw new InsufficientAllocatedCapitalException(
                    cycleId,
                    capitalType,
                    sourceTargetType,
                    sourceTargetId,
                    amount,
                    sourceBefore
            );
        }

        BigDecimal destinationBefore = destinationAllocation.map(CapitalAllocation::getAllocatedAmount).orElse(zero());
        BigDecimal sourceAfter = sourceBefore.subtract(amount).setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
        BigDecimal destinationAfter = destinationBefore.add(amount).setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);

        sourceAllocation.decrease(amount);
        if (sourceAllocation.isDepleted()) {
            capitalAllocationRepository.delete(sourceAllocation);
        }

        CapitalAllocation destination = destinationAllocation.orElseGet(() -> CapitalAllocation.create(
                cycle,
                capitalType,
                destinationTargetType,
                destinationTargetId,
                amount
        ));
        if (destinationAllocation.isPresent()) {
            destination.increase(amount);
        }
        capitalAllocationRepository.saveAndFlush(destination);

        BigDecimal totalAllocated = sumAllocated(cycleId, capitalType);
        List<UUID> historyIds = List.of(
                recordHistory(
                        cycle,
                        capitalType,
                        CapitalActionType.REALLOCATE,
                        amount,
                        sourceBefore,
                        sourceAfter,
                        reason,
                        "Reallocated capital from source target.",
                        sourceTargetType,
                        sourceTargetId,
                        ownerId
                ),
                recordHistory(
                        cycle,
                        capitalType,
                        CapitalActionType.REALLOCATE,
                        amount,
                        destinationBefore,
                        destinationAfter,
                        reason,
                        "Reallocated capital to destination target.",
                        destinationTargetType,
                        destinationTargetId,
                        ownerId
                )
        );

        return response(
                cycleId,
                capitalType,
                destinationTargetType,
                destinationTargetId,
                destinationAfter,
                plannedAmount,
                totalAllocated,
                historyIds
        );
    }

    @Transactional
    @Override
    public AllocationResponse releaseCapital(UUID ownerId, UUID cycleId, ReleaseCapitalRequest request) {
        Objects.requireNonNull(request, "Release capital request is required.");
        CapitalKind capitalType = requireCapitalType(request.capitalType());
        AllocationTargetType targetType = requireTargetType(request.targetType());
        UUID targetId = requireTargetId(request.targetId(), "Allocation target id is required.");
        BigDecimal amount = normalizeAmount(capitalType, request.amount());
        String reason = optionalReason(request.reason());

        allocationTargetValidator.validateTarget(ownerId, targetType, targetId);
        CapitalCycle cycle = findAdjustableOwnedCycle(ownerId, cycleId);
        BigDecimal plannedAmount = lockCapitalAndGetPlannedAmount(cycleId, capitalType);
        CapitalAllocation allocation = capitalAllocationRepository.findTargetForUpdate(
                cycleId,
                capitalType,
                targetType,
                targetId
        ).orElseThrow(() -> new AllocationNotFoundException(cycleId, capitalType, targetType, targetId));

        BigDecimal targetBefore = allocation.getAllocatedAmount();
        BigDecimal totalBefore = sumAllocated(cycleId, capitalType);
        if (targetBefore.compareTo(amount) < 0) {
            throw new InsufficientAllocatedCapitalException(
                    cycleId,
                    capitalType,
                    targetType,
                    targetId,
                    amount,
                    targetBefore
            );
        }
        BigDecimal targetAfter = targetBefore.subtract(amount).setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);

        allocation.decrease(amount);
        if (allocation.isDepleted()) {
            capitalAllocationRepository.delete(allocation);
        }

        BigDecimal totalAfter = totalBefore.subtract(amount).setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
        UUID historyId = recordHistory(
                cycle,
                capitalType,
                CapitalActionType.RELEASE,
                amount,
                targetBefore,
                targetAfter,
                reason,
                null,
                targetType,
                targetId,
                ownerId
        );

        return response(
                cycleId,
                capitalType,
                targetType,
                targetId,
                targetAfter,
                plannedAmount,
                totalAfter,
                List.of(historyId)
        );
    }

    private CapitalCycle findAdjustableOwnedCycle(UUID ownerId, UUID cycleId) {
        CapitalCycle cycle = capitalCycleRepository.findByIdAndOwnerId(cycleId, ownerId)
                .orElseThrow(() -> new CapitalCycleNotFoundException(cycleId));
        cycle.ensureCapitalAdjustmentAllowed();
        return cycle;
    }

    private BigDecimal lockCapitalAndGetPlannedAmount(UUID cycleId, CapitalKind capitalType) {
        return switch (capitalType) {
            case TIME -> timeCapitalRepository.findByCapitalCycleIdForUpdate(cycleId)
                    .map(TimeCapital::getPlannedMinutes)
                    .map(this::money)
                    .orElseThrow(() -> new CapitalNotSetupException(cycleId, CapitalKind.TIME));
            case MONEY -> moneyCapitalRepository.findByCapitalCycleIdForUpdate(cycleId)
                    .map(MoneyCapital::getPlannedAmount)
                    .orElseThrow(() -> new CapitalNotSetupException(cycleId, CapitalKind.MONEY));
        };
    }

    private List<CapitalAllocation> lockReallocationTargets(
            UUID cycleId,
            CapitalKind capitalType,
            AllocationTargetType sourceTargetType,
            UUID sourceTargetId,
            AllocationTargetType destinationTargetType,
            UUID destinationTargetId
    ) {
        if (sourceTargetType != destinationTargetType) {
            throw new InvalidAllocationTargetException("Reallocation between different target types is not supported.");
        }
        List<UUID> targetIds = List.of(sourceTargetId, destinationTargetId)
                .stream()
                .sorted(Comparator.naturalOrder())
                .toList();
        return capitalAllocationRepository.findTargetsForUpdate(
                cycleId,
                capitalType,
                sourceTargetType,
                targetIds
        );
    }

    private Optional<CapitalAllocation> findLockedAllocation(
            Collection<CapitalAllocation> allocations,
            AllocationTargetType targetType,
            UUID targetId
    ) {
        return allocations.stream()
                .filter(allocation -> allocation.getTargetType() == targetType)
                .filter(allocation -> allocation.getTargetId().equals(targetId))
                .findFirst();
    }

    private void validateOverAllocationApproval(
            CapitalCycle cycle,
            CapitalKind capitalType,
            BigDecimal plannedAmount,
            BigDecimal projectedAllocatedAmount,
            boolean allowOverAllocation
    ) {
        if (!cycle.isOverAllocationAllowed()) {
            throw new OverAllocationNotAllowedException(
                    cycle.getId(),
                    capitalType,
                    plannedAmount,
                    projectedAllocatedAmount
            );
        }
        if (!allowOverAllocation) {
            throw new OverAllocationConfirmationRequiredException(
                    cycle.getId(),
                    capitalType,
                    plannedAmount,
                    projectedAllocatedAmount
            );
        }
    }

    private UUID recordHistory(
            CapitalCycle cycle,
            CapitalKind capitalType,
            CapitalActionType actionType,
            BigDecimal amount,
            BigDecimal beforeAmount,
            BigDecimal afterAmount,
            String reason,
            String description,
            AllocationTargetType targetType,
            UUID targetId,
            UUID actorId
    ) {
        CapitalHistory history = capitalHistoryRepository.saveAndFlush(CapitalHistory.record(
                cycle,
                capitalType,
                actionType,
                amount,
                beforeAmount,
                afterAmount,
                reason,
                description,
                toReferenceType(targetType),
                targetId,
                CapitalActorType.USER,
                actorId
        ));
        return history.getId();
    }

    private AllocationResponse response(
            UUID cycleId,
            CapitalKind capitalType,
            AllocationTargetType targetType,
            UUID targetId,
            BigDecimal targetAllocatedAmount,
            BigDecimal plannedAmount,
            BigDecimal totalAllocatedAmount,
            List<UUID> historyIds
    ) {
        BigDecimal remainingAmount = plannedAmount.subtract(totalAllocatedAmount)
                .setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
        return new AllocationResponse(
                cycleId,
                capitalType,
                targetType,
                targetId,
                targetAllocatedAmount,
                plannedAmount,
                totalAllocatedAmount,
                remainingAmount,
                remainingAmount.compareTo(BigDecimal.ZERO) < 0,
                historyIds
        );
    }

    private BigDecimal sumAllocated(UUID cycleId, CapitalKind capitalType) {
        BigDecimal total = capitalAllocationRepository.sumAllocatedAmount(cycleId, capitalType);
        if (total == null) {
            return zero();
        }
        return total.setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
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

    private UUID requireTargetId(UUID targetId, String message) {
        if (targetId == null) {
            throw new InvalidAllocationTargetException(message);
        }
        return targetId;
    }

    private BigDecimal normalizeAmount(CapitalKind capitalType, BigDecimal amount) {
        if (amount == null) {
            throw new InvalidAllocationAmountException("Allocation amount is required.");
        }
        BigDecimal normalizedAmount;
        try {
            normalizedAmount = amount.setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new InvalidAllocationAmountException(
                    "Allocation amount scale must not exceed " + MONEY_SCALE + "."
            );
        }
        if (normalizedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAllocationAmountException("Allocation amount must be greater than zero.");
        }
        if (capitalType == CapitalKind.TIME && normalizedAmount.stripTrailingZeros().scale() > 0) {
            throw new InvalidAllocationAmountException("Time allocation amount must be whole minutes.");
        }
        return normalizedAmount;
    }

    private String optionalReason(String reason) {
        if (reason == null) {
            return null;
        }
        String normalizedReason = reason.trim();
        if (normalizedReason.isEmpty()) {
            return null;
        }
        if (normalizedReason.length() > REASON_MAX_LENGTH) {
            throw new InvalidAllocationAmountException(
                    "Allocation reason must not exceed " + REASON_MAX_LENGTH + " characters."
            );
        }
        return normalizedReason;
    }

    private CapitalReferenceType toReferenceType(AllocationTargetType targetType) {
        return switch (targetType) {
            case TASK -> CapitalReferenceType.TASK;
        };
    }

    private BigDecimal money(long amount) {
        return BigDecimal.valueOf(amount).setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
    }

    private BigDecimal zero() {
        return BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
    }
}
