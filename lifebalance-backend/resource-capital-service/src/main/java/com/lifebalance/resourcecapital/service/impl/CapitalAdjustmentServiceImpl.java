package com.lifebalance.resourcecapital.service.impl;

import com.lifebalance.resourcecapital.domain.capital.CapitalAdjustmentType;
import com.lifebalance.common.web.PageableLimits;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capital.exception.CapitalNotSetupException;
import com.lifebalance.resourcecapital.domain.capital.exception.InvalidAdjustmentAmountException;
import com.lifebalance.resourcecapital.domain.capitaladjustment.CapitalAdjustment;
import com.lifebalance.resourcecapital.domain.capitaladjustment.CapitalType;
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
import com.lifebalance.resourcecapital.dto.AdjustCapitalRequestDTO;
import com.lifebalance.resourcecapital.dto.AdjustMoneyCapitalRequest;
import com.lifebalance.resourcecapital.dto.AdjustTimeCapitalRequest;
import com.lifebalance.resourcecapital.dto.CapitalAdjustmentResponseDTO;
import com.lifebalance.resourcecapital.dto.MoneyCapitalAdjustmentResponse;
import com.lifebalance.resourcecapital.dto.TimeCapitalAdjustmentResponse;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalAdjustmentRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalAdjustmentSpecification;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalCycleRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalHistoryRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.MoneyCapitalRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.TimeCapitalRepository;
import com.lifebalance.resourcecapital.service.CapitalAdjustmentService;
import com.lifebalance.resourcecapital.service.CapitalAllocationReader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
public class CapitalAdjustmentServiceImpl implements CapitalAdjustmentService {

    private static final int MONEY_SCALE = 4;
    private static final int REASON_MAX_LENGTH = 1000;
    private static final Sort DEFAULT_SORT = Sort.by(
            Sort.Order.desc("createdAt"),
            Sort.Order.desc("id"));
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final CapitalCycleRepository capitalCycleRepository;
    private final TimeCapitalRepository timeCapitalRepository;
    private final MoneyCapitalRepository moneyCapitalRepository;
    private final CapitalAdjustmentRepository capitalAdjustmentRepository;
    private final CapitalHistoryRepository capitalHistoryRepository;
    private final CapitalAllocationReader capitalAllocationReader;

    public CapitalAdjustmentServiceImpl(
            CapitalCycleRepository capitalCycleRepository,
            TimeCapitalRepository timeCapitalRepository,
            MoneyCapitalRepository moneyCapitalRepository,
            CapitalAdjustmentRepository capitalAdjustmentRepository,
            CapitalHistoryRepository capitalHistoryRepository,
            CapitalAllocationReader capitalAllocationReader) {
        this.capitalCycleRepository = capitalCycleRepository;
        this.timeCapitalRepository = timeCapitalRepository;
        this.moneyCapitalRepository = moneyCapitalRepository;
        this.capitalAdjustmentRepository = capitalAdjustmentRepository;
        this.capitalHistoryRepository = capitalHistoryRepository;
        this.capitalAllocationReader = capitalAllocationReader;
    }

    @Transactional
    @Override
    public CapitalAdjustmentResponseDTO adjustCapital(UUID ownerId, AdjustCapitalRequestDTO request) {
        Objects.requireNonNull(ownerId, "Owner id is required.");
        Objects.requireNonNull(request, "Capital adjustment request is required.");
        UUID cycleId = requireCapitalCycleId(request.capitalCycleId());
        CapitalKind capitalType = requireCapitalType(request.capitalType());
        CapitalAdjustmentType adjustmentType = requireApiAdjustmentType(request.adjustmentType());
        String reason = requireReason(request.reason());
        CapitalCycle cycle = findAdjustableOwnedCycle(ownerId, cycleId);

        return switch (capitalType) {
            case TIME -> adjustTimeCapital(
                    cycle,
                    ownerId,
                    adjustmentType,
                    request.amount(),
                    reason,
                    request.overAllocationConfirmed());
            case MONEY -> adjustMoneyCapital(
                    cycle,
                    ownerId,
                    adjustmentType,
                    request.amount(),
                    reason,
                    request.overAllocationConfirmed());
        };
    }

