package com.lifebalance.resourcecapital.service.impl;

import com.lifebalance.resourcecapital.domain.capital.CapitalAdjustmentType;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capital.exception.CapitalBelowAllocatedException;
import com.lifebalance.resourcecapital.domain.capital.exception.CapitalNotSetupException;
import com.lifebalance.resourcecapital.domain.capital.exception.InvalidAdjustmentAmountException;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.CapitalCycleNotFoundException;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActionType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActorType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalHistory;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalReferenceType;
import com.lifebalance.resourcecapital.domain.moneycapital.MoneyCapital;
import com.lifebalance.resourcecapital.domain.timecapital.TimeCapital;
import com.lifebalance.resourcecapital.dto.AdjustMoneyCapitalRequest;
import com.lifebalance.resourcecapital.dto.AdjustTimeCapitalRequest;
import com.lifebalance.resourcecapital.dto.MoneyCapitalAdjustmentResponse;
import com.lifebalance.resourcecapital.dto.TimeCapitalAdjustmentResponse;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalCycleRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalHistoryRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.MoneyCapitalRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.TimeCapitalRepository;
import com.lifebalance.resourcecapital.service.CapitalAdjustmentService;
import com.lifebalance.resourcecapital.service.CapitalAllocationReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;

@Service
public class CapitalAdjustmentServiceImpl implements CapitalAdjustmentService {

    private static final int MONEY_SCALE = 4;
    private static final int REASON_MAX_LENGTH = 1000;

    private final CapitalCycleRepository capitalCycleRepository;
    private final TimeCapitalRepository timeCapitalRepository;
    private final MoneyCapitalRepository moneyCapitalRepository;
    private final CapitalHistoryRepository capitalHistoryRepository;
    private final CapitalAllocationReader capitalAllocationReader;

    public CapitalAdjustmentServiceImpl(
            CapitalCycleRepository capitalCycleRepository,
            TimeCapitalRepository timeCapitalRepository,
            MoneyCapitalRepository moneyCapitalRepository,
            CapitalHistoryRepository capitalHistoryRepository,
            CapitalAllocationReader capitalAllocationReader
    ) {
        this.capitalCycleRepository = capitalCycleRepository;
        this.timeCapitalRepository = timeCapitalRepository;
        this.moneyCapitalRepository = moneyCapitalRepository;
        this.capitalHistoryRepository = capitalHistoryRepository;
        this.capitalAllocationReader = capitalAllocationReader;
    }

    @Transactional
    @Override
    public TimeCapitalAdjustmentResponse adjustTimeCapital(
            UUID ownerId,
            UUID cycleId,
            AdjustTimeCapitalRequest request
    ) {
        Objects.requireNonNull(request, "Adjust time capital request is required.");
        CapitalAdjustmentType adjustmentType = requireAdjustmentType(request.adjustmentType());
        long amountInMinutes = requirePositiveMinutes(request.amountInMinutes());
        String reason = requireReason(request.reason());

        CapitalCycle cycle = findAdjustableOwnedCycle(ownerId, cycleId);
        TimeCapital timeCapital = findTimeCapital(cycleId);

        long beforeMinutes = timeCapital.getPlannedMinutes();
        CapitalActionType actionType = toActionType(adjustmentType);
        if (adjustmentType == CapitalAdjustmentType.INCREASE) {
            timeCapital.increasePlannedMinutes(amountInMinutes);
        } else {
            long afterMinutes = calculateTimeDecrease(beforeMinutes, amountInMinutes);
            long allocatedMinutes = capitalAllocationReader.getAllocatedMinutes(cycleId);
            if (afterMinutes < allocatedMinutes) {
                throw new CapitalBelowAllocatedException(
                        cycleId,
                        CapitalKind.TIME,
                        money(afterMinutes),
                        money(allocatedMinutes)
                );
            }
            timeCapital.decreasePlannedMinutes(amountInMinutes);
        }

        CapitalHistory history = capitalHistoryRepository.saveAndFlush(CapitalHistory.record(
                cycle,
                CapitalKind.TIME,
                actionType,
                money(amountInMinutes),
                money(beforeMinutes),
                money(timeCapital.getPlannedMinutes()),
                reason,
                null,
                CapitalReferenceType.MANUAL,
                null,
                CapitalActorType.USER,
                ownerId
        ));

        return new TimeCapitalAdjustmentResponse(
                cycleId,
                actionType,
                amountInMinutes,
                beforeMinutes,
                timeCapital.getPlannedMinutes(),
                reason,
                history.getId()
        );
    }

