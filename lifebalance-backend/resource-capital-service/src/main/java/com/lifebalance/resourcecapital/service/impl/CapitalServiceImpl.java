package com.lifebalance.resourcecapital.service.impl;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capital.exception.CapitalAlreadyInitializedException;
import com.lifebalance.resourcecapital.domain.capital.exception.CapitalAllocationDataIntegrityException;
import com.lifebalance.resourcecapital.domain.capital.exception.CapitalNotSetupException;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.CapitalCycleNotFoundException;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActionType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActorType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalHistory;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalReferenceType;
import com.lifebalance.resourcecapital.domain.moneycapital.MoneyCapital;
import com.lifebalance.resourcecapital.domain.timecapital.TimeCapital;
import com.lifebalance.resourcecapital.dto.CapitalBalanceResponse;
import com.lifebalance.resourcecapital.dto.CapitalBalanceSummaryDto;
import com.lifebalance.resourcecapital.dto.CapitalOverviewResponse;
import com.lifebalance.resourcecapital.dto.CapitalSummaryResponseDTO;
import com.lifebalance.resourcecapital.dto.MoneyCapitalResponse;
import com.lifebalance.resourcecapital.dto.SetupMoneyCapitalRequest;
import com.lifebalance.resourcecapital.dto.SetupTimeCapitalRequest;
import com.lifebalance.resourcecapital.dto.TimeCapitalResponse;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalCycleRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalHistoryRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.MoneyCapitalRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.TimeCapitalRepository;
import com.lifebalance.resourcecapital.service.CapitalBalanceService;
import com.lifebalance.resourcecapital.service.CapitalService;
import com.lifebalance.resourcecapital.service.mapper.CapitalMapper;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;

@Service
public class CapitalServiceImpl implements CapitalService {

    private static final int MONEY_SCALE = 4;
    private static final int HOURS_SCALE = 4;
    private static final BigDecimal SIXTY = new BigDecimal("60");
    private static final String TIME_CAPITAL_CYCLE_UNIQUE_CONSTRAINT = "uk_time_capitals_cycle";
    private static final String MONEY_CAPITAL_CYCLE_UNIQUE_CONSTRAINT = "uk_money_capitals_cycle";

    private final CapitalCycleRepository capitalCycleRepository;
    private final TimeCapitalRepository timeCapitalRepository;
    private final MoneyCapitalRepository moneyCapitalRepository;
    private final CapitalHistoryRepository capitalHistoryRepository;
    private final CapitalBalanceService capitalBalanceService;
    private final CapitalMapper capitalMapper;

    public CapitalServiceImpl(
            CapitalCycleRepository capitalCycleRepository,
            TimeCapitalRepository timeCapitalRepository,
            MoneyCapitalRepository moneyCapitalRepository,
            CapitalHistoryRepository capitalHistoryRepository,
            CapitalBalanceService capitalBalanceService,
            CapitalMapper capitalMapper
    ) {
        this.capitalCycleRepository = capitalCycleRepository;
        this.timeCapitalRepository = timeCapitalRepository;
        this.moneyCapitalRepository = moneyCapitalRepository;
        this.capitalHistoryRepository = capitalHistoryRepository;
        this.capitalBalanceService = capitalBalanceService;
        this.capitalMapper = capitalMapper;
    }

    @Transactional
    @Override
    public TimeCapitalResponse setupTimeCapital(UUID ownerId, UUID cycleId, SetupTimeCapitalRequest request) {
        Objects.requireNonNull(request, "Setup time capital request is required.");
        CapitalCycle cycle = findOwnedCycle(ownerId, cycleId);
        ensureTimeCapitalNotInitialized(cycleId);

        TimeCapital timeCapital = TimeCapital.create(cycle, request.plannedMinutes());
        try {
            TimeCapital savedCapital = timeCapitalRepository.saveAndFlush(timeCapital);
            recordSetupHistory(
                    cycle,
                    CapitalKind.TIME,
                    money(savedCapital.getPlannedMinutes()),
                    "Setup time capital",
                    ownerId
            );
            return capitalMapper.toTimeResponse(savedCapital);
        } catch (DataIntegrityViolationException exception) {
            if (!isConstraintViolation(exception, TIME_CAPITAL_CYCLE_UNIQUE_CONSTRAINT)) {
                throw exception;
            }
            throw new CapitalAlreadyInitializedException(cycleId, CapitalKind.TIME, exception);
        }
    }