    @Transactional
    @Override
    public TimeCapitalAdjustmentResponse adjustTimeCapital(
            UUID ownerId,
            UUID cycleId,
            AdjustTimeCapitalRequest request) {
        Objects.requireNonNull(request, "Adjust time capital request is required.");
        CapitalAdjustmentType adjustmentType = requireAdjustmentType(request.adjustmentType());
        long amountInMinutes = requirePositiveMinutes(request.amountInMinutes());
        String reason = requireReason(request.reason());

        CapitalCycle cycle = findAdjustableOwnedCycle(ownerId, cycleId);
        TimeCapital timeCapital = findTimeCapitalForUpdate(cycleId);

        long beforeMinutes = timeCapital.getPlannedMinutes();
        CapitalActionType actionType = toActionType(adjustmentType);
        AdjustmentOverAllocationResult overAllocation = AdjustmentOverAllocationResult.withinBalance();
        if (adjustmentType == CapitalAdjustmentType.INCREASE) {
            timeCapital.increasePlannedMinutes(amountInMinutes);
        } else {
            long afterMinutes = calculateTimeDecrease(beforeMinutes, amountInMinutes);
            long allocatedMinutes = capitalAllocationReader.getAllocatedMinutes(ownerId, cycleId);
            overAllocation = validateAdjustmentOverAllocation(
                    cycle,
                    CapitalKind.TIME,
                    money(beforeMinutes),
                    money(afterMinutes),
                    money(allocatedMinutes),
                    money(amountInMinutes),
                    request.allowOverAllocation());
            timeCapital.decreasePlannedMinutes(amountInMinutes);
        }

        CapitalAdjustmentResponseDTO adjustment = recordAdjustmentAndHistory(
                cycle,
                ownerId,
                CapitalKind.TIME,
                adjustmentType,
                actionType,
                money(beforeMinutes),
                money(timeCapital.getPlannedMinutes()),
                reason);
        recordOverAllocationApprovalHistory(
                cycle,
                ownerId,
                CapitalKind.TIME,
                overAllocation,
                reason,
                "Over-allocation approved for time capital adjustment.");

        return new TimeCapitalAdjustmentResponse(
                cycleId,
                actionType,
                amountInMinutes,
                beforeMinutes,
                timeCapital.getPlannedMinutes(),
                reason,
                adjustment.historyId());
    }

    @Transactional
    @Override
    public MoneyCapitalAdjustmentResponse adjustMoneyCapital(
            UUID ownerId,
            UUID cycleId,
            AdjustMoneyCapitalRequest request) {
        Objects.requireNonNull(request, "Adjust money capital request is required.");
        CapitalAdjustmentType adjustmentType = requireAdjustmentType(request.adjustmentType());
        BigDecimal amount = requirePositiveMoney(request.amount());
        String reason = requireReason(request.reason());

        CapitalCycle cycle = findAdjustableOwnedCycle(ownerId, cycleId);
        MoneyCapital moneyCapital = findMoneyCapitalForUpdate(cycleId);
        validateCurrencyMatchesCycle(request.currencyCode(), moneyCapital.getCurrencyCode());

        BigDecimal beforeAmount = moneyCapital.getPlannedAmount();
        CapitalActionType actionType = toActionType(adjustmentType);
        AdjustmentOverAllocationResult overAllocation = AdjustmentOverAllocationResult.withinBalance();
        if (adjustmentType == CapitalAdjustmentType.INCREASE) {
            moneyCapital.increasePlannedAmount(amount);
        } else {
            BigDecimal afterAmount = calculateMoneyDecrease(beforeAmount, amount);
            BigDecimal allocatedAmount = capitalAllocationReader.getAllocatedAmount(ownerId, cycleId);
            overAllocation = validateAdjustmentOverAllocation(
                    cycle,
                    CapitalKind.MONEY,
                    beforeAmount,
                    afterAmount,
                    allocatedAmount,
                    amount,
                    request.allowOverAllocation());
            moneyCapital.decreasePlannedAmount(amount);
        }

        CapitalAdjustmentResponseDTO adjustment = recordAdjustmentAndHistory(
                cycle,
                ownerId,
                CapitalKind.MONEY,
                adjustmentType,
                actionType,
                beforeAmount,
                moneyCapital.getPlannedAmount(),
                reason);
        recordOverAllocationApprovalHistory(
                cycle,
                ownerId,
                CapitalKind.MONEY,
                overAllocation,
                reason,
                "Over-allocation approved for money capital adjustment.");

        return new MoneyCapitalAdjustmentResponse(
                cycleId,
                actionType,
                amount,
                beforeAmount,
                moneyCapital.getPlannedAmount(),
                moneyCapital.getCurrencyCode(),
                reason,
                adjustment.historyId());
    }