    @Transactional
    @Override
    public MoneyCapitalAdjustmentResponse adjustMoneyCapital(
            UUID ownerId,
            UUID cycleId,
            AdjustMoneyCapitalRequest request
    ) {
        Objects.requireNonNull(request, "Adjust money capital request is required.");
        CapitalAdjustmentType adjustmentType = requireAdjustmentType(request.adjustmentType());
        BigDecimal amount = requirePositiveMoney(request.amount());
        String reason = requireReason(request.reason());

        CapitalCycle cycle = findAdjustableOwnedCycle(ownerId, cycleId);
        MoneyCapital moneyCapital = findMoneyCapital(cycleId);

        BigDecimal beforeAmount = moneyCapital.getPlannedAmount();
        CapitalActionType actionType = toActionType(adjustmentType);
        if (adjustmentType == CapitalAdjustmentType.INCREASE) {
            moneyCapital.increasePlannedAmount(amount);
        } else {
            BigDecimal afterAmount = calculateMoneyDecrease(beforeAmount, amount);
            BigDecimal allocatedAmount = capitalAllocationReader.getAllocatedAmount(cycleId);
            if (afterAmount.compareTo(allocatedAmount) < 0) {
                throw new CapitalBelowAllocatedException(
                        cycleId,
                        CapitalKind.MONEY,
                        afterAmount,
                        allocatedAmount
                );
            }
            moneyCapital.decreasePlannedAmount(amount);
        }

        CapitalHistory history = capitalHistoryRepository.saveAndFlush(CapitalHistory.record(
                cycle,
                CapitalKind.MONEY,
                actionType,
                amount,
                beforeAmount,
                moneyCapital.getPlannedAmount(),
                reason,
                null,
                CapitalReferenceType.MANUAL,
                null,
                CapitalActorType.USER,
                ownerId
        ));

        return new MoneyCapitalAdjustmentResponse(
                cycleId,
                actionType,
                amount,
                beforeAmount,
                moneyCapital.getPlannedAmount(),
                moneyCapital.getCurrencyCode(),
                reason,
                history.getId()
        );
    }

    private CapitalCycle findAdjustableOwnedCycle(UUID ownerId, UUID cycleId) {
        CapitalCycle cycle = capitalCycleRepository.findByIdAndOwnerId(cycleId, ownerId)
                .orElseThrow(() -> new CapitalCycleNotFoundException(cycleId));
        cycle.ensureCapitalAdjustmentAllowed();
        return cycle;
    }

    private TimeCapital findTimeCapital(UUID cycleId) {
        return timeCapitalRepository.findByCapitalCycleId(cycleId)
                .orElseThrow(() -> new CapitalNotSetupException(cycleId, CapitalKind.TIME));
    }

    private MoneyCapital findMoneyCapital(UUID cycleId) {
        return moneyCapitalRepository.findByCapitalCycleId(cycleId)
                .orElseThrow(() -> new CapitalNotSetupException(cycleId, CapitalKind.MONEY));
    }

    private CapitalActionType toActionType(CapitalAdjustmentType adjustmentType) {
        return switch (adjustmentType) {
            case INCREASE -> CapitalActionType.ADJUSTMENT_INCREASE;
            case DECREASE -> CapitalActionType.ADJUSTMENT_DECREASE;
        };
    }

    private CapitalAdjustmentType requireAdjustmentType(CapitalAdjustmentType adjustmentType) {
        if (adjustmentType == null) {
            throw new InvalidAdjustmentAmountException("Adjustment type is required.");
        }
        return adjustmentType;
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
        if (amount == null) {
            throw InvalidAdjustmentAmountException.invalidMoney("amount is required");
        }
        try {
            amount = amount.setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw InvalidAdjustmentAmountException.invalidMoney("amount scale must not exceed " + MONEY_SCALE);
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw InvalidAdjustmentAmountException.invalidMoney("amount must be greater than zero");
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
                    "cannot decrease current planned amount " + beforeAmount + " by " + amount
            );
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
                    "Adjustment reason must not exceed " + REASON_MAX_LENGTH + " characters."
            );
        }
        return normalizedReason;
    }

    private BigDecimal money(long amount) {
        return BigDecimal.valueOf(amount).setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
    }
}
