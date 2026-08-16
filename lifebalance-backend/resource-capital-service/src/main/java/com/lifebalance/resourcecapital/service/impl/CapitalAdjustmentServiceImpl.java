package com.lifebalance.resourcecapital.service.impl;

import com.lifebalance.resourcecapital.domain.capital.CapitalAdjustmentType;
import com.lifebalance.common.web.PageableLimits;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capital.exception.CapitalBelowAllocatedException;
import com.lifebalance.resourcecapital.domain.capital.exception.CapitalNotSetupException;
import com.lifebalance.resourcecapital.domain.capital.exception.InvalidAdjustmentAmountException;
import com.lifebalance.resourcecapital.domain.capitaladjustment.CapitalAdjustment;
import com.lifebalance.resourcecapital.domain.capitaladjustment.CapitalType;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.CapitalCycleNotFoundException;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.InvalidCapitalCycleStateException;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActionType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActorType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalHistory;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalReferenceType;
import com.lifebalance.resourcecapital.domain.moneycapital.MoneyCapital;
import com.lifebalance.resourcecapital.domain.timecapital.TimeCapital;
import com.lifebalance.resourcecapital.dto.AdjustMoneyCapitalRequest;
import com.lifebalance.resourcecapital.dto.AdjustTimeCapitalRequest;
import com.lifebalance.resourcecapital.dto.CapitalAdjustmentRequest;
import com.lifebalance.resourcecapital.dto.CapitalAdjustmentResponse;
import com.lifebalance.resourcecapital.dto.MoneyCapitalAdjustmentResponse;
import com.lifebalance.resourcecapital.dto.TimeCapitalAdjustmentResponse;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalAdjustmentRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalCycleRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalHistoryRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.MoneyCapitalRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.TimeCapitalRepository;
import com.lifebalance.resourcecapital.service.CapitalAdjustmentService;
import com.lifebalance.resourcecapital.service.CapitalAllocationReader;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class CapitalAdjustmentServiceImpl implements CapitalAdjustmentService {

    private static final int MONEY_SCALE = 4;
    private static final int REASON_MAX_LENGTH = 1000;
    private static final Sort DEFAULT_SORT = Sort.by(
            Sort.Order.desc("createdAt"),
            Sort.Order.desc("id")
    );
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
            CapitalAllocationReader capitalAllocationReader
    ) {
        this.capitalCycleRepository = capitalCycleRepository;
        this.timeCapitalRepository = timeCapitalRepository;
        this.moneyCapitalRepository = moneyCapitalRepository;
        this.capitalAdjustmentRepository = capitalAdjustmentRepository;
        this.capitalHistoryRepository = capitalHistoryRepository;
        this.capitalAllocationReader = capitalAllocationReader;
    }

    @Transactional
    @Override
    public CapitalAdjustmentResponse adjustCapital(UUID ownerId, CapitalAdjustmentRequest request) {
        Objects.requireNonNull(request, "Capital adjustment request is required.");
        UUID cycleId = requireCapitalCycleId(request.capitalCycleId());
        CapitalKind capitalType = requireCapitalType(request.capitalType());
        CapitalAdjustmentType adjustmentType = requireApiAdjustmentType(request.adjustmentType());
        String reason = requireReason(request.reason());
        CapitalCycle cycle = findActiveOwnedCycle(ownerId, cycleId);

        return switch (capitalType) {
            case TIME -> adjustTimeCapital(cycle, ownerId, adjustmentType, request.amount(), reason);
            case MONEY -> adjustMoneyCapital(cycle, ownerId, adjustmentType, request.amount(), reason);
        };
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

    @Transactional(readOnly = true)
    @Override
    public Page<CapitalAdjustmentResponse> getAdjustments(
            UUID ownerId,
            UUID capitalCycleId,
            CapitalKind capitalType,
            Pageable pageable
    ) {
        Objects.requireNonNull(ownerId, "Owner id is required.");
        if (capitalCycleId != null) {
            findOwnedCycle(ownerId, capitalCycleId);
        }

        return capitalAdjustmentRepository.findAll(
                        specification(ownerId, capitalCycleId, capitalType),
                        pageableWithDefaultSort(pageable)
                )
                .map(this::toResponse);
    }

    private CapitalAdjustmentResponse adjustTimeCapital(
            CapitalCycle cycle,
            UUID ownerId,
            CapitalAdjustmentType adjustmentType,
            BigDecimal requestedAmount,
            String reason
    ) {
        TimeCapital timeCapital = findTimeCapital(cycle.getId());
        long amountInMinutes = requireWholePositiveMinutes(requestedAmount);
        long beforeMinutes = timeCapital.getPlannedMinutes();
        long afterMinutes = switch (adjustmentType) {
            case INCREASE -> increaseTimeCapital(timeCapital, amountInMinutes);
            case DECREASE -> decreaseTimeCapital(cycle.getId(), timeCapital, beforeMinutes, amountInMinutes);
            case OVERRIDE -> overrideTimeCapital(cycle.getId(), timeCapital, beforeMinutes, amountInMinutes);
        };

        return recordAdjustmentAndHistory(
                cycle,
                ownerId,
                CapitalKind.TIME,
                adjustmentType,
                toHistoryActionType(adjustmentType),
                money(amountInMinutes),
                money(beforeMinutes),
                money(afterMinutes),
                reason
        );
    }

    private CapitalAdjustmentResponse adjustMoneyCapital(
            CapitalCycle cycle,
            UUID ownerId,
            CapitalAdjustmentType adjustmentType,
            BigDecimal requestedAmount,
            String reason
    ) {
        MoneyCapital moneyCapital = findMoneyCapital(cycle.getId());
        BigDecimal amount = requirePositiveMoney(requestedAmount);
        BigDecimal beforeAmount = moneyCapital.getPlannedAmount();
        BigDecimal afterAmount = switch (adjustmentType) {
            case INCREASE -> increaseMoneyCapital(moneyCapital, beforeAmount, amount);
            case DECREASE -> decreaseMoneyCapital(cycle.getId(), moneyCapital, beforeAmount, amount);
            case OVERRIDE -> overrideMoneyCapital(cycle.getId(), moneyCapital, beforeAmount, amount);
        };

        return recordAdjustmentAndHistory(
                cycle,
                ownerId,
                CapitalKind.MONEY,
                adjustmentType,
                toHistoryActionType(adjustmentType),
                amount,
                beforeAmount,
                afterAmount,
                reason
        );
    }

    private long increaseTimeCapital(TimeCapital timeCapital, long amountInMinutes) {
        timeCapital.increasePlannedMinutes(amountInMinutes);
        return timeCapital.getPlannedMinutes();
    }

    private long decreaseTimeCapital(
            UUID cycleId,
            TimeCapital timeCapital,
            long beforeMinutes,
            long amountInMinutes
    ) {
        long afterMinutes = calculateTimeDecrease(beforeMinutes, amountInMinutes);
        ensureTimeNotBelowAllocated(cycleId, afterMinutes);
        timeCapital.decreasePlannedMinutes(amountInMinutes);
        return timeCapital.getPlannedMinutes();
    }

    private long overrideTimeCapital(
            UUID cycleId,
            TimeCapital timeCapital,
            long beforeMinutes,
            long targetMinutes
    ) {
        ensureTimeNotBelowAllocated(cycleId, targetMinutes);
        if (targetMinutes > beforeMinutes) {
            timeCapital.increasePlannedMinutes(Math.subtractExact(targetMinutes, beforeMinutes));
        } else if (targetMinutes < beforeMinutes) {
            timeCapital.decreasePlannedMinutes(Math.subtractExact(beforeMinutes, targetMinutes));
        }
        return timeCapital.getPlannedMinutes();
    }

    private BigDecimal increaseMoneyCapital(
            MoneyCapital moneyCapital,
            BigDecimal beforeAmount,
            BigDecimal amount
    ) {
        moneyCapital.increasePlannedAmount(amount);
        return beforeAmount.add(amount).setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
    }

    private BigDecimal decreaseMoneyCapital(
            UUID cycleId,
            MoneyCapital moneyCapital,
            BigDecimal beforeAmount,
            BigDecimal amount
    ) {
        BigDecimal afterAmount = calculateMoneyDecrease(beforeAmount, amount);
        ensureMoneyNotBelowAllocated(cycleId, afterAmount);
        moneyCapital.decreasePlannedAmount(amount);
        return afterAmount;
    }

    private BigDecimal overrideMoneyCapital(
            UUID cycleId,
            MoneyCapital moneyCapital,
            BigDecimal beforeAmount,
            BigDecimal targetAmount
    ) {
        ensureMoneyNotBelowAllocated(cycleId, targetAmount);
        int comparison = targetAmount.compareTo(beforeAmount);
        if (comparison > 0) {
            moneyCapital.increasePlannedAmount(targetAmount.subtract(beforeAmount));
        } else if (comparison < 0) {
            moneyCapital.decreasePlannedAmount(beforeAmount.subtract(targetAmount));
        }
        return targetAmount;
    }

    private void ensureTimeNotBelowAllocated(UUID cycleId, long afterMinutes) {
        long allocatedMinutes = capitalAllocationReader.getAllocatedMinutes(cycleId);
        if (afterMinutes < allocatedMinutes) {
            throw new CapitalBelowAllocatedException(
                    cycleId,
                    CapitalKind.TIME,
                    money(afterMinutes),
                    money(allocatedMinutes)
            );
        }
    }

    private void ensureMoneyNotBelowAllocated(UUID cycleId, BigDecimal afterAmount) {
        BigDecimal allocatedAmount = capitalAllocationReader.getAllocatedAmount(cycleId);
        if (afterAmount.compareTo(allocatedAmount) < 0) {
            throw new CapitalBelowAllocatedException(
                    cycleId,
                    CapitalKind.MONEY,
                    afterAmount,
                    allocatedAmount
            );
        }
    }

    private CapitalAdjustmentResponse recordAdjustmentAndHistory(
            CapitalCycle cycle,
            UUID ownerId,
            CapitalKind capitalType,
            CapitalAdjustmentType adjustmentType,
            CapitalActionType actionType,
            BigDecimal amount,
            BigDecimal beforeAmount,
            BigDecimal afterAmount,
            String reason
    ) {
        CapitalAdjustment adjustment = capitalAdjustmentRepository.saveAndFlush(CapitalAdjustment.record(
                cycle,
                ownerId,
                capitalType,
                adjustmentType,
                beforeAmount,
                afterAmount,
                reason
        ));

        CapitalHistory history = capitalHistoryRepository.saveAndFlush(CapitalHistory.record(
                cycle,
                capitalType,
                actionType,
                amount,
                beforeAmount,
                afterAmount,
                reason,
                null,
                CapitalReferenceType.MANUAL,
                null,
                CapitalActorType.USER,
                ownerId
        ));

        return new CapitalAdjustmentResponse(
                adjustment.getId(),
                cycle.getId(),
                capitalType,
                adjustmentType,
                actionType,
                amount,
                beforeAmount,
                afterAmount,
                reason,
                history.getId(),
                adjustment.getCreatedAt()
        );
    }

    private CapitalAdjustmentResponse toResponse(CapitalAdjustment adjustment) {
        return new CapitalAdjustmentResponse(
                adjustment.getId(),
                adjustment.getCapitalCycle().getId(),
                adjustment.getCapitalType(),
                adjustment.getAdjustmentType(),
                toHistoryActionType(adjustment.getAdjustmentType()),
                adjustment.getAmount(),
                adjustment.getPreviousAmount(),
                adjustment.getNewAmount(),
                adjustment.getReason(),
                null,
                adjustment.getCreatedAt()
        );
    }

    private CapitalCycle findActiveOwnedCycle(UUID ownerId, UUID cycleId) {
        CapitalCycle cycle = findOwnedCycle(ownerId, cycleId);
        if (cycle.getStatus() != CapitalCycleStatus.ACTIVE) {
            throw new InvalidCapitalCycleStateException(
                    cycle.getId(),
                    cycle.getStatus(),
                    "adjust capital",
                    "capital adjustment APIs require an ACTIVE cycle"
            );
        }
        return cycle;
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
            case OVERRIDE -> throw new InvalidAdjustmentAmountException(
                    "Override adjustment is not supported by this service."
            );
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
                    "Time adjustment amount must be whole minutes."
            );
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

    private Specification<CapitalAdjustment> specification(
            UUID ownerId,
            UUID capitalCycleId,
            CapitalKind capitalType
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("capitalCycle").get("ownerId"), ownerId));
            if (capitalCycleId != null) {
                predicates.add(criteriaBuilder.equal(root.get("capitalCycle").get("id"), capitalCycleId));
            }
            if (capitalType != null) {
                predicates.add(criteriaBuilder.equal(root.get("capitalType"), CapitalType.from(capitalType)));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
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
}