    @Transactional(readOnly = true)
    @Override
    public Page<CapitalAdjustmentResponseDTO> getAdjustmentHistory(
            UUID ownerId,
            UUID capitalCycleId,
            CapitalKind capitalType,
            Pageable pageable) {
        Objects.requireNonNull(ownerId, "Owner id is required.");
        if (capitalCycleId != null) {
            findOwnedCycle(ownerId, capitalCycleId);
        }

        return capitalAdjustmentRepository.findAll(
                CapitalAdjustmentSpecification.filter(
                        ownerId,
                        capitalCycleId,
                        CapitalType.from(capitalType),
                        null,
                        null,
                        null),
                pageableWithDefaultSort(pageable))
                .map(this::toResponse);
    }

    private CapitalAdjustmentResponseDTO adjustTimeCapital(
            CapitalCycle cycle,
            UUID ownerId,
            CapitalAdjustmentType adjustmentType,
            BigDecimal requestedAmount,
            String reason,
            boolean overAllocationConfirmed) {
        TimeCapital timeCapital = findTimeCapitalForUpdate(cycle.getId());
        long amountInMinutes = adjustmentType == CapitalAdjustmentType.OVERRIDE
                ? requireWholeZeroOrPositiveMinutes(requestedAmount)
                : requireWholePositiveMinutes(requestedAmount);
        long beforeMinutes = timeCapital.getPlannedMinutes();
        AdjustmentOverAllocationResult overAllocation = AdjustmentOverAllocationResult.withinBalance();
        long afterMinutes = beforeMinutes;
        switch (adjustmentType) {
            case INCREASE -> afterMinutes = increaseTimeCapital(timeCapital, amountInMinutes);
            case DECREASE -> {
                long projectedMinutes = calculateTimeDecrease(beforeMinutes, amountInMinutes);
                long allocatedMinutes = capitalAllocationReader.getAllocatedMinutes(ownerId, cycle.getId());
                overAllocation = validateAdjustmentOverAllocation(
                        cycle,
                        CapitalKind.TIME,
                        money(beforeMinutes),
                        money(projectedMinutes),
                        money(allocatedMinutes),
                        money(amountInMinutes),
                        overAllocationConfirmed);
                timeCapital.decreasePlannedMinutes(amountInMinutes);
                afterMinutes = timeCapital.getPlannedMinutes();
            }
            case OVERRIDE -> {
                long allocatedMinutes = capitalAllocationReader.getAllocatedMinutes(ownerId, cycle.getId());
                overAllocation = validateAdjustmentOverAllocation(
                        cycle,
                        CapitalKind.TIME,
                        money(beforeMinutes),
                        money(amountInMinutes),
                        money(allocatedMinutes),
                        money(beforeMinutes).subtract(money(amountInMinutes)),
                        overAllocationConfirmed);
                applyTimeOverride(timeCapital, beforeMinutes, amountInMinutes);
                afterMinutes = timeCapital.getPlannedMinutes();
            }
        }

        CapitalAdjustmentResponseDTO response = recordAdjustmentAndHistory(
                cycle,
                ownerId,
                CapitalKind.TIME,
                adjustmentType,
                toHistoryActionType(adjustmentType),
                money(beforeMinutes),
                money(afterMinutes),
                reason);
        recordOverAllocationApprovalHistory(
                cycle,
                ownerId,
                CapitalKind.TIME,
                overAllocation,
                reason,
                "Over-allocation approved for time capital adjustment.");
        return response;
    }

