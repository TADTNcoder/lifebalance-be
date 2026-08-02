package com.lifebalance.resourcecapital.service.impl;

import com.lifebalance.resourcecapital.domain.capital.exception.CapitalAlreadyInitializedException;
import com.lifebalance.resourcecapital.domain.capital.exception.CapitalNotSetupException;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.CapitalCycleNotFoundException;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.InvalidCapitalCycleStateException;
import com.lifebalance.resourcecapital.domain.moneycapital.MoneyCapital;
import com.lifebalance.resourcecapital.domain.timecapital.TimeCapital;
import com.lifebalance.resourcecapital.dto.CapitalOverviewResponse;
import com.lifebalance.resourcecapital.dto.MoneyCapitalResponse;
import com.lifebalance.resourcecapital.dto.SetupMoneyCapitalRequest;
import com.lifebalance.resourcecapital.dto.SetupTimeCapitalRequest;
import com.lifebalance.resourcecapital.dto.TimeCapitalResponse;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalCycleRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.MoneyCapitalRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.TimeCapitalRepository;
import com.lifebalance.resourcecapital.service.mapper.CapitalMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapitalServiceImplTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_OWNER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID CYCLE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID TIME_CAPITAL_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID MONEY_CAPITAL_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final Instant NOW = Instant.parse("2026-08-02T10:00:00Z");

    @Mock
    private CapitalCycleRepository capitalCycleRepository;

    @Mock
    private TimeCapitalRepository timeCapitalRepository;

    @Mock
    private MoneyCapitalRepository moneyCapitalRepository;

    @Test
    void setupTimeCapitalCreatesZeroValueCapitalForDraftCycle() throws Exception {
        CapitalCycle cycle = draftCycle();
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));
        when(timeCapitalRepository.existsByCapitalCycleId(CYCLE_ID)).thenReturn(false);
        when(timeCapitalRepository.saveAndFlush(any(TimeCapital.class))).thenAnswer(invocation -> {
            TimeCapital timeCapital = invocation.getArgument(0);
            setField(timeCapital, "id", TIME_CAPITAL_ID);
            return timeCapital;
        });

        TimeCapitalResponse response = createService().setupTimeCapital(
                OWNER_ID,
                CYCLE_ID,
                new SetupTimeCapitalRequest(0L)
        );

        ArgumentCaptor<TimeCapital> captor = ArgumentCaptor.forClass(TimeCapital.class);
        verify(timeCapitalRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getPlannedMinutes()).isZero();
        assertThat(response.id()).isEqualTo(TIME_CAPITAL_ID);
        assertThat(response.initialized()).isTrue();
        assertThat(response.plannedMinutes()).isZero();
        assertThat(response.allocatedMinutes()).isZero();
        assertThat(response.availableMinutes()).isZero();
        assertThat(response.remainingMinutes()).isZero();
    }

    @Test
    void setupTimeCapitalCreatesCapitalForReopenedCycleWhenMissing() throws Exception {
        CapitalCycle cycle = reopenedCycle();
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));
        when(timeCapitalRepository.existsByCapitalCycleId(CYCLE_ID)).thenReturn(false);
        when(timeCapitalRepository.saveAndFlush(any(TimeCapital.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TimeCapitalResponse response = createService().setupTimeCapital(
                OWNER_ID,
                CYCLE_ID,
                new SetupTimeCapitalRequest(480L)
        );

        assertThat(response.initialized()).isTrue();
        assertThat(response.plannedMinutes()).isEqualTo(480L);
        assertThat(response.availableMinutes()).isEqualTo(480L);
        assertThat(response.remainingMinutes()).isEqualTo(480L);
    }

    @Test
    void setupTimeCapitalRejectsClosedCycle() throws Exception {
        CapitalCycle cycle = closedCycle();
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));
        when(timeCapitalRepository.existsByCapitalCycleId(CYCLE_ID)).thenReturn(false);

        assertThatThrownBy(() -> createService().setupTimeCapital(
                OWNER_ID,
                CYCLE_ID,
                new SetupTimeCapitalRequest(480L)
        )).isInstanceOf(InvalidCapitalCycleStateException.class)
                .hasMessageContaining("CLOSED")
                .hasMessageContaining("initialize time capital");
        verify(timeCapitalRepository, never()).saveAndFlush(any());
    }

    @Test
    void setupTimeCapitalRejectsNegativePlannedMinutes() throws Exception {
        CapitalCycle cycle = draftCycle();
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));

        assertThatThrownBy(() -> createService().setupTimeCapital(
                OWNER_ID,
                CYCLE_ID,
                new SetupTimeCapitalRequest(-1L)
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Planned minutes");
    }

    @Test
    void setupTimeCapitalTreatsExistingZeroCapitalAsInitialized() {
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(draftCycle()));
        when(timeCapitalRepository.existsByCapitalCycleId(CYCLE_ID)).thenReturn(true);

        assertThatThrownBy(() -> createService().setupTimeCapital(
                OWNER_ID,
                CYCLE_ID,
                new SetupTimeCapitalRequest(120L)
        )).isInstanceOf(CapitalAlreadyInitializedException.class)
                .hasMessageContaining("TIME");
        verify(timeCapitalRepository, never()).saveAndFlush(any());
    }

    @Test
    void setupTimeCapitalConvertsDatabaseDuplicateToDomainException() {
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(draftCycle()));
        when(timeCapitalRepository.existsByCapitalCycleId(CYCLE_ID)).thenReturn(false);
        when(timeCapitalRepository.saveAndFlush(any(TimeCapital.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> createService().setupTimeCapital(
                OWNER_ID,
                CYCLE_ID,
                new SetupTimeCapitalRequest(120L)
        )).isInstanceOf(CapitalAlreadyInitializedException.class)
                .hasMessageContaining("TIME");
    }

    @Test
    void setupMoneyCapitalCreatesZeroValueCapitalAndNormalizesCurrency() throws Exception {
        CapitalCycle cycle = draftCycle();
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));
        when(moneyCapitalRepository.existsByCapitalCycleId(CYCLE_ID)).thenReturn(false);
        when(moneyCapitalRepository.saveAndFlush(any(MoneyCapital.class))).thenAnswer(invocation -> {
            MoneyCapital moneyCapital = invocation.getArgument(0);
            setField(moneyCapital, "id", MONEY_CAPITAL_ID);
            return moneyCapital;
        });

        MoneyCapitalResponse response = createService().setupMoneyCapital(
                OWNER_ID,
                CYCLE_ID,
                new SetupMoneyCapitalRequest(BigDecimal.ZERO, "vnd")
        );

        assertThat(response.id()).isEqualTo(MONEY_CAPITAL_ID);
        assertThat(response.initialized()).isTrue();
        assertThat(response.plannedAmount()).isEqualByComparingTo("0.0000");
        assertThat(response.allocatedAmount()).isEqualByComparingTo("0.0000");
        assertThat(response.availableAmount()).isEqualByComparingTo("0.0000");
        assertThat(response.remainingAmount()).isEqualByComparingTo("0.0000");
        assertThat(response.currencyCode()).isEqualTo("VND");
    }

    @Test
    void setupMoneyCapitalRejectsClosedCycle() throws Exception {
        CapitalCycle cycle = closedCycle();
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));
        when(moneyCapitalRepository.existsByCapitalCycleId(CYCLE_ID)).thenReturn(false);

        assertThatThrownBy(() -> createService().setupMoneyCapital(
                OWNER_ID,
                CYCLE_ID,
                new SetupMoneyCapitalRequest(new BigDecimal("100.0000"), "VND")
        )).isInstanceOf(InvalidCapitalCycleStateException.class)
                .hasMessageContaining("CLOSED")
                .hasMessageContaining("initialize money capital");
        verify(moneyCapitalRepository, never()).saveAndFlush(any());
    }

    @Test
    void setupMoneyCapitalRejectsNegativePlannedAmount() {
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(draftCycle()));

        assertThatThrownBy(() -> createService().setupMoneyCapital(
                OWNER_ID,
                CYCLE_ID,
                new SetupMoneyCapitalRequest(new BigDecimal("-0.0001"), "VND")
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("greater than or equal to zero");
    }

    @Test
    void setupMoneyCapitalTreatsExistingZeroCapitalAsInitialized() {
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(draftCycle()));
        when(moneyCapitalRepository.existsByCapitalCycleId(CYCLE_ID)).thenReturn(true);

        assertThatThrownBy(() -> createService().setupMoneyCapital(
                OWNER_ID,
                CYCLE_ID,
                new SetupMoneyCapitalRequest(new BigDecimal("100.0000"), "VND")
        )).isInstanceOf(CapitalAlreadyInitializedException.class)
                .hasMessageContaining("MONEY");
        verify(moneyCapitalRepository, never()).saveAndFlush(any());
    }

    @Test
    void setupMoneyCapitalConvertsDatabaseDuplicateToDomainException() {
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(draftCycle()));
        when(moneyCapitalRepository.existsByCapitalCycleId(CYCLE_ID)).thenReturn(false);
        when(moneyCapitalRepository.saveAndFlush(any(MoneyCapital.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> createService().setupMoneyCapital(
                OWNER_ID,
                CYCLE_ID,
                new SetupMoneyCapitalRequest(new BigDecimal("100.0000"), "VND")
        )).isInstanceOf(CapitalAlreadyInitializedException.class)
                .hasMessageContaining("MONEY");
    }

    @Test
    void getAvailableAndRemainingTimeCapitalReturnPlannedMinusAllocatedOnly() throws Exception {
        TimeCapital timeCapital = timeCapital(360);
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(draftCycle()));
        when(timeCapitalRepository.findByCapitalCycleId(CYCLE_ID)).thenReturn(Optional.of(timeCapital));

        TimeCapitalResponse available = createService().getAvailableTimeCapital(OWNER_ID, CYCLE_ID);
        TimeCapitalResponse remaining = createService().getRemainingTimeCapital(OWNER_ID, CYCLE_ID);

        assertThat(available.allocatedMinutes()).isZero();
        assertThat(available.availableMinutes()).isEqualTo(360L);
        assertThat(remaining.remainingMinutes()).isEqualTo(360L);
    }

    @Test
    void getAvailableAndRemainingMoneyCapitalReturnPlannedMinusAllocatedOnly() throws Exception {
        MoneyCapital moneyCapital = moneyCapital(new BigDecimal("1234.5000"), "VND");
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(draftCycle()));
        when(moneyCapitalRepository.findByCapitalCycleId(CYCLE_ID)).thenReturn(Optional.of(moneyCapital));

        MoneyCapitalResponse available = createService().getAvailableMoneyCapital(OWNER_ID, CYCLE_ID);
        MoneyCapitalResponse remaining = createService().getRemainingMoneyCapital(OWNER_ID, CYCLE_ID);

        assertThat(available.allocatedAmount()).isEqualByComparingTo("0.0000");
        assertThat(available.availableAmount()).isEqualByComparingTo("1234.5000");
        assertThat(remaining.remainingAmount()).isEqualByComparingTo("1234.5000");
    }

    @Test
    void getAvailableCapitalRejectsMissingSetup() {
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(draftCycle()));
        when(timeCapitalRepository.findByCapitalCycleId(CYCLE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createService().getAvailableTimeCapital(OWNER_ID, CYCLE_ID))
                .isInstanceOf(CapitalNotSetupException.class)
                .hasMessageContaining("TIME");
    }

    @Test
    void getCapitalOverviewDistinguishesMissingCapitalFromInitializedZeroCapital() throws Exception {
        TimeCapital timeCapital = timeCapital(0);
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(draftCycle()));
        when(timeCapitalRepository.findByCapitalCycleId(CYCLE_ID)).thenReturn(Optional.of(timeCapital));
        when(moneyCapitalRepository.findByCapitalCycleId(CYCLE_ID)).thenReturn(Optional.empty());

        CapitalOverviewResponse overview = createService().getCapitalOverview(OWNER_ID, CYCLE_ID);

        assertThat(overview.cycleId()).isEqualTo(CYCLE_ID);
        assertThat(overview.cycleStatus()).isEqualTo(CapitalCycleStatus.DRAFT);
        assertThat(overview.timeCapital().initialized()).isTrue();
        assertThat(overview.timeCapital().plannedMinutes()).isZero();
        assertThat(overview.moneyCapital().initialized()).isFalse();
        assertThat(overview.moneyCapital().plannedAmount()).isNull();
        assertThat(overview.moneyCapital().availableAmount()).isNull();
    }

    @Test
    void ownerCannotSetupOrReadAnotherOwnersCapital() {
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OTHER_OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createService().setupTimeCapital(
                OTHER_OWNER_ID,
                CYCLE_ID,
                new SetupTimeCapitalRequest(60L)
        )).isInstanceOf(CapitalCycleNotFoundException.class);
        assertThatThrownBy(() -> createService().getCapitalOverview(OTHER_OWNER_ID, CYCLE_ID))
                .isInstanceOf(CapitalCycleNotFoundException.class);
        verify(timeCapitalRepository, never()).saveAndFlush(any());
    }

    private CapitalServiceImpl createService() {
        return new CapitalServiceImpl(
                capitalCycleRepository,
                timeCapitalRepository,
                moneyCapitalRepository,
                new CapitalMapper()
        );
    }

    private static CapitalCycle draftCycle() {
        CapitalCycle cycle = CapitalCycle.create(
                OWNER_ID,
                "August 1",
                "Daily resource cycle",
                CapitalCycleType.DAILY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 1)
        );
        setField(cycle, "id", CYCLE_ID);
        return cycle;
    }

    private static CapitalCycle closedCycle() {
        CapitalCycle cycle = draftCycle();
        cycle.activate(NOW.minusSeconds(120));
        cycle.close("Finished", NOW.minusSeconds(90));
        return cycle;
    }

    private static CapitalCycle reopenedCycle() {
        CapitalCycle cycle = closedCycle();
        cycle.reopen("Need correction", NOW.minusSeconds(60));
        return cycle;
    }

    private static TimeCapital timeCapital(long plannedMinutes) throws Exception {
        TimeCapital timeCapital = TimeCapital.create(draftCycle(), plannedMinutes);
        setField(timeCapital, "id", TIME_CAPITAL_ID);
        return timeCapital;
    }

    private static MoneyCapital moneyCapital(BigDecimal plannedAmount, String currencyCode) throws Exception {
        MoneyCapital moneyCapital = MoneyCapital.create(draftCycle(), plannedAmount, currencyCode);
        setField(moneyCapital, "id", MONEY_CAPITAL_ID);
        return moneyCapital;
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to set test field " + fieldName, exception);
        }
    }
}
