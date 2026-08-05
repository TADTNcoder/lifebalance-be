package com.lifebalance.resourcecapital.service;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.CapitalCycleNotFoundException;
import com.lifebalance.resourcecapital.dto.AllocateCapitalRequest;
import com.lifebalance.resourcecapital.dto.CapitalBalanceResponse;
import com.lifebalance.resourcecapital.dto.ResourceBreakdownDto;
import com.lifebalance.resourcecapital.dto.SetupMoneyCapitalRequest;
import com.lifebalance.resourcecapital.dto.SetupTimeCapitalRequest;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalCycleRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "eureka.client.enabled=false"
})
class CapitalBalanceServiceIntegrationTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_OWNER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID TASK_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Autowired
    private CapitalBalanceService capitalBalanceService;

    @Autowired
    private CapitalService capitalService;

    @Autowired
    private AllocationService allocationService;

    @Autowired
    private CapitalCycleRepository capitalCycleRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void getCycleBalanceCalculatesCurrentAllocationState() {
        CapitalCycle cycle = capitalCycleRepository.saveAndFlush(dailyCycle("August 1", LocalDate.of(2026, 8, 1)));
        capitalService.setupTimeCapital(OWNER_ID, cycle.getId(), new SetupTimeCapitalRequest(480L));
        capitalService.setupMoneyCapital(
                OWNER_ID,
                cycle.getId(),
                new SetupMoneyCapitalRequest(new BigDecimal("1000.0000"), "VND")
        );
        allocationService.allocateCapital(
                OWNER_ID,
                cycle.getId(),
                new AllocateCapitalRequest(
                        CapitalKind.TIME,
                        AllocationTargetType.TASK,
                        TASK_ID,
                        new BigDecimal("120.0000"),
                        false,
                        "Plan focus work"
                )
        );
        entityManager.flush();
        entityManager.clear();

        CapitalBalanceResponse balance = capitalBalanceService.getCycleBalance(OWNER_ID, cycle.getId());
        List<ResourceBreakdownDto> breakdown = capitalBalanceService.getAllocationBreakdownByTarget(
                OWNER_ID,
                cycle.getId(),
                AllocationTargetType.TASK
        );

        assertThat(balance.timeCapital().total()).isEqualByComparingTo("480.0000");
        assertThat(balance.timeCapital().allocated()).isEqualByComparingTo("120.0000");
        assertThat(balance.timeCapital().available()).isEqualByComparingTo("360.0000");
        assertThat(balance.timeCapital().remaining()).isEqualByComparingTo("360.0000");
        assertThat(balance.timeCapital().allocatedPercentage()).isEqualByComparingTo("25.00");
        assertThat(balance.timeCapital().overAllocated()).isFalse();
        assertThat(balance.moneyCapital().total()).isEqualByComparingTo("1000.0000");
        assertThat(balance.moneyCapital().allocated()).isEqualByComparingTo("0.0000");
        assertThat(balance.moneyCapital().currencyCode()).isEqualTo("VND");
        assertThat(breakdown).hasSize(1);
        assertThat(breakdown.get(0).targetId()).isEqualTo(TASK_ID);
        assertThat(breakdown.get(0).percentageOfTotal()).isEqualByComparingTo("25.00");
        assertThat(breakdown.get(0).percentageOfAllocated()).isEqualByComparingTo("100.00");
    }

    @Test
    void ownerCannotReadAnotherOwnersBalance() {
        CapitalCycle cycle = capitalCycleRepository.saveAndFlush(dailyCycle("August 2", LocalDate.of(2026, 8, 2)));

        assertThatThrownBy(() -> capitalBalanceService.getCycleBalance(OTHER_OWNER_ID, cycle.getId()))
                .isInstanceOf(CapitalCycleNotFoundException.class);
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