    private CapitalAdjustmentResponseDTO adjustMoneyCapital(
            CapitalCycle cycle,
            UUID ownerId,
            CapitalAdjustmentType adjustmentType,
            BigDecimal requestedAmount,
            String reason,
            boolean overAllocationConfirmed) {
        MoneyCapital moneyCapital = findMoneyCapitalForUpdate(cycle.getId());
        BigDecimal amount = adjustmentType == CapitalAdjustmentType.OVERRIDE
                ? requireZeroOrPositiveMoney(requestedAmount)
                : requirePositiveMoney(requestedAmount);
        BigDecimal beforeAmount = moneyCapital.getPlannedAmount();
        AdjustmentOverAllocationResult overAllocation = AdjustmentOverAllocationResult.withinBalance();
        BigDecimal afterAmount = beforeAmount;
        switch (adjustmentType) {
            case INCREASE -> afterAmount = increaseMoneyCapital(moneyCapital, beforeAmount, amount);
            case DECREASE -> {
                BigDecimal projectedAmount = calculateMoneyDecrease(beforeAmount, amount);
                BigDecimal allocatedAmount = capitalAllocationReader.getAllocatedAmount(ownerId, cycle.getId());
                overAllocation = validateAdjustmentOverAllocation(
                        cycle,
                        CapitalKind.MONEY,
                        beforeAmount,
                        projectedAmount,
                        allocatedAmount,
                        amount,
                        overAllocationConfirmed);
                moneyCapital.decreasePlannedAmount(amount);
                afterAmount = projectedAmount;
            }
            case OVERRIDE -> {
                BigDecimal allocatedAmount = capitalAllocationReader.getAllocatedAmount(ownerId, cycle.getId());
                overAllocation = validateAdjustmentOverAllocation(
                        cycle,
                        CapitalKind.MONEY,
                        beforeAmount,
                        amount,
                        allocatedAmount,
                        beforeAmount.subtract(amount),
                        overAllocationConfirmed);
                applyMoneyOverride(moneyCapital, beforeAmount, amount);
                afterAmount = amount;
            }
        }

        CapitalAdjustmentResponseDTO response = recordAdjustmentAndHistory(
                cycle,
                ownerId,
                CapitalKind.MONEY,
                adjustmentType,
                toHistoryActionType(adjustmentType),
                beforeAmount,
                afterAmount,
                reason);
        recordOverAllocationApprovalHistory(
                cycle,
                ownerId,
                CapitalKind.MONEY,
                overAllocation,
                reason,
                "Over-allocation approved for money capital adjustment.");
        return response;
    }

    private long increaseTimeCapital(TimeCapital timeCapital, long amountInMinutes) {
        timeCapital.increasePlannedMinutes(amountInMinutes);
        return timeCapital.getPlannedMinutes();
    }

    private void applyTimeOverride(TimeCapital timeCapital, long beforeMinutes, long targetMinutes) {
        if (targetMinutes > beforeMinutes) {
            timeCapital.increasePlannedMinutes(Math.subtractExact(targetMinutes, beforeMinutes));
        } else if (targetMinutes < beforeMinutes) {
            timeCapital.decreasePlannedMinutes(Math.subtractExact(beforeMinutes, targetMinutes));
        }
    }

    private BigDecimal increaseMoneyCapital(
            MoneyCapital moneyCapital,
            BigDecimal beforeAmount,
            BigDecimal amount) {
        moneyCapital.increasePlannedAmount(amount);
        return beforeAmount.add(amount).setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
    }

    private void applyMoneyOverride(MoneyCapital moneyCapital, BigDecimal beforeAmount, BigDecimal targetAmount) {
        int comparison = targetAmount.compareTo(beforeAmount);
        if (comparison > 0) {
            moneyCapital.increasePlannedAmount(targetAmount.subtract(beforeAmount));
        } else if (comparison < 0) {
            moneyCapital.decreasePlannedAmount(beforeAmount.subtract(targetAmount));
        }
    }

