package com.lifebalance.resourcecapital.service;

import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActionType;
import com.lifebalance.resourcecapital.domain.capital.exception.CapitalAlreadyInitializedException;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import com.lifebalance.resourcecapital.dto.CapitalOverviewResponse;
import com.lifebalance.resourcecapital.dto.MoneyCapitalResponse;
import com.lifebalance.resourcecapital.dto.SetupMoneyCapitalRequest;
import com.lifebalance.resourcecapital.dto.SetupTimeCapitalRequest;
import com.lifebalance.resourcecapital.dto.TimeCapitalResponse;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalCycleRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalHistoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "eureka.client.enabled=false"
})
class CapitalServiceIntegrationTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private CapitalService capitalService;

    @Autowired
    private CapitalCycleRepository capitalCycleRepository;

    @Autowired
    private CapitalHistoryRepository capitalHistoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void setupTimeAndMoneyCapitalPersistsAndOverviewReturnsFoundationCalculations() {
        CapitalCycle cycle = capitalCycleRepository.saveAndFlush(dailyCycle("August 1", LocalDate.of(2026, 8, 1)));

        TimeCapitalResponse timeCapital = capitalService.setupTimeCapital(
                OWNER_ID,
                cycle.getId(),
                new SetupTimeCapitalRequest(480L)
        );
        MoneyCapitalResponse moneyCapital = capitalService.setupMoneyCapital(
                OWNER_ID,
                cycle.getId(),
                new SetupMoneyCapitalRequest(new BigDecimal("1234.5000"), "usd")
        );
        entityManager.flush();
        entityManager.clear();

        CapitalOverviewResponse overview = capitalService.getCapitalOverview(OWNER_ID, cycle.getId());
        var historyPage = capitalHistoryRepository.findByCapitalCycleId(cycle.getId(), PageRequest.of(0, 10));

        assertThat(timeCapital.initialized()).isTrue();
        assertThat(timeCapital.availableMinutes()).isEqualTo(480L);
        assertThat(timeCapital.remainingMinutes()).isEqualTo(480L);
        assertThat(moneyCapital.initialized()).isTrue();
        assertThat(moneyCapital.currencyCode()).isEqualTo("USD");
        assertThat(moneyCapital.availableAmount()).isEqualByComparingTo("1234.5000");
        assertThat(moneyCapital.remainingAmount()).isEqualByComparingTo("1234.5000");
        assertThat(overview.cycleStatus()).isEqualTo(CapitalCycleStatus.DRAFT);
        assertThat(overview.timeCapital().plannedMinutes()).isEqualTo(480L);
        assertThat(overview.moneyCapital().plannedAmount()).isEqualByComparingTo("1234.5000");
        assertThat(historyPage.getContent())
                .extracting(history -> history.getActionType())
                .containsExactly(CapitalActionType.CAPITAL_SET, CapitalActionType.CAPITAL_SET);
    }

    @Test
    void setupAllowsZeroCapitalAndOverviewDistinguishesItFromMissingCapital() {
        CapitalCycle cycle = capitalCycleRepository.saveAndFlush(dailyCycle("August 2", LocalDate.of(2026, 8, 2)));

        capitalService.setupTimeCapital(OWNER_ID, cycle.getId(), new SetupTimeCapitalRequest(0L));
        entityManager.flush();
        entityManager.clear();

        CapitalOverviewResponse overview = capitalService.getCapitalOverview(OWNER_ID, cycle.getId());

        assertThat(overview.timeCapital().initialized()).isTrue();
        assertThat(overview.timeCapital().plannedMinutes()).isZero();
        assertThat(overview.timeCapital().availableMinutes()).isZero();
        assertThat(overview.timeCapital().remainingMinutes()).isZero();
        assertThat(overview.moneyCapital().initialized()).isFalse();
        assertThat(overview.moneyCapital().plannedAmount()).isNull();
        assertThat(overview.moneyCapital().availableAmount()).isNull();
        assertThat(overview.moneyCapital().remainingAmount()).isNull();
    }

    @Test
    void setupRejectsDuplicateCapitalEvenWhenExistingValueIsZero() {
        CapitalCycle cycle = capitalCycleRepository.saveAndFlush(dailyCycle("August 3", LocalDate.of(2026, 8, 3)));
        capitalService.setupTimeCapital(OWNER_ID, cycle.getId(), new SetupTimeCapitalRequest(0L));

        assertThatThrownBy(() -> capitalService.setupTimeCapital(
                OWNER_ID,
                cycle.getId(),
                new SetupTimeCapitalRequest(60L)
        )).isInstanceOf(CapitalAlreadyInitializedException.class);
    }

    private CapitalCycle dailyCycle(String name, LocalDate date) {
        return CapitalCycle.create(
                OWNER_ID,
                name,
                "Daily resource cycle",
                CapitalCycleType.DAILY,
                date,
                date
        );
    }
}
