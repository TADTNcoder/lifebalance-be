package com.lifebalance.resourcecapital.service;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationStatus;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;
import com.lifebalance.resourcecapital.domain.capitalallocation.CapitalAllocation;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.InsufficientAllocatedCapitalException;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.InvalidAllocationStateException;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.OverAllocationConfirmationRequiredException;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActionType;
import com.lifebalance.resourcecapital.dto.AllocateCapitalRequest;
import com.lifebalance.resourcecapital.dto.AllocationResponse;
import com.lifebalance.resourcecapital.dto.CapitalAllocationChangeRequest;
import com.lifebalance.resourcecapital.dto.CapitalAllocationReleaseRequest;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.Instant;
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
    private CapitalAllocationService capitalAllocationService;

    @MockitoBean
    private AllocationTargetValidator allocationTargetValidator;

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
        activateCycle(cycle);

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
        assertThat(capitalAllocationRepository.findByUserIdAndCapitalCycleIdAndTargetTypeAndTargetIdAndCapitalType(
                OWNER_ID,
                cycle.getId(),
                AllocationTargetType.TASK,
                SOURCE_TASK_ID,
                CapitalKind.TIME
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
        activateCycle(cycle);
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
        activateCycle(cycle);
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
        assertThat(capitalAllocationRepository.findByUserIdAndCapitalCycleIdAndTargetTypeAndTargetIdAndCapitalType(
                OWNER_ID,
                cycle.getId(),
                AllocationTargetType.TASK,
                DESTINATION_TASK_ID,
                CapitalKind.TIME
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
    void changeAllocationIncreaseRequiresConfirmationAndAppendsHistory() {
        CapitalCycle cycle = createCycle("August 8", LocalDate.of(2026, 8, 8), true);
        capitalService.setupTimeCapital(OWNER_ID, cycle.getId(), new SetupTimeCapitalRequest(100L));
        activateCycle(cycle);
        allocationService.allocateCapital(
                OWNER_ID,
                cycle.getId(),
                new AllocateCapitalRequest(
                        CapitalKind.TIME,
                        AllocationTargetType.TASK,
                        SOURCE_TASK_ID,
                        new BigDecimal("90.0000"),
                        false,
                        "Initial"
                )
        );

        CapitalAllocation allocation = findTaskAllocation(cycle, SOURCE_TASK_ID);
        assertThatThrownBy(() -> capitalAllocationService.changeAllocation(
                OWNER_ID,
                allocation.getId(),
                new CapitalAllocationChangeRequest(
                        new BigDecimal("120.0000"),
                        false,
                        "Needs confirmation"
                )
        )).isInstanceOf(OverAllocationConfirmationRequiredException.class);
        entityManager.flush();
        entityManager.clear();

        CapitalAllocation afterRejectedChange = findTaskAllocation(cycle, SOURCE_TASK_ID);
        assertThat(afterRejectedChange.getAllocatedAmount()).isEqualByComparingTo("90.0000");

        AllocationResponse response = capitalAllocationService.changeAllocation(
                OWNER_ID,
                afterRejectedChange.getId(),
                new CapitalAllocationChangeRequest(
                        new BigDecimal("120.0000"),
                        true,
                        "Approved change"
                )
        );
        entityManager.flush();
        entityManager.clear();

        assertThat(response.targetAllocatedAmount()).isEqualByComparingTo("120.0000");
        assertThat(response.totalAllocatedAmount()).isEqualByComparingTo("120.0000");
        assertThat(response.remainingAmount()).isEqualByComparingTo("-20.0000");
        assertThat(response.overAllocated()).isTrue();
        assertThat(response.historyIds()).hasSize(2);
        assertThat(findTaskAllocation(cycle, SOURCE_TASK_ID)).satisfies(changedAllocation -> {
            assertThat(changedAllocation.getAllocatedAmount()).isEqualByComparingTo("120.0000");
            assertThat(changedAllocation.getIsOverAllocated()).isTrue();
            assertThat(changedAllocation.getOverAllocationConfirmed()).isTrue();
        });
        assertThat(capitalHistoryRepository.findByCapitalCycleIdAndActionType(
                cycle.getId(),
                CapitalActionType.ALLOCATE,
                PageRequest.of(0, 10)
        ).getContent()).hasSize(1);
        assertThat(capitalHistoryRepository.findByCapitalCycleIdAndActionType(
                cycle.getId(),
                CapitalActionType.REALLOCATE,
                PageRequest.of(0, 10)
        ).getContent()).singleElement().satisfies(history -> {
            assertThat(history.getReferenceId()).isEqualTo(SOURCE_TASK_ID);
            assertThat(history.getBeforeAmount()).isEqualByComparingTo("90.0000");
            assertThat(history.getAfterAmount()).isEqualByComparingTo("120.0000");
        });
        assertThat(capitalHistoryRepository.findByCapitalCycleIdAndActionType(
                cycle.getId(),
                CapitalActionType.OVER_ALLOCATION_APPROVED,
                PageRequest.of(0, 10)
        ).getContent()).singleElement()
                .satisfies(history -> assertThat(history.getReferenceId()).isEqualTo(SOURCE_TASK_ID));
    }

    @Test
    void changeAllocationDecreaseRecordsReallocationAndKeepsReleaseBalanceUntouched() {
        CapitalCycle cycle = createCycle("August 9", LocalDate.of(2026, 8, 9), false);
        capitalService.setupTimeCapital(OWNER_ID, cycle.getId(), new SetupTimeCapitalRequest(100L));
        activateCycle(cycle);
        allocationService.allocateCapital(
                OWNER_ID,
                cycle.getId(),
                new AllocateCapitalRequest(
                        CapitalKind.TIME,
                        AllocationTargetType.TASK,
                        SOURCE_TASK_ID,
                        new BigDecimal("80.0000"),
                        false,
                        "Initial"
                )
        );
        CapitalAllocation allocation = findTaskAllocation(cycle, SOURCE_TASK_ID);

        AllocationResponse response = capitalAllocationService.changeAllocation(
                OWNER_ID,
                allocation.getId(),
                new CapitalAllocationChangeRequest(
                        new BigDecimal("50.0000"),
                        false,
                        "Reduce allocation"
                )
        );
        entityManager.flush();
        entityManager.clear();

        assertThat(response.targetAllocatedAmount()).isEqualByComparingTo("50.0000");
        assertThat(response.totalAllocatedAmount()).isEqualByComparingTo("50.0000");
        assertThat(response.remainingAmount()).isEqualByComparingTo("50.0000");
        assertThat(response.historyIds()).hasSize(1);
        assertThat(findTaskAllocation(cycle, SOURCE_TASK_ID)).satisfies(changedAllocation -> {
            assertThat(changedAllocation.getAllocatedAmount()).isEqualByComparingTo("50.0000");
            assertThat(changedAllocation.getReleasedAmount()).isEqualByComparingTo("0.0000");
            assertThat(changedAllocation.getStatus()).isEqualTo(AllocationStatus.ACTIVE);
        });
        assertThat(capitalHistoryRepository.findByCapitalCycleIdAndActionType(
                cycle.getId(),
                CapitalActionType.ALLOCATE,
                PageRequest.of(0, 10)
        ).getContent()).hasSize(1);
        assertThat(capitalHistoryRepository.findByCapitalCycleIdAndActionType(
                cycle.getId(),
                CapitalActionType.RELEASE,
                PageRequest.of(0, 10)
        ).getContent()).isEmpty();
        assertThat(capitalHistoryRepository.findByCapitalCycleIdAndActionType(
                cycle.getId(),
                CapitalActionType.REALLOCATE,
                PageRequest.of(0, 10)
        ).getContent()).singleElement().satisfies(history -> {
            assertThat(history.getReferenceId()).isEqualTo(SOURCE_TASK_ID);
            assertThat(history.getBeforeAmount()).isEqualByComparingTo("80.0000");
            assertThat(history.getAfterAmount()).isEqualByComparingTo("50.0000");
        });
    }

    @Test
    void changeAllocationDecreaseRejectsAmountBelowSpentAndDoesNotWriteReallocationHistory() {
        CapitalCycle cycle = createCycle("August 11", LocalDate.of(2026, 8, 11), false);
        capitalService.setupTimeCapital(OWNER_ID, cycle.getId(), new SetupTimeCapitalRequest(100L));
        activateCycle(cycle);
        allocationService.allocateCapital(
                OWNER_ID,
                cycle.getId(),
                new AllocateCapitalRequest(
                        CapitalKind.TIME,
                        AllocationTargetType.TASK,
                        SOURCE_TASK_ID,
                        new BigDecimal("80.0000"),
                        false,
                        "Initial"
                )
        );
        CapitalAllocation allocation = findTaskAllocation(cycle, SOURCE_TASK_ID);
        allocation.spend(new BigDecimal("60.0000"));
        capitalAllocationRepository.saveAndFlush(allocation);
        entityManager.flush();
        entityManager.clear();

        CapitalAllocation spentAllocation = findTaskAllocation(cycle, SOURCE_TASK_ID);
        assertThatThrownBy(() -> capitalAllocationService.changeAllocation(
                OWNER_ID,
                spentAllocation.getId(),
                new CapitalAllocationChangeRequest(
                        new BigDecimal("50.0000"),
                        false,
                        "Below spent amount"
                )
        )).isInstanceOf(InsufficientAllocatedCapitalException.class);
        entityManager.flush();
        entityManager.clear();

        assertThat(findTaskAllocation(cycle, SOURCE_TASK_ID)).satisfies(unchangedAllocation -> {
            assertThat(unchangedAllocation.getAllocatedAmount()).isEqualByComparingTo("80.0000");
            assertThat(unchangedAllocation.getSpentAmount()).isEqualByComparingTo("60.0000");
            assertThat(unchangedAllocation.getReleasedAmount()).isEqualByComparingTo("0.0000");
            assertThat(unchangedAllocation.getStatus()).isEqualTo(AllocationStatus.ACTIVE);
        });
        assertThat(capitalHistoryRepository.findByCapitalCycleIdAndActionType(
                cycle.getId(),
                CapitalActionType.REALLOCATE,
                PageRequest.of(0, 10)
        ).getContent()).isEmpty();
    }

    @Test
    void changeAllocationRejectsReleasedAllocation() {
        CapitalCycle cycle = createCycle("August 10", LocalDate.of(2026, 8, 10), false);
        capitalService.setupTimeCapital(OWNER_ID, cycle.getId(), new SetupTimeCapitalRequest(100L));
        activateCycle(cycle);
        allocationService.allocateCapital(
                OWNER_ID,
                cycle.getId(),
                new AllocateCapitalRequest(
                        CapitalKind.TIME,
                        AllocationTargetType.TASK,
                        SOURCE_TASK_ID,
                        new BigDecimal("40.0000"),
                        false,
                        "Initial"
                )
        );
        CapitalAllocation allocation = findTaskAllocation(cycle, SOURCE_TASK_ID);
        capitalAllocationService.releaseCapital(
                OWNER_ID,
                allocation.getId(),
                new CapitalAllocationReleaseRequest(new BigDecimal("40.0000"), "Done")
        );
        entityManager.flush();
        entityManager.clear();

        CapitalAllocation releasedAllocation = findTaskAllocation(cycle, SOURCE_TASK_ID);
        assertThat(releasedAllocation.getStatus()).isEqualTo(AllocationStatus.RELEASED);
        assertThatThrownBy(() -> capitalAllocationService.changeAllocation(
                OWNER_ID,
                releasedAllocation.getId(),
                new CapitalAllocationChangeRequest(
                        new BigDecimal("10.0000"),
                        false,
                        "Too late"
                )
        )).isInstanceOf(InvalidAllocationStateException.class);
    }

    @Test
    void capitalOverviewUsesPersistedAllocatedAmount() {
        CapitalCycle cycle = createCycle("August 7", LocalDate.of(2026, 8, 7), false);
        capitalService.setupTimeCapital(OWNER_ID, cycle.getId(), new SetupTimeCapitalRequest(90L));
        activateCycle(cycle);
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

    private void activateCycle(CapitalCycle cycle) {
        cycle.activate(Instant.parse("2026-08-04T00:00:00Z"));
        capitalCycleRepository.saveAndFlush(cycle);
    }

    private CapitalAllocation findTaskAllocation(CapitalCycle cycle, UUID taskId) {
        return capitalAllocationRepository.findByUserIdAndCapitalCycleIdAndTargetTypeAndTargetIdAndCapitalType(
                OWNER_ID,
                cycle.getId(),
                AllocationTargetType.TASK,
                taskId,
                CapitalKind.TIME
        ).orElseThrow();
    }
}