    private AdjustmentOverAllocationResult validateAdjustmentOverAllocation(
            CapitalCycle cycle,
            CapitalKind capitalType,
            BigDecimal beforeAmount,
            BigDecimal afterAmount,
            BigDecimal allocatedAmount,
            BigDecimal requestedAmount,
            boolean overAllocationConfirmed) {
        BigDecimal normalizedBefore = normalizeMoney(beforeAmount);
        BigDecimal normalizedAfter = normalizeMoney(afterAmount);
        BigDecimal normalizedAllocated = normalizeMoney(allocatedAmount);
        BigDecimal availableCapital = normalizedBefore.subtract(normalizedAllocated)
                .setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
        BigDecimal remainingAfterAdjustment = normalizedAfter.subtract(normalizedAllocated)
                .setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
        if (remainingAfterAdjustment.compareTo(BigDecimal.ZERO) >= 0) {
            return AdjustmentOverAllocationResult.withinBalance();
        }

        BigDecimal normalizedRequested = normalizeMoney(requestedAmount);
        if (normalizedRequested.compareTo(BigDecimal.ZERO) <= 0) {
            normalizedRequested = remainingAfterAdjustment.abs().setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
        }
        if (!cycle.isOverAllocationAllowed()) {
            throw new OverAllocationNotAllowedException(
                    cycle.getId(),
                    capitalType,
                    availableCapital,
                    normalizedRequested,
                    remainingAfterAdjustment);
        }
        if (!overAllocationConfirmed) {
            throw new OverAllocationConfirmationRequiredException(
                    cycle.getId(),
                    capitalType,
                    availableCapital,
                    normalizedRequested,
                    remainingAfterAdjustment);
        }

        return new AdjustmentOverAllocationResult(
                availableCapital,
                normalizedRequested,
                remainingAfterAdjustment,
                true);
    }

    private CapitalAdjustmentResponseDTO recordAdjustmentAndHistory(
            CapitalCycle cycle,
            UUID ownerId,
            CapitalKind capitalType,
            CapitalAdjustmentType adjustmentType,
            CapitalActionType actionType,
            BigDecimal beforeAmount,
            BigDecimal afterAmount,
            String reason) {
        BigDecimal amountDelta = afterAmount.subtract(beforeAmount).setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
        CapitalAdjustment adjustment = capitalAdjustmentRepository.saveAndFlush(CapitalAdjustment.record(
                cycle,
                ownerId,
                capitalType,
                adjustmentType,
                beforeAmount,
                afterAmount,
                reason));

        CapitalHistory history = capitalHistoryRepository.saveAndFlush(CapitalHistory.record(
                cycle,
                capitalType,
                actionType,
                amountDelta.abs(),
                beforeAmount,
                afterAmount,
                reason,
                null,
                CapitalReferenceType.MANUAL,
                null,
                CapitalActorType.USER,
                ownerId));

        return new CapitalAdjustmentResponseDTO(
                adjustment.getId(),
                cycle.getId(),
                capitalType,
                adjustmentType,
                actionType,
                amountDelta,
                beforeAmount,
                afterAmount,
                reason,
                history.getId(),
                adjustment.getCreatedAt());
    }

    private UUID recordOverAllocationApprovalHistory(
            CapitalCycle cycle,
            UUID ownerId,
            CapitalKind capitalType,
            AdjustmentOverAllocationResult overAllocation,
            String reason,
            String description) {
        if (!overAllocation.overAllocated()) {
            return null;
        }
        CapitalHistory history = capitalHistoryRepository.saveAndFlush(CapitalHistory.record(
                cycle,
                capitalType,
                CapitalActionType.OVER_ALLOCATION_APPROVED,
                overAllocation.requestedAmount(),
                overAllocation.availableCapital(),
                overAllocation.remainingAfterAdjustment(),
                reason,
                description,
                CapitalReferenceType.MANUAL,
                null,
                CapitalActorType.USER,
                ownerId));
        return history.getId();
    }

