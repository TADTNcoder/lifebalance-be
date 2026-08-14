package com.lifebalance.resourcecapital.service;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationStatus;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.OverAllocationConfirmationRequiredException;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActionType;
import com.lifebalance.resourcecapital.dto.AllocateCapitalRequest;
import com.lifebalance.resourcecapital.dto.AllocationResponse;
import com.lifebalance.resourcecapital.dto.ReallocateCapitalRequest;
import com.lifebalance.resourcecapital.dto.ReleaseCapitalRequest;
import com.lifebalance.resourcecapital.dto.SetupTimeCapitalRequest;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalAllocationRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalCycleRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalHistoryRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.TimeCapitalRepository;
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
class AllocationServiceIntegrationTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SOURCE_TASK_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID DESTINATION_TASK_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Autowired
    private AllocationService allocationService;

    @Autowired
    private CapitalService capitalService;

    @Autowired
    private CapitalAllocationReader capitalAllocationReader;

    @Autowired
    private CapitalCycleRepository capitalCycleRepository;

    @Autowired
    private TimeCapitalRepository timeCapitalRepository;

    @Autowired
    private CapitalAllocationRepository capitalAllocationRepository;

    @Autowired
    private CapitalHistoryRepository capitalHistoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void allocateCapitalPersistsAllocationStateAndHistory() {
        CapitalCycle cycle = createCycle("August 4", LocalDate.of(2026, 8, 4), false);
        capitalService.setupTimeCapital(OWNER_ID, cycle.getId(), new SetupTimeCapitalRequest(120L));

        AllocationResponse response = allocationService.allocateCapital(
                OWNER_ID,
                cycle.getId(),
                new AllocateCapitalRequest(
                        CapitalKind.TIME,
                        AllocationTargetType.TASK,
                        SOURCE_TASK_ID,
                        new BigDecimal("45.0000"),
                        false,
                        "Initial task budget"
                )
        );
        entityManager.flush();
        entityManager.clear();

        assertThat(response.targetAllocatedAmount()).isEqualByComparingTo("45.0000");
        assertThat(response.totalAllocatedAmount()).isEqualByComparingTo("45.0000");
        assertThat(response.remainingAmount()).isEqualByComparingTo("75.0000");
        assertThat(capitalAllocationReader.getAllocatedMinutes(cycle.getId())).isEqualTo(45L);
        assertThat(capitalAllocationRepository.findByCapitalCycleIdAndCapitalTypeAndTargetTypeAndTargetId(
                cycle.getId(),
                CapitalKind.TIME,
                AllocationTargetType.TASK,
                SOURCE_TASK_ID
        )).isPresent()
                .get()
                .satisfies(allocation -> assertThat(allocation.getAllocatedAmount()).isEqualByComparingTo("45.0000"));
        assertThat(capitalHistoryRepository.findByCapitalCycleIdAndActionType(
                cycle.getId(),
                CapitalActionType.ALLOCATE,
                PageRequest.of(0, 10)
        ).getContent()).singleElement().satisfies(history -> {
            assertThat(history.getId()).isEqualTo(response.historyIds().getFirst());
            assertThat(history.getReferenceId()).isEqualTo(SOURCE_TASK_ID);
            assertThat(history.getBeforeAmount()).isEqualByComparingTo("0.0000");
            assertThat(history.getAfterAmount()).isEqualByComparingTo("45.0000");
        });
    }

    @Test
    void approvedOverAllocationPersistsAllocateAndApprovalHistories() {
        CapitalCycle cycle = createCycle("August 5", LocalDate.of(2026, 8, 5), true);
        capitalService.setupTimeCapital(OWNER_ID, cycle.getId(), new SetupTimeCapitalRequest(60L));
        allocationService.allocateCapital(
                OWNER_ID,
                cycle.getId(),
                new AllocateCapitalRequest(
                        CapitalKind.TIME,
                        AllocationTargetType.TASK,
                        SOURCE_TASK_ID,
                        new BigDecimal("50.0000"),
                        false,
                        "Initial"
                )
        );

        assertThatThrownBy(() -> allocationService.allocateCapital(
                OWNER_ID,
                cycle.getId(),
                new AllocateCapitalRequest(
                        CapitalKind.TIME,
                        AllocationTargetType.TASK,
                        DESTINATION_TASK_ID,
                        new BigDecimal("20.0000"),
                        false,
                        "Missing confirmation"
                )
        )).isInstanceOf(OverAllocationConfirmationRequiredException.class);

        AllocationResponse response = allocationService.allocateCapital(
                OWNER_ID,
                cycle.getId(),
                new AllocateCapitalRequest(
                        CapitalKind.TIME,
                        AllocationTargetType.TASK,
                        DESTINATION_TASK_ID,
                        new BigDecimal("20.0000"),
                        true,
                        "Explicitly approved"
                )
        );
        entityManager.flush();
        entityManager.clear();

        assertThat(response.overAllocated()).isTrue();
        assertThat(response.remainingAmount()).isEqualByComparingTo("-10.0000");
        assertThat(response.historyIds()).hasSize(2);
        assertThat(capitalHistoryRepository.findByCapitalCycleIdAndActionType(
                cycle.getId(),
                CapitalActionType.OVER_ALLOCATION_APPROVED,
                PageRequest.of(0, 10)
        ).getContent()).singleElement().satisfies(history -> {
            assertThat(history.getReferenceId()).isEqualTo(DESTINATION_TASK_ID);
            assertThat(history.getBeforeAmount()).isEqualByComparingTo("10.0000");
            assertThat(history.getAfterAmount()).isEqualByComparingTo("-10.0000");
        });
    }

    @Test
    void reallocateAndReleaseUpdateStateAndHistory() {
        CapitalCycle cycle = createCycle("August 6", LocalDate.of(2026, 8, 6), false);
        capitalService.setupTimeCapital(OWNER_ID, cycle.getId(), new SetupTimeCapitalRequest(120L));
        allocationService.allocateCapital(
                OWNER_ID,
                cycle.getId(),
                new AllocateCapitalRequest(
                        CapitalKind.TIME,
                        AllocationTargetType.TASK,
                        SOURCE_TASK_ID,
                        new BigDecimal("80.0000"),
                        false,
                        "Source"
                )
        );

        AllocationResponse reallocateResponse = allocationService.reallocateCapital(
                OWNER_ID,
                cycle.getId(),
                new ReallocateCapitalRequest(
                        CapitalKind.TIME,
                        AllocationTargetType.TASK,
                        SOURCE_TASK_ID,
                        AllocationTargetType.TASK,
                        DESTINATION_TASK_ID,
                        new BigDecimal("30.0000"),
                        "Move"
                )
        );
        AllocationResponse releaseResponse = allocationService.releaseCapital(
                OWNER_ID,
                cycle.getId(),
                new ReleaseCapitalRequest(
                        CapitalKind.TIME,
                        AllocationTargetType.TASK,
                        DESTINATION_TASK_ID,
                        new BigDecimal("30.0000"),
                        "Release destination"
                )
        );
        entityManager.flush();
        entityManager.clear();

        assertThat(reallocateResponse.totalAllocatedAmount()).isEqualByComparingTo("80.0000");
        assertThat(reallocateResponse.targetAllocatedAmount()).isEqualByComparingTo("30.0000");
        assertThat(releaseResponse.targetAllocatedAmount()).isEqualByComparingTo("0.0000");
        assertThat(releaseResponse.totalAllocatedAmount()).isEqualByComparingTo("50.0000");
        assertThat(capitalAllocationRepository.findByCapitalCycleIdAndCapitalTypeAndTargetTypeAndTargetId(
                cycle.getId(),
                CapitalKind.TIME,
                AllocationTargetType.TASK,
                DESTINATION_TASK_ID
        )).isPresent()
                .get()
                .satisfies(allocation -> {
                    assertThat(allocation.getAllocatedAmount()).isEqualByComparingTo("0.0000");
                    assertThat(allocation.getStatus()).isEqualTo(AllocationStatus.RELEASED);
                });
        assertThat(capitalAllocationReader.getAllocatedMinutes(cycle.getId())).isEqualTo(50L);
        assertThat(capitalHistoryRepository.findByCapitalCycleIdAndActionType(
                cycle.getId(),
                CapitalActionType.REALLOCATE,
                PageRequest.of(0, 10)
        ).getContent()).hasSize(2);
        assertThat(capitalHistoryRepository.findByCapitalCycleIdAndActionType(
                cycle.getId(),
                CapitalActionType.RELEASE,
                PageRequest.of(0, 10)
        ).getContent()).singleElement()
                .satisfies(history -> assertThat(history.getReferenceId()).isEqualTo(DESTINATION_TASK_ID));
    }

    @Test
    void capitalOverviewUsesPersistedAllocatedAmount() {
        CapitalCycle cycle = createCycle("August 7", LocalDate.of(2026, 8, 7), false);
        capitalService.setupTimeCapital(OWNER_ID, cycle.getId(), new SetupTimeCapitalRequest(90L));
        allocationService.allocateCapital(
                OWNER_ID,
                cycle.getId(),
                new AllocateCapitalRequest(
                        CapitalKind.TIME,
                        AllocationTargetType.TASK,
                        SOURCE_TASK_ID,
                        new BigDecimal("35.0000"),
                        false,
                        "Overview"
                )
        );
        entityManager.flush();
        entityManager.clear();

        assertThat(capitalService.getCapitalOverview(OWNER_ID, cycle.getId()).timeCapital())
                .satisfies(timeCapital -> {
                    assertThat(timeCapital.plannedMinutes()).isEqualTo(90L);
                    assertThat(timeCapital.allocatedMinutes()).isEqualTo(35L);
                    assertThat(timeCapital.remainingMinutes()).isEqualTo(55L);
                });
    }

    private CapitalCycle createCycle(String name, LocalDate date, boolean overAllocationAllowed) {
        CapitalCycle cycle = CapitalCycle.create(
                OWNER_ID,
                name,
                "Daily resource cycle",
                CapitalCycleType.DAILY,
                date,
                date
        );
        if (overAllocationAllowed) {
            cycle.allowOverAllocation();
        }
        return capitalCycleRepository.saveAndFlush(cycle);
    }
}
