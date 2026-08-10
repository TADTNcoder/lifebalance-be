package com.lifebalance.resourcecapital.service;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.CapitalCycleNotFoundException;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActionType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActorType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalHistory;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalReferenceType;
import com.lifebalance.resourcecapital.dto.HistoryFilterRequest;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalCycleRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalHistoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

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
class CapitalHistoryServiceIntegrationTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_OWNER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID TASK_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID OTHER_TASK_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final Instant BASE_TIME = Instant.parse("2026-08-01T00:00:00Z");

    @Autowired
    private CapitalHistoryService capitalHistoryService;

    @Autowired
    private CapitalCycleRepository capitalCycleRepository;

    @Autowired
    private CapitalHistoryRepository capitalHistoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void getHistoryByCycleFiltersWithPagination() {
        CapitalCycle cycle = capitalCycleRepository.saveAndFlush(dailyCycle(OWNER_ID, "August 1", LocalDate.of(2026, 8, 1)));
        CapitalCycle otherCycle = capitalCycleRepository.saveAndFlush(dailyCycle(OWNER_ID, "August 2", LocalDate.of(2026, 8, 2)));
        CapitalHistory matching = capitalHistoryRepository.save(history(
                cycle,
                CapitalKind.TIME,
                CapitalActionType.ALLOCATE,
                TASK_ID,
                OWNER_ID,
                "Focus task allocation",
                BASE_TIME.plusSeconds(60)
        ));
        capitalHistoryRepository.save(history(
                cycle,
                CapitalKind.TIME,
                CapitalActionType.ALLOCATE,
                OTHER_TASK_ID,
                OWNER_ID,
                "Different reference",
                BASE_TIME.plusSeconds(60)
        ));
        capitalHistoryRepository.save(history(
                cycle,
                CapitalKind.MONEY,
                CapitalActionType.ALLOCATE,
                TASK_ID,
                OWNER_ID,
                "Focus money allocation",
                BASE_TIME.plusSeconds(60)
        ));
        capitalHistoryRepository.save(history(
                cycle,
                CapitalKind.TIME,
                CapitalActionType.RELEASE,
                TASK_ID,
                OWNER_ID,
                "Focus task release",
                BASE_TIME.plusSeconds(60)
        ));
        capitalHistoryRepository.save(history(
                otherCycle,
                CapitalKind.TIME,
                CapitalActionType.ALLOCATE,
                TASK_ID,
                OWNER_ID,
                "Focus task allocation",
                BASE_TIME.plusSeconds(60)
        ));
        capitalHistoryRepository.flush();
        entityManager.clear();

        var filter = new HistoryFilterRequest(
                CapitalKind.TIME,
                CapitalActionType.ALLOCATE,
                BASE_TIME,
                BASE_TIME.plusSeconds(120),
                "focus",
                CapitalReferenceType.TASK,
                TASK_ID,
                CapitalActorType.USER,
                OWNER_ID
        );

        var page = capitalHistoryService.getHistoryByCycle(OWNER_ID, cycle.getId(), filter, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).id()).isEqualTo(matching.getId());
        assertThat(page.getContent().get(0).capitalCycleId()).isEqualTo(cycle.getId());
        assertThat(page.getContent().get(0).reason()).isEqualTo("Focus task allocation");
    }

    @Test
    void getHistoryFiltersAcrossOwnedCycles() {
        CapitalCycle cycle = capitalCycleRepository.saveAndFlush(dailyCycle(OWNER_ID, "August 5", LocalDate.of(2026, 8, 5)));
        CapitalCycle otherOwnedCycle = capitalCycleRepository.saveAndFlush(dailyCycle(OWNER_ID, "August 6", LocalDate.of(2026, 8, 6)));
        CapitalCycle foreignCycle = capitalCycleRepository.saveAndFlush(dailyCycle(OTHER_OWNER_ID, "August 7", LocalDate.of(2026, 8, 7)));
        CapitalHistory matching = capitalHistoryRepository.save(history(
                cycle,
                CapitalKind.MONEY,
                CapitalActionType.ALLOCATE,
                TASK_ID,
                OWNER_ID,
                "Money focus allocation",
                BASE_TIME.plusSeconds(180)
        ));
        capitalHistoryRepository.save(history(
                otherOwnedCycle,
                CapitalKind.TIME,
                CapitalActionType.ALLOCATE,
                TASK_ID,
                OWNER_ID,
                "Time focus allocation",
                BASE_TIME.plusSeconds(180)
        ));
        capitalHistoryRepository.save(history(
                foreignCycle,
                CapitalKind.MONEY,
                CapitalActionType.ALLOCATE,
                TASK_ID,
                OTHER_OWNER_ID,
                "Foreign money focus allocation",
                BASE_TIME.plusSeconds(180)
        ));
        capitalHistoryRepository.flush();
        entityManager.clear();

        var filter = new HistoryFilterRequest(
                CapitalKind.MONEY,
                null,
                BASE_TIME,
                BASE_TIME.plusSeconds(240),
                "focus",
                null,
                null,
                null,
                null
        );

        var page = capitalHistoryService.getHistory(OWNER_ID, null, filter, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).id()).isEqualTo(matching.getId());
        assertThat(page.getContent().get(0).capitalCycleId()).isEqualTo(cycle.getId());
        assertThat(page.getContent().get(0).capitalType()).isEqualTo(CapitalKind.MONEY);
    }

    @Test
    void getHistoryByCycleReturnsEmptyPageWhenCycleHasNoHistory() {
        CapitalCycle cycle = capitalCycleRepository.saveAndFlush(dailyCycle(OWNER_ID, "August 3", LocalDate.of(2026, 8, 3)));

        var page = capitalHistoryService.getHistoryByCycle(
                OWNER_ID,
                cycle.getId(),
                null,
                PageRequest.of(0, 10)
        );

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isZero();
    }

    @Test
    void ownerCannotReadAnotherOwnersHistory() {
        CapitalCycle cycle = capitalCycleRepository.saveAndFlush(dailyCycle(OWNER_ID, "August 4", LocalDate.of(2026, 8, 4)));

        assertThatThrownBy(() -> capitalHistoryService.getHistoryByCycle(
                OTHER_OWNER_ID,
                cycle.getId(),
                null,
                PageRequest.of(0, 10)
        )).isInstanceOf(CapitalCycleNotFoundException.class);
    }

    private CapitalHistory history(
            CapitalCycle cycle,
            CapitalKind capitalType,
            CapitalActionType actionType,
            UUID taskId,
            UUID actorId,
            String reason,
            Instant createdAt
    ) {
        return CapitalHistory.recordAt(
                cycle,
                capitalType,
                actionType,
                new BigDecimal("60.0000"),
                BigDecimal.ZERO.setScale(4),
                new BigDecimal("60.0000"),
                reason,
                "History search description",
                CapitalReferenceType.TASK,
                taskId,
                CapitalActorType.USER,
                actorId,
                createdAt
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