    private CapitalAdjustmentResponseDTO toResponse(CapitalAdjustment adjustment) {
        return new CapitalAdjustmentResponseDTO(
                adjustment.getId(),
                adjustment.getCapitalCycle().getId(),
                adjustment.getCapitalType(),
                adjustment.getAdjustmentType(),
                toHistoryActionType(adjustment.getAdjustmentType()),
                adjustment.getAmountDelta(),
                adjustment.getPreviousAmount(),
                adjustment.getNewAmount(),
                adjustment.getReason(),
                null,
                adjustment.getCreatedAt());
    }

    private CapitalCycle findAdjustableOwnedCycle(UUID ownerId, UUID cycleId) {
        CapitalCycle cycle = findOwnedCycle(ownerId, cycleId);
        cycle.ensureCapitalAdjustmentAllowed();
        return cycle;
    }

    private CapitalCycle findOwnedCycle(UUID ownerId, UUID cycleId) {
        UUID validatedCycleId = requireCapitalCycleId(cycleId);
        return capitalCycleRepository.findByIdAndOwnerId(validatedCycleId, ownerId)
                .orElseThrow(() -> new CapitalCycleNotFoundException(validatedCycleId));
    }

    private TimeCapital findTimeCapitalForUpdate(UUID cycleId) {
        return timeCapitalRepository.findByCapitalCycleIdForUpdate(cycleId)
                .orElseThrow(() -> new CapitalNotSetupException(cycleId, CapitalKind.TIME));
    }

    private MoneyCapital findMoneyCapitalForUpdate(UUID cycleId) {
        return moneyCapitalRepository.findByCapitalCycleIdForUpdate(cycleId)
                .orElseThrow(() -> new CapitalNotSetupException(cycleId, CapitalKind.MONEY));
    }

    private CapitalActionType toActionType(CapitalAdjustmentType adjustmentType) {
        return switch (adjustmentType) {
            case INCREASE -> CapitalActionType.ADJUSTMENT_INCREASE;
            case DECREASE -> CapitalActionType.ADJUSTMENT_DECREASE;
            case OVERRIDE -> throw new InvalidAdjustmentAmountException(
                    "Override adjustment is not supported by this service.");
        };
    }

    private CapitalActionType toHistoryActionType(CapitalAdjustmentType adjustmentType) {
        return switch (adjustmentType) {
            case INCREASE -> CapitalActionType.ADJUSTMENT_INCREASE;
            case DECREASE -> CapitalActionType.ADJUSTMENT_DECREASE;
            case OVERRIDE -> CapitalActionType.CAPITAL_SET;
        };
    }

    private CapitalAdjustmentType requireAdjustmentType(CapitalAdjustmentType adjustmentType) {
        if (adjustmentType == null) {
            throw new InvalidAdjustmentAmountException("Adjustment type is required.");
        }
        if (adjustmentType == CapitalAdjustmentType.OVERRIDE) {
            throw new InvalidAdjustmentAmountException("Override adjustment is not supported by this service.");
        }
        return adjustmentType;
    }

    private CapitalAdjustmentType requireApiAdjustmentType(CapitalAdjustmentType adjustmentType) {
        if (adjustmentType == null) {
            throw new InvalidAdjustmentAmountException("Adjustment type is required.");
        }
        return adjustmentType;
    }

    private UUID requireCapitalCycleId(UUID cycleId) {
        if (cycleId == null) {
            throw new InvalidAdjustmentAmountException("Capital cycle id is required.");
        }
        return cycleId;
    }

    private CapitalKind requireCapitalType(CapitalKind capitalType) {
        if (capitalType == null) {
            throw new InvalidAdjustmentAmountException("Capital type is required.");
        }
        return capitalType;
    }

    private long requireWholePositiveMinutes(BigDecimal amount) {
        BigDecimal normalizedAmount = requirePositiveMoney(amount);
        try {
            return normalizedAmount.toBigIntegerExact().longValueExact();
        } catch (ArithmeticException exception) {
            throw InvalidAdjustmentAmountException.invalidMoney(
                    "Time adjustment amount must be whole minutes.");
        }
    }

