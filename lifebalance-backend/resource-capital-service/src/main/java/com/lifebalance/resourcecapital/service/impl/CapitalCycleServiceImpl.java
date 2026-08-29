package com.lifebalance.resourcecapital.service.impl;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capital.exception.CapitalNotSetupException;
import com.lifebalance.resourcecapital.domain.capital.exception.InvalidCapitalTransferException;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.ActiveCapitalCycleAlreadyExistsException;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.CapitalCycleDeletionNotAllowedException;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.CapitalCycleNotFoundException;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.CapitalCycleOverlapException;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.InvalidCapitalCycleStateException;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActionType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActorType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalHistory;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalReferenceType;
import com.lifebalance.resourcecapital.domain.moneycapital.MoneyCapital;
import com.lifebalance.resourcecapital.domain.timecapital.TimeCapital;
import com.lifebalance.resourcecapital.dto.CapitalCycleResponse;
import com.lifebalance.resourcecapital.dto.CloseCapitalCycleRequest;
import com.lifebalance.resourcecapital.dto.CreateCapitalCycleRequest;
import com.lifebalance.resourcecapital.dto.ReopenCapitalCycleRequest;
import com.lifebalance.resourcecapital.dto.TransferRemainingCapitalRequest;
import com.lifebalance.resourcecapital.dto.TransferRemainingCapitalResponse;
import com.lifebalance.resourcecapital.dto.UpdateCapitalCycleRequest;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalAdjustmentRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalAllocationRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalCycleRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalHistoryRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.MoneyCapitalRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.TimeCapitalRepository;
import com.lifebalance.resourcecapital.service.CapitalAllocationReader;
import com.lifebalance.resourcecapital.service.CapitalCycleBusinessValidator;
import com.lifebalance.resourcecapital.service.CapitalCycleService;
import com.lifebalance.resourcecapital.service.mapper.CapitalCycleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class CapitalCycleServiceImpl implements CapitalCycleService {

    private static final String ACTIVE_CYCLE_UNIQUE_INDEX = "uq_capital_cycles_owner_type_active";
    private static final int MONEY_SCALE = 4;
    private static final int REASON_MAX_LENGTH = 1000;

    private final CapitalCycleRepository capitalCycleRepository;
    private final CapitalCycleMapper capitalCycleMapper;
    private final CapitalCycleBusinessValidator capitalCycleBusinessValidator;
    private final TimeCapitalRepository timeCapitalRepository;
    private final MoneyCapitalRepository moneyCapitalRepository;
    private final CapitalAdjustmentRepository capitalAdjustmentRepository;
    private final CapitalAllocationRepository capitalAllocationRepository;
    private final CapitalAllocationReader capitalAllocationReader;
    private final CapitalHistoryRepository capitalHistoryRepository;
    private final Clock clock;

    @Autowired
    public CapitalCycleServiceImpl(
            CapitalCycleRepository capitalCycleRepository,
            CapitalCycleMapper capitalCycleMapper,
            CapitalCycleBusinessValidator capitalCycleBusinessValidator,
            TimeCapitalRepository timeCapitalRepository,
            MoneyCapitalRepository moneyCapitalRepository,
            CapitalAdjustmentRepository capitalAdjustmentRepository,
            CapitalAllocationRepository capitalAllocationRepository,
            CapitalAllocationReader capitalAllocationReader,
            CapitalHistoryRepository capitalHistoryRepository
    ) {
        this(
                capitalCycleRepository,
                capitalCycleMapper,
                capitalCycleBusinessValidator,
                timeCapitalRepository,
                moneyCapitalRepository,
                capitalAdjustmentRepository,
                capitalAllocationRepository,
                capitalAllocationReader,
                capitalHistoryRepository,
                Clock.systemUTC()
        );
    }

    CapitalCycleServiceImpl(
            CapitalCycleRepository capitalCycleRepository,
            CapitalCycleMapper capitalCycleMapper,
            CapitalCycleBusinessValidator capitalCycleBusinessValidator,
            TimeCapitalRepository timeCapitalRepository,
            MoneyCapitalRepository moneyCapitalRepository,
            CapitalAdjustmentRepository capitalAdjustmentRepository,
            CapitalAllocationRepository capitalAllocationRepository,
            CapitalAllocationReader capitalAllocationReader,
            CapitalHistoryRepository capitalHistoryRepository,
            Clock clock
    ) {
        this.capitalCycleRepository = capitalCycleRepository;
        this.capitalCycleMapper = capitalCycleMapper;
        this.capitalCycleBusinessValidator = capitalCycleBusinessValidator;
        this.timeCapitalRepository = timeCapitalRepository;
        this.moneyCapitalRepository = moneyCapitalRepository;
        this.capitalAdjustmentRepository = capitalAdjustmentRepository;
        this.capitalAllocationRepository = capitalAllocationRepository;
        this.capitalAllocationReader = capitalAllocationReader;
        this.capitalHistoryRepository = capitalHistoryRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<CapitalCycleResponse> listCycles(
            UUID ownerId,
            CapitalCycleType type,
            CapitalCycleStatus status,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    ) {
        Objects.requireNonNull(ownerId, "Owner id is required.");
        Objects.requireNonNull(pageable, "Pageable is required.");

        // The overview/list request commonly sends no optional filters. Avoid the
        // nullable-parameter JPQL query in that case because PostgreSQL may reject
        // untyped NULL bind parameters ("could not determine data type of parameter").
        if (type == null && status == null && fromDate == null && toDate == null) {
            return capitalCycleRepository.findByOwnerId(ownerId, pageable)
                    .map(capitalCycleMapper::toResponse);
        }

        return capitalCycleRepository.searchOwnedCycles(ownerId, type, status, fromDate, toDate, pageable)
                .map(capitalCycleMapper::toResponse);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<CapitalCycleResponse> getActiveCycle(UUID ownerId, CapitalCycleType type) {
        Objects.requireNonNull(ownerId, "Owner id is required.");

        Optional<CapitalCycle> activeCycle = type == null
                ? capitalCycleRepository.findFirstByOwnerIdAndStatusOrderByActivatedAtDescCreatedAtDesc(
                        ownerId,
                        CapitalCycleStatus.ACTIVE
                )
                : capitalCycleRepository.findByOwnerIdAndTypeAndStatus(
                        ownerId,
                        type,
                        CapitalCycleStatus.ACTIVE
                );
        return activeCycle.map(capitalCycleMapper::toResponse);
    }

    @Transactional(readOnly = true)
    @Override
    public CapitalCycleResponse getCycle(UUID ownerId, UUID cycleId) {
        return capitalCycleMapper.toResponse(findOwnedCycle(ownerId, cycleId));
    }

    @Transactional
    @Override
    public CapitalCycleResponse createCycle(UUID ownerId, CreateCapitalCycleRequest request) {
        Objects.requireNonNull(request, "Create capital cycle request is required.");
        CapitalCycle cycle = CapitalCycle.create(
                ownerId,
                request.getName(),
                request.getDescription(),
                request.getType(),
                request.getStartDate(),
                request.getEndDate()
        );
        ensureNoOverlappingCycle(
                ownerId,
                request.getType(),
                request.getStartDate(),
                request.getEndDate(),
                null
        );

        return capitalCycleMapper.toResponse(capitalCycleRepository.save(cycle));
    }

    @Transactional
    @Override
    public CapitalCycleResponse updateCycle(UUID ownerId, UUID cycleId, UpdateCapitalCycleRequest request) {
        Objects.requireNonNull(request, "Update capital cycle request is required.");
        CapitalCycle cycle = findOwnedCycle(ownerId, cycleId);

        cycle.validateUpdateInformation(
                request.getName(),
                request.getDescription(),
                request.getType(),
                request.getStartDate(),
                request.getEndDate()
        );

        ensureNoOverlappingCycle(
                ownerId,
                request.getType(),
                request.getStartDate(),
                request.getEndDate(),
                cycleId
        );

        cycle.updateInformation(
                request.getName(),
                request.getDescription(),
                request.getType(),
                request.getStartDate(),
                request.getEndDate()
        );

        return capitalCycleMapper.toResponse(cycle);
    }

    @Transactional
    @Override
    public CapitalCycleResponse activateCycle(UUID ownerId, UUID cycleId) {
        CapitalCycle cycle = findOwnedCycle(ownerId, cycleId);
        ensureTransitionAllowed(cycle, CapitalCycleStatus.ACTIVE, "activate");
        capitalCycleBusinessValidator.validateActivationAllowed(ownerId, cycle.getType(), cycle.getId());

        try {
            cycle.activate(clock.instant());
            capitalCycleRepository.saveAndFlush(cycle);
        } catch (DataIntegrityViolationException exception) {
            if (isActiveCycleUniqueViolation(exception)) {
                throw new ActiveCapitalCycleAlreadyExistsException(ownerId, cycle.getType());
            }
            throw exception;
        }

        return capitalCycleMapper.toResponse(cycle);
    }

    @Transactional
    @Override
    public CapitalCycleResponse closeCycle(UUID ownerId, UUID cycleId, CloseCapitalCycleRequest request) {
        Objects.requireNonNull(request, "Close capital cycle request is required.");
        CapitalCycle cycle = findOwnedCycle(ownerId, cycleId);

        cycle.close(request.getReason(), clock.instant());

        return capitalCycleMapper.toResponse(cycle);
    }

    @Transactional
    @Override
    public CapitalCycleResponse reopenCycle(UUID ownerId, UUID cycleId, ReopenCapitalCycleRequest request) {
        Objects.requireNonNull(request, "Reopen capital cycle request is required.");
        CapitalCycle cycle = findOwnedCycle(ownerId, cycleId);

        cycle.reopen(request.getReason(), clock.instant());

        return capitalCycleMapper.toResponse(cycle);
    }

    @Transactional
    @Override
    public TransferRemainingCapitalResponse transferRemainingCapital(
            UUID ownerId,
            UUID sourceCycleId,
            TransferRemainingCapitalRequest request
    ) {
        Objects.requireNonNull(ownerId, "Owner id is required.");
        Objects.requireNonNull(request, "Transfer remaining capital request is required.");

        UUID validatedSourceCycleId = requireCycleId(sourceCycleId, "sourceCycleId");
        UUID targetCycleId = requireCycleId(request.targetCycleId(), "targetCycleId");
        CapitalKind capitalType = requireCapitalType(request.capitalType());
        BigDecimal requestedAmount = requirePositiveMoney(request.amount());
        String reason = requireReason(request.reason());

        if (validatedSourceCycleId.equals(targetCycleId)) {
            throw InvalidCapitalTransferException.sameCycle(validatedSourceCycleId);
        }
        if (!request.transferConfirmed()) {
            throw InvalidCapitalTransferException.confirmationRequired(validatedSourceCycleId, targetCycleId);
        }

        TransferCyclePair cyclePair = findOwnedTransferCyclesForUpdate(ownerId, validatedSourceCycleId, targetCycleId);
        validateTransferCycles(cyclePair.source(), cyclePair.target());

        return switch (capitalType) {
            case TIME -> transferTimeRemaining(ownerId, cyclePair.source(), cyclePair.target(), requestedAmount, reason);
            case MONEY -> transferMoneyRemaining(ownerId, cyclePair.source(), cyclePair.target(), requestedAmount, reason);
        };
    }

    @Transactional
    @Override
    public void deleteCycle(UUID ownerId, UUID cycleId) {
        Objects.requireNonNull(ownerId, "Owner id is required.");
        CapitalCycle cycle = findOwnedCycleForUpdate(ownerId, cycleId);
        if (!cycle.isDraft()) {
            throw new CapitalCycleDeletionNotAllowedException(
                    cycleId,
                    "only draft capital cycles can be deleted"
            );
        }
        ensureNoDeletionDependencies(ownerId, cycleId);

        capitalCycleRepository.delete(cycle);
    }

    private CapitalCycle findOwnedCycle(UUID ownerId, UUID cycleId) {
        return capitalCycleRepository.findByIdAndOwnerId(cycleId, ownerId)
                .orElseThrow(() -> new CapitalCycleNotFoundException(cycleId));
    }

    private TransferCyclePair findOwnedTransferCyclesForUpdate(UUID ownerId, UUID sourceCycleId, UUID targetCycleId) {
        UUID firstCycleId = sourceCycleId.toString().compareTo(targetCycleId.toString()) <= 0
                ? sourceCycleId
                : targetCycleId;
        UUID secondCycleId = firstCycleId.equals(sourceCycleId)
                ? targetCycleId
                : sourceCycleId;

        CapitalCycle firstCycle = findOwnedCycleForUpdate(ownerId, firstCycleId);
        CapitalCycle secondCycle = findOwnedCycleForUpdate(ownerId, secondCycleId);

        CapitalCycle source = firstCycle.getId().equals(sourceCycleId) ? firstCycle : secondCycle;
        CapitalCycle target = firstCycle.getId().equals(targetCycleId) ? firstCycle : secondCycle;
        return new TransferCyclePair(source, target);
    }

    private CapitalCycle findOwnedCycleForUpdate(UUID ownerId, UUID cycleId) {
        return capitalCycleRepository.findByIdAndOwnerIdForUpdate(cycleId, ownerId)
                .orElseThrow(() -> new CapitalCycleNotFoundException(cycleId));
    }

    private void validateTransferCycles(CapitalCycle sourceCycle, CapitalCycle targetCycle) {
        if (!sourceCycle.isClosed()) {
            throw InvalidCapitalTransferException.sourceMustBeClosed(sourceCycle.getId(), sourceCycle.getStatus());
        }
        targetCycle.ensureCapitalAdjustmentAllowed();
        if (!targetCycle.getStartDate().isAfter(sourceCycle.getEndDate())) {
            throw InvalidCapitalTransferException.targetMustBeFuture(sourceCycle.getId(), targetCycle.getId());
        }
    }

    private TransferRemainingCapitalResponse transferTimeRemaining(
            UUID ownerId,
            CapitalCycle sourceCycle,
            CapitalCycle targetCycle,
            BigDecimal requestedAmount,
            String reason
    ) {
        long amountInMinutes = requireWholeMinutes(requestedAmount);
        TimeCapital sourceCapital = findTimeCapitalForUpdate(sourceCycle.getId());
        TimeCapital targetCapital = findTimeCapitalForUpdate(targetCycle.getId());

        BigDecimal sourceBefore = money(sourceCapital.getPlannedMinutes());
        BigDecimal targetBefore = money(targetCapital.getPlannedMinutes());
        BigDecimal remaining = sourceBefore
                .subtract(money(capitalAllocationReader.getAllocatedMinutes(ownerId, sourceCycle.getId())))
                .setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
        validateTransferAmount(sourceCycle.getId(), CapitalKind.TIME, remaining, requestedAmount);

        sourceCapital.decreasePlannedMinutes(amountInMinutes);
        targetCapital.increasePlannedMinutes(amountInMinutes);

        return recordTransferHistory(
                ownerId,
                sourceCycle,
                targetCycle,
                CapitalKind.TIME,
                requestedAmount,
                sourceBefore,
                money(sourceCapital.getPlannedMinutes()),
                targetBefore,
                money(targetCapital.getPlannedMinutes()),
                reason
        );
    }

    private TransferRemainingCapitalResponse transferMoneyRemaining(
            UUID ownerId,
            CapitalCycle sourceCycle,
            CapitalCycle targetCycle,
            BigDecimal requestedAmount,
            String reason
    ) {
        MoneyCapital sourceCapital = findMoneyCapitalForUpdate(sourceCycle.getId());
        MoneyCapital targetCapital = findMoneyCapitalForUpdate(targetCycle.getId());
        validateTransferCurrency(sourceCycle, targetCycle, sourceCapital, targetCapital);

        BigDecimal sourceBefore = sourceCapital.getPlannedAmount();
        BigDecimal targetBefore = targetCapital.getPlannedAmount();
        BigDecimal remaining = sourceBefore
                .subtract(capitalAllocationReader.getAllocatedAmount(ownerId, sourceCycle.getId()))
                .setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
        validateTransferAmount(sourceCycle.getId(), CapitalKind.MONEY, remaining, requestedAmount);

        sourceCapital.decreasePlannedAmount(requestedAmount);
        targetCapital.increasePlannedAmount(requestedAmount);

        return recordTransferHistory(
                ownerId,
                sourceCycle,
                targetCycle,
                CapitalKind.MONEY,
                requestedAmount,
                sourceBefore,
                sourceCapital.getPlannedAmount(),
                targetBefore,
                targetCapital.getPlannedAmount(),
                reason
        );
    }

    private TransferRemainingCapitalResponse recordTransferHistory(
            UUID ownerId,
            CapitalCycle sourceCycle,
            CapitalCycle targetCycle,
            CapitalKind capitalType,
            BigDecimal amount,
            BigDecimal sourceBefore,
            BigDecimal sourceAfter,
            BigDecimal targetBefore,
            BigDecimal targetAfter,
            String reason
    ) {
        CapitalHistory sourceHistory = capitalHistoryRepository.saveAndFlush(CapitalHistory.record(
                sourceCycle,
                capitalType,
                CapitalActionType.TRANSFER_REMAINING,
                amount,
                sourceBefore,
                sourceAfter,
                reason,
                "Transferred remaining capital to cycle " + targetCycle.getId() + ".",
                CapitalReferenceType.TARGET_CAPITAL_CYCLE,
                targetCycle.getId(),
                CapitalActorType.USER,
                ownerId
        ));
        CapitalHistory targetHistory = capitalHistoryRepository.saveAndFlush(CapitalHistory.record(
                targetCycle,
                capitalType,
                CapitalActionType.TRANSFER_REMAINING,
                amount,
                targetBefore,
                targetAfter,
                reason,
                "Received remaining capital from cycle " + sourceCycle.getId() + ".",
                CapitalReferenceType.TARGET_CAPITAL_CYCLE,
                sourceCycle.getId(),
                CapitalActorType.USER,
                ownerId
        ));

        return new TransferRemainingCapitalResponse(
                sourceCycle.getId(),
                targetCycle.getId(),
                capitalType,
                amount,
                sourceBefore,
                sourceAfter,
                targetBefore,
                targetAfter,
                reason,
                sourceHistory.getId(),
                targetHistory.getId(),
                sourceHistory.getCreatedAt()
        );
    }

    private void validateTransferAmount(
            UUID sourceCycleId,
            CapitalKind capitalType,
            BigDecimal remaining,
            BigDecimal requestedAmount
    ) {
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            throw InvalidCapitalTransferException.remainingNotPositive(sourceCycleId, capitalType, remaining);
        }
        if (requestedAmount.compareTo(remaining) > 0) {
            throw InvalidCapitalTransferException.amountExceedsRemaining(
                    sourceCycleId,
                    capitalType,
                    remaining,
                    requestedAmount
            );
        }
    }

    private TimeCapital findTimeCapitalForUpdate(UUID cycleId) {
        return timeCapitalRepository.findByCapitalCycleIdForUpdate(cycleId)
                .orElseThrow(() -> new CapitalNotSetupException(cycleId, CapitalKind.TIME));
    }

    private MoneyCapital findMoneyCapitalForUpdate(UUID cycleId) {
        return moneyCapitalRepository.findByCapitalCycleIdForUpdate(cycleId)
                .orElseThrow(() -> new CapitalNotSetupException(cycleId, CapitalKind.MONEY));
    }

    private void validateTransferCurrency(
            CapitalCycle sourceCycle,
            CapitalCycle targetCycle,
            MoneyCapital sourceCapital,
            MoneyCapital targetCapital
    ) {
        if (!sourceCapital.getCurrencyCode().equals(targetCapital.getCurrencyCode())) {
            throw InvalidCapitalTransferException.currencyMismatch(
                    sourceCycle.getId(),
                    targetCycle.getId(),
                    sourceCapital.getCurrencyCode(),
                    targetCapital.getCurrencyCode()
            );
        }
    }

    private UUID requireCycleId(UUID cycleId, String fieldName) {
        if (cycleId == null) {
            throw new InvalidCapitalTransferException(
                    fieldName + " is required.",
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    java.util.Map.of("field", fieldName)
            );
        }
        return cycleId;
    }

    private CapitalKind requireCapitalType(CapitalKind capitalType) {
        if (capitalType == null) {
            throw new InvalidCapitalTransferException(
                    "Capital type is required.",
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    java.util.Map.of("field", "capitalType")
            );
        }
        return capitalType;
    }

    private BigDecimal requirePositiveMoney(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidCapitalTransferException(
                    "Transfer amount is required.",
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    java.util.Map.of("field", "amount")
            );
        }
        BigDecimal normalizedAmount;
        try {
            normalizedAmount = amount.setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new InvalidCapitalTransferException(
                    "Transfer amount scale must not exceed " + MONEY_SCALE + ".",
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    java.util.Map.of("amount", String.valueOf(amount))
            );
        }
        if (normalizedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidCapitalTransferException(
                    "Transfer amount must be greater than zero.",
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    java.util.Map.of("amount", String.valueOf(normalizedAmount))
            );
        }
        return normalizedAmount;
    }

    private long requireWholeMinutes(BigDecimal amount) {
        try {
            return amount.toBigIntegerExact().longValueExact();
        } catch (ArithmeticException exception) {
            throw InvalidCapitalTransferException.timeAmountMustBeWholeMinutes(amount);
        }
    }

    private String requireReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new InvalidCapitalTransferException(
                    "Transfer reason is required.",
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    java.util.Map.of("field", "reason")
            );
        }
        String normalizedReason = reason.trim();
        if (normalizedReason.length() > REASON_MAX_LENGTH) {
            throw new InvalidCapitalTransferException(
                    "Transfer reason must not exceed " + REASON_MAX_LENGTH + " characters.",
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    java.util.Map.of("field", "reason")
            );
        }
        return normalizedReason;
    }

    private BigDecimal money(long amount) {
        return BigDecimal.valueOf(amount).setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
    }

    private void ensureNoOverlappingCycle(
            UUID ownerId,
            CapitalCycleType type,
            LocalDate startDate,
            LocalDate endDate,
            UUID excludedCycleId
    ) {
        if (capitalCycleRepository.existsOverlappingCycle(ownerId, type, startDate, endDate, excludedCycleId)) {
            throw new CapitalCycleOverlapException(ownerId, startDate, endDate);
        }
    }

    private void ensureTransitionAllowed(CapitalCycle cycle, CapitalCycleStatus targetStatus, String action) {
        if (!cycle.getStatus().canTransitionTo(targetStatus)) {
            throw new InvalidCapitalCycleStateException(cycle.getId(), cycle.getStatus(), targetStatus, action);
        }
    }

    private boolean isActiveCycleUniqueViolation(DataIntegrityViolationException exception) {
        Throwable mostSpecificCause = exception.getMostSpecificCause();
        String message = mostSpecificCause.getMessage();
        return message != null && message.contains(ACTIVE_CYCLE_UNIQUE_INDEX);
    }

    private void ensureNoDeletionDependencies(UUID ownerId, UUID cycleId) {
        if (timeCapitalRepository.existsByCapitalCycleId(cycleId)) {
            throw new CapitalCycleDeletionNotAllowedException(cycleId, "time capital is already initialized");
        }
        if (moneyCapitalRepository.existsByCapitalCycleId(cycleId)) {
            throw new CapitalCycleDeletionNotAllowedException(cycleId, "money capital is already initialized");
        }
        if (capitalAdjustmentRepository.existsByUserIdAndCapitalCycleId(ownerId, cycleId)) {
            throw new CapitalCycleDeletionNotAllowedException(cycleId, "capital adjustments already exist");
        }
        if (capitalAllocationRepository.existsByUserIdAndCapitalCycleId(ownerId, cycleId)) {
            throw new CapitalCycleDeletionNotAllowedException(cycleId, "capital allocations already exist");
        }
        if (capitalHistoryRepository.existsByCapitalCycleId(cycleId)) {
            throw new CapitalCycleDeletionNotAllowedException(cycleId, "capital history already exists");
        }
    }

    private record TransferCyclePair(CapitalCycle source, CapitalCycle target) {
    }

}