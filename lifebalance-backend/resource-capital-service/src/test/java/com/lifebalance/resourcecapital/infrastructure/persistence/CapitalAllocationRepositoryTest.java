package com.lifebalance.resourcecapital.infrastructure.persistence;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationStatus;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;
import com.lifebalance.resourcecapital.domain.capitalallocation.CapitalAllocation;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
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

@Transactional
@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "eureka.client.enabled=false"
})
class CapitalAllocationRepositoryTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_OWNER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID TASK_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID OTHER_TASK_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    @Autowired
    private CapitalCycleRepository capitalCycleRepository;

    @Autowired
    private CapitalAllocationRepository capitalAllocationRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findsAllocationsByOwnerAndCycleWithoutLeakingOtherUsers() {
        CapitalCycle cycle = capitalCycleRepository.save(dailyCycle(OWNER_ID, "Owner cycle", LocalDate.of(2026, 8, 1)));
        CapitalCycle otherCycle = capitalCycleRepository.save(dailyCycle(OTHER_OWNER_ID, "Other cycle", LocalDate.of(2026, 8, 2)));
        capitalCycleRepository.flush();
        CapitalAllocation timeAllocation = capitalAllocationRepository.save(allocation(
                cycle,
                CapitalKind.TIME,
                TASK_ID,
                "60.0000"
        ));
        CapitalAllocation moneyAllocation = capitalAllocationRepository.save(allocation(
                cycle,
                CapitalKind.MONEY,
                OTHER_TASK_ID,
                "100.0000"
        ));
        capitalAllocationRepository.save(allocation(
                otherCycle,
                CapitalKind.TIME,
                TASK_ID,
                "90.0000"
        ));
        capitalAllocationRepository.flush();
        entityManager.clear();

        var ownerAllocations = capitalAllocationRepository.findByUserIdAndCapitalCycleId(OWNER_ID, cycle.getId());
        var leakedAllocations = capitalAllocationRepository.findByUserIdAndCapitalCycleId(OWNER_ID, otherCycle.getId());

        assertThat(ownerAllocations)
                .extracting(CapitalAllocation::getId)
                .containsExactlyInAnyOrder(timeAllocation.getId(), moneyAllocation.getId());
        assertThat(leakedAllocations).isEmpty();
    }

    @Test
    void filtersByOwnerCycleTargetAndCapitalType() {
        CapitalCycle cycle = capitalCycleRepository.save(dailyCycle(OWNER_ID, "Owner cycle", LocalDate.of(2026, 8, 3)));
        CapitalCycle otherCycle = capitalCycleRepository.save(dailyCycle(OTHER_OWNER_ID, "Other cycle", LocalDate.of(2026, 8, 4)));
        capitalCycleRepository.flush();
        CapitalAllocation visible = capitalAllocationRepository.save(allocation(
                cycle,
                CapitalKind.TIME,
                TASK_ID,
                "45.0000"
        ));
        capitalAllocationRepository.save(allocation(
                cycle,
                CapitalKind.MONEY,
                TASK_ID,
                "200.0000"
        ));
        capitalAllocationRepository.save(allocation(
                otherCycle,
                CapitalKind.TIME,
                TASK_ID,
                "30.0000"
        ));
        capitalAllocationRepository.flush();
        entityManager.clear();

        var exactTarget = capitalAllocationRepository.findByUserIdAndCapitalCycleIdAndTargetTypeAndTargetIdAndCapitalType(
                OWNER_ID,
                cycle.getId(),
                AllocationTargetType.TASK,
                TASK_ID,
                CapitalKind.TIME
        );
        var timePage = capitalAllocationRepository.findByUserIdAndCapitalCycleIdAndCapitalType(
                OWNER_ID,
                cycle.getId(),
                CapitalKind.TIME,
                PageRequest.of(0, 10)
        );

        assertThat(exactTarget)
                .isPresent()
                .get()
                .extracting(CapitalAllocation::getId)
                .isEqualTo(visible.getId());
        assertThat(timePage.getContent())
                .extracting(CapitalAllocation::getId)
                .containsExactly(visible.getId());
    }

    @Test
    void findsAvailableAllocationsAndSumsAmountsWithinOwnerScope() {
        CapitalCycle cycle = capitalCycleRepository.save(dailyCycle(OWNER_ID, "Owner cycle", LocalDate.of(2026, 8, 5)));
        CapitalCycle otherCycle = capitalCycleRepository.save(dailyCycle(OTHER_OWNER_ID, "Other cycle", LocalDate.of(2026, 8, 6)));
        capitalCycleRepository.flush();
        CapitalAllocation available = allocation(cycle, CapitalKind.TIME, TASK_ID, "100.0000");
        available.spend(new BigDecimal("20.0000"));
        available.release(new BigDecimal("10.0000"));
        CapitalAllocation unavailable = allocation(cycle, CapitalKind.TIME, OTHER_TASK_ID, "40.0000");
        unavailable.spend(new BigDecimal("20.0000"));
        unavailable.release(new BigDecimal("20.0000"));
        CapitalAllocation otherOwnerAvailable = allocation(otherCycle, CapitalKind.TIME, TASK_ID, "100.0000");
        capitalAllocationRepository.save(available);
        capitalAllocationRepository.save(unavailable);
        capitalAllocationRepository.save(otherOwnerAvailable);
        capitalAllocationRepository.flush();
        entityManager.clear();

        var availableAllocations = capitalAllocationRepository.findAvailableForReallocateOrRelease(
                OWNER_ID,
                cycle.getId(),
                AllocationStatus.ACTIVE
        );
        BigDecimal allocated = capitalAllocationRepository.sumAllocatedAmount(
                OWNER_ID,
                cycle.getId(),
                CapitalKind.TIME,
                AllocationStatus.ACTIVE
        );
        BigDecimal spent = capitalAllocationRepository.sumSpentAmount(
                OWNER_ID,
                cycle.getId(),
                CapitalKind.TIME,
                AllocationStatus.ACTIVE
        );

        assertThat(availableAllocations)
                .extracting(CapitalAllocation::getId)
                .containsExactly(available.getId());
        assertThat(allocated).isEqualByComparingTo("110.0000");
        assertThat(spent).isEqualByComparingTo("40.0000");
    }

    private CapitalAllocation allocation(
            CapitalCycle cycle,
            CapitalKind capitalType,
            UUID targetId,
            String amount
    ) {
        return CapitalAllocation.create(
                cycle,
                capitalType,
                AllocationTargetType.TASK,
                targetId,
                new BigDecimal(amount)
        );
    }

    private CapitalCycle dailyCycle(UUID ownerId, String name, LocalDate date) {
        return CapitalCycle.create(
                ownerId,
                name,
                "Daily resource cycle",
                CapitalCycleType.DAILY,
                date,
                date
        );
    }
}