    private long requireWholeZeroOrPositiveMinutes(BigDecimal amount) {
        BigDecimal normalizedAmount = requireZeroOrPositiveMoney(amount);
        try {
            return normalizedAmount.toBigIntegerExact().longValueExact();
        } catch (ArithmeticException exception) {
            throw InvalidAdjustmentAmountException.invalidMoney(
                    "Time adjustment amount must be whole minutes.");
        }
    }

    private long requirePositiveMinutes(Long amountInMinutes) {
        if (amountInMinutes == null) {
            throw new InvalidAdjustmentAmountException("Time adjustment amount is required.");
        }
        if (amountInMinutes <= 0) {
            throw InvalidAdjustmentAmountException.nonPositiveTime(amountInMinutes);
        }
        return amountInMinutes;
    }

    private BigDecimal requirePositiveMoney(BigDecimal amount) {
        BigDecimal normalizedAmount = requireZeroOrPositiveMoney(amount);
        if (normalizedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw InvalidAdjustmentAmountException.invalidMoney("amount must be greater than zero");
        }
        return normalizedAmount;
    }

    private BigDecimal requireZeroOrPositiveMoney(BigDecimal amount) {
        if (amount == null) {
            throw InvalidAdjustmentAmountException.invalidMoney("amount is required");
        }
        try {
            amount = amount.setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw InvalidAdjustmentAmountException.invalidMoney("amount scale must not exceed " + MONEY_SCALE);
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw InvalidAdjustmentAmountException.invalidMoney("amount must be greater than or equal to zero");
        }
        return amount;
    }

    private long calculateTimeDecrease(long beforeMinutes, long amountInMinutes) {
        long afterMinutes = beforeMinutes - amountInMinutes;
        if (afterMinutes < 0) {
            throw InvalidAdjustmentAmountException.timeBelowZero(beforeMinutes, amountInMinutes);
        }
        return afterMinutes;
    }

    private BigDecimal calculateMoneyDecrease(BigDecimal beforeAmount, BigDecimal amount) {
        BigDecimal afterAmount = beforeAmount.subtract(amount);
        if (afterAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw InvalidAdjustmentAmountException.invalidMoney(
                    "cannot decrease current planned amount " + beforeAmount + " by " + amount);
        }
        return afterAmount;
    }

    private String requireReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new InvalidAdjustmentAmountException("Adjustment reason is required.");
        }
        String normalizedReason = reason.trim();
        if (normalizedReason.length() > REASON_MAX_LENGTH) {
            throw new InvalidAdjustmentAmountException(
                    "Adjustment reason must not exceed " + REASON_MAX_LENGTH + " characters.");
        }
        return normalizedReason;
    }

    private void validateCurrencyMatchesCycle(String requestedCurrencyCode, String cycleCurrencyCode) {
        if (requestedCurrencyCode == null || requestedCurrencyCode.isBlank()) {
            return;
        }
        String normalizedCurrencyCode = requestedCurrencyCode.trim().toUpperCase(Locale.ROOT);
        if (!normalizedCurrencyCode.matches("[A-Z]{3}")) {
            throw InvalidAdjustmentAmountException.invalidMoney(
                    "currencyCode must contain exactly three letters"
            );
        }
        if (!normalizedCurrencyCode.equals(cycleCurrencyCode)) {
            throw InvalidAdjustmentAmountException.invalidMoney(
                    "currencyCode " + normalizedCurrencyCode
                            + " must match cycle money capital currency " + cycleCurrencyCode
            );
        }
    }

    private BigDecimal money(long amount) {
        return BigDecimal.valueOf(amount).setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
    }

    private BigDecimal normalizeMoney(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
        }
        return amount.setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
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

    private record AdjustmentOverAllocationResult(
            BigDecimal availableCapital,
            BigDecimal requestedAmount,
            BigDecimal remainingAfterAdjustment,
            boolean overAllocated) {

        private static AdjustmentOverAllocationResult withinBalance() {
            BigDecimal zero = BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
            return new AdjustmentOverAllocationResult(zero, zero, zero, false);
        }

    }
}