    @Transactional
    @Override
    public MoneyCapitalResponse setupMoneyCapital(UUID ownerId, UUID cycleId, SetupMoneyCapitalRequest request) {
        Objects.requireNonNull(request, "Setup money capital request is required.");
        CapitalCycle cycle = findOwnedCycle(ownerId, cycleId);
        ensureMoneyCapitalNotInitialized(cycleId);

        MoneyCapital moneyCapital = MoneyCapital.create(cycle, request.plannedAmount(), request.currencyCode());
        try {
            MoneyCapital savedCapital = moneyCapitalRepository.saveAndFlush(moneyCapital);
            recordSetupHistory(
                    cycle,
                    CapitalKind.MONEY,
                    savedCapital.getPlannedAmount(),
                    "Setup money capital",
                    ownerId
            );
            return capitalMapper.toMoneyResponse(savedCapital);
        } catch (DataIntegrityViolationException exception) {
            if (!isConstraintViolation(exception, MONEY_CAPITAL_CYCLE_UNIQUE_CONSTRAINT)) {
                throw exception;
            }
            throw new CapitalAlreadyInitializedException(cycleId, CapitalKind.MONEY, exception);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public TimeCapitalResponse getAvailableTimeCapital(UUID ownerId, UUID cycleId) {
        findOwnedCycle(ownerId, cycleId);
        return capitalMapper.toTimeResponse(findTimeCapital(cycleId));
    }

    @Transactional(readOnly = true)
    @Override
    public MoneyCapitalResponse getAvailableMoneyCapital(UUID ownerId, UUID cycleId) {
        findOwnedCycle(ownerId, cycleId);
        return capitalMapper.toMoneyResponse(findMoneyCapital(cycleId));
    }

    @Transactional(readOnly = true)
    @Override
    public TimeCapitalResponse getRemainingTimeCapital(UUID ownerId, UUID cycleId) {
        findOwnedCycle(ownerId, cycleId);
        return capitalMapper.toTimeResponse(findTimeCapital(cycleId));
    }

    @Transactional(readOnly = true)
    @Override
    public MoneyCapitalResponse getRemainingMoneyCapital(UUID ownerId, UUID cycleId) {
        findOwnedCycle(ownerId, cycleId);
        return capitalMapper.toMoneyResponse(findMoneyCapital(cycleId));
    }

    @Transactional(readOnly = true)
    @Override
    public CapitalOverviewResponse getCapitalOverview(UUID ownerId, UUID cycleId) {
        CapitalCycle cycle = findOwnedCycle(ownerId, cycleId);
        TimeCapitalResponse timeCapital = timeCapitalRepository.findByCapitalCycleId(cycleId)
                .map(capitalMapper::toTimeResponse)
                .orElseGet(() -> capitalMapper.uninitializedTimeResponse(cycleId));
        MoneyCapitalResponse moneyCapital = moneyCapitalRepository.findByCapitalCycleId(cycleId)
                .map(capitalMapper::toMoneyResponse)
                .orElseGet(() -> capitalMapper.uninitializedMoneyResponse(cycleId));

        return capitalMapper.toOverview(cycle, timeCapital, moneyCapital);
    }

    @Transactional(readOnly = true)
    @Override
    public CapitalSummaryResponseDTO getCapitalSummary(UUID ownerId) {
        Objects.requireNonNull(ownerId, "Owner id is required.");
        return capitalCycleRepository
                .findFirstByOwnerIdAndStatusOrderByActivatedAtDescCreatedAtDesc(
                        ownerId,
                        CapitalCycleStatus.ACTIVE
                )
                .map(cycle -> {
                    CapitalBalanceResponse balance = capitalBalanceService.getCycleBalance(ownerId, cycle.getId());
                    return toSummary(cycle, balance);
                })
                .orElseGet(this::emptySummary);
    }

    private CapitalCycle findOwnedCycle(UUID ownerId, UUID cycleId) {
        return capitalCycleRepository.findByIdAndOwnerId(cycleId, ownerId)
                .orElseThrow(() -> new CapitalCycleNotFoundException(cycleId));
    }

    private void ensureTimeCapitalNotInitialized(UUID cycleId) {
        if (timeCapitalRepository.existsByCapitalCycleId(cycleId)) {
            throw new CapitalAlreadyInitializedException(cycleId, CapitalKind.TIME);
        }
    }

    private void ensureMoneyCapitalNotInitialized(UUID cycleId) {
        if (moneyCapitalRepository.existsByCapitalCycleId(cycleId)) {
            throw new CapitalAlreadyInitializedException(cycleId, CapitalKind.MONEY);
        }
    }

    private TimeCapital findTimeCapital(UUID cycleId) {
        return timeCapitalRepository.findByCapitalCycleId(cycleId)
                .orElseThrow(() -> new CapitalNotSetupException(cycleId, CapitalKind.TIME));
    }

    private MoneyCapital findMoneyCapital(UUID cycleId) {
        return moneyCapitalRepository.findByCapitalCycleId(cycleId)
                .orElseThrow(() -> new CapitalNotSetupException(cycleId, CapitalKind.MONEY));
    }

    private void recordSetupHistory(
            CapitalCycle cycle,
            CapitalKind capitalType,
            BigDecimal amount,
            String reason,
            UUID actorId
    ) {
        capitalHistoryRepository.saveAndFlush(CapitalHistory.record(
                cycle,
                capitalType,
                CapitalActionType.CAPITAL_SET,
                amount,
                money(0),
                amount,
                reason,
                null,
                CapitalReferenceType.MANUAL,
                null,
                CapitalActorType.USER,
                actorId
        ));
    }

    private CapitalSummaryResponseDTO toSummary(CapitalCycle cycle, CapitalBalanceResponse balance) {
        return new CapitalSummaryResponseDTO(
                true,
                cycle.getId(),
                cycle.getType(),
                cycle.getStatus(),
                cycle.getStartDate(),
                cycle.getEndDate(),
                toTimeSummary(cycle.getId(), balance.timeCapital()),
                toMoneySummary(balance.moneyCapital())
        );
    }

    private CapitalSummaryResponseDTO emptySummary() {
        return new CapitalSummaryResponseDTO(
                false,
                null,
                null,
                null,
                null,
                null,
                new CapitalSummaryResponseDTO.TimeCapitalSummaryDTO(
                        zeroMoney(),
                        zeroMoney(),
                        zeroMoney(),
                        0L,
                        0L,
                        0L,
                        false,
                        false
                ),
                new CapitalSummaryResponseDTO.MoneyCapitalSummaryDTO(
                        zeroMoney(),
                        zeroMoney(),
                        zeroMoney(),
                        null,
                        false,
                        false
                )
        );
    }

    private CapitalSummaryResponseDTO.TimeCapitalSummaryDTO toTimeSummary(
            UUID cycleId,
            CapitalBalanceSummaryDto timeCapital
    ) {
        BigDecimal allocatedMinutes = normalize(timeCapital.total());
        BigDecimal spentMinutes = normalize(timeCapital.allocated());
        BigDecimal remainingMinutes = normalize(timeCapital.remaining());
        return new CapitalSummaryResponseDTO.TimeCapitalSummaryDTO(
                minutesToHours(allocatedMinutes),
                minutesToHours(spentMinutes),
                minutesToHours(remainingMinutes),
                toWholeMinutes(cycleId, allocatedMinutes),
                toWholeMinutes(cycleId, spentMinutes),
                toWholeMinutes(cycleId, remainingMinutes),
                timeCapital.initialized(),
                timeCapital.overAllocated()
        );
    }

    private CapitalSummaryResponseDTO.MoneyCapitalSummaryDTO toMoneySummary(CapitalBalanceSummaryDto moneyCapital) {
        return new CapitalSummaryResponseDTO.MoneyCapitalSummaryDTO(
                normalize(moneyCapital.total()),
                normalize(moneyCapital.allocated()),
                normalize(moneyCapital.remaining()),
                moneyCapital.currencyCode(),
                moneyCapital.initialized(),
                moneyCapital.overAllocated()
        );
    }

    private BigDecimal minutesToHours(BigDecimal minutes) {
        return normalize(minutes).divide(SIXTY, HOURS_SCALE, RoundingMode.HALF_UP);
    }

    private Long toWholeMinutes(UUID cycleId, BigDecimal amount) {
        try {
            return normalize(amount).longValueExact();
        } catch (ArithmeticException exception) {
            throw new CapitalAllocationDataIntegrityException(cycleId, CapitalKind.TIME);
        }
    }

    private BigDecimal normalize(BigDecimal amount) {
        if (amount == null) {
            return zeroMoney();
        }
        return amount.setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
    }

    private BigDecimal money(long amount) {
        return BigDecimal.valueOf(amount).setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
    }

    private BigDecimal zeroMoney() {
        return BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
    }

    private boolean isConstraintViolation(DataIntegrityViolationException exception, String constraintName) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof ConstraintViolationException constraintViolationException
                    && constraintName.equalsIgnoreCase(constraintViolationException.getConstraintName())) {
                return true;
            }
            String message = current.getMessage();
            if (message != null && message.toLowerCase().contains(constraintName.toLowerCase())) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
