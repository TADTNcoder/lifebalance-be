package com.lifebalance.resourcecapital.infrastructure.persistence;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActionType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActorType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalHistory;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalReferenceType;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
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
class CapitalHistoryRepositoryTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ACTOR_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID TASK_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final Instant BASE_TIME = Instant.parse("2026-08-01T00:00:00Z");

    @Autowired
    private CapitalCycleRepository capitalCycleRepository;

    @Autowired
    private CapitalHistoryRepository capitalHistoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesAndFindsCapitalHistoryById() {
        CapitalCycle cycle = capitalCycleRepository.saveAndFlush(dailyCycle("August 1", LocalDate.of(2026, 8, 1)));
        CapitalHistory history = capitalHistoryRepository.saveAndFlush(CapitalHistory.recordAt(
                cycle,
                CapitalKind.MONEY,
                CapitalActionType.CAPITAL_SET,
                new BigDecimal("1000.0000"),
                BigDecimal.ZERO,
                new BigDecimal("1000.0000"),
                "Setup money capital",
                "Initial budget",
                null,
                null,
                CapitalActorType.USER,
                ACTOR_ID,
                BASE_TIME
        ));
        entityManager.clear();

        assertThat(capitalHistoryRepository.findById(history.getId()))
                .isPresent()
                .get()
                .satisfies(found -> {
                    assertThat(found.getCapitalCycle().getId()).isEqualTo(cycle.getId());
                    assertThat(found.getCapitalType()).isEqualTo(CapitalKind.MONEY);
                    assertThat(found.getActionType()).isEqualTo(CapitalActionType.CAPITAL_SET);
                    assertThat(found.getAmount()).isEqualByComparingTo("1000.0000");
                    assertThat(found.getAmount().scale()).isEqualTo(4);
                    assertThat(found.getActorType()).isEqualTo(CapitalActorType.USER);
                    assertThat(found.getActorId()).isEqualTo(ACTOR_ID);
                    assertThat(found.getCreatedAt()).isEqualTo(BASE_TIME);
                });
    }

    @Test
    void findsHistoryByCycleWithStableNewestFirstOrder() {
        CapitalCycle cycle = capitalCycleRepository.saveAndFlush(dailyCycle("August 2", LocalDate.of(2026, 8, 2)));
        UUID olderId = UUID.fromString("44444444-4444-4444-4444-444444444441");
        UUID tieLowerId = UUID.fromString("44444444-4444-4444-4444-444444444442");
        UUID tieHigherId = UUID.fromString("44444444-4444-4444-4444-444444444443");
        insertHistoryRow(olderId, cycle.getId(), CapitalActionType.CYCLE_CREATED, null, BASE_TIME);
        insertHistoryRow(tieLowerId, cycle.getId(), CapitalActionType.CYCLE_UPDATED, null, BASE_TIME.plusSeconds(60));
        insertHistoryRow(tieHigherId, cycle.getId(), CapitalActionType.CYCLE_ACTIVATED, null, BASE_TIME.plusSeconds(60));
        entityManager.clear();

        var page = capitalHistoryRepository.findByCapitalCycleId(cycle.getId(), PageRequest.of(0, 10));

        assertThat(page.getContent())
                .extracting(CapitalHistory::getId)
                .containsExactly(tieHigherId, tieLowerId, olderId);
    }

    @Test
    void filtersByCycleAndCapitalType() {
        CapitalCycle cycle = capitalCycleRepository.saveAndFlush(dailyCycle("August 3", LocalDate.of(2026, 8, 3)));
        CapitalHistory timeHistory = capitalHistoryRepository.save(capitalHistory(
                cycle,
                CapitalKind.TIME,
                CapitalActionType.ALLOCATE,
                CapitalReferenceType.TASK,
                TASK_ID,
                BASE_TIME.plusSeconds(60)
        ));
        capitalHistoryRepository.save(capitalHistory(
                cycle,
                CapitalKind.MONEY,
                CapitalActionType.ALLOCATE,
                CapitalReferenceType.TASK,
                UUID.fromString("33333333-3333-3333-3333-333333333334"),
                BASE_TIME
        ));
        capitalHistoryRepository.flush();
        entityManager.clear();

        var page = capitalHistoryRepository.findByCapitalCycleIdAndCapitalType(
                cycle.getId(),
                CapitalKind.TIME,
                PageRequest.of(0, 10)
        );

        assertThat(page.getContent())
                .extracting(CapitalHistory::getId)
                .containsExactly(timeHistory.getId());
    }

    @Test
    void filtersByCycleAndActionType() {
        CapitalCycle cycle = capitalCycleRepository.saveAndFlush(dailyCycle("August 4", LocalDate.of(2026, 8, 4)));
        CapitalHistory allocation = capitalHistoryRepository.save(capitalHistory(
                cycle,
                CapitalKind.TIME,
                CapitalActionType.ALLOCATE,
                CapitalReferenceType.TASK,
                TASK_ID,
                BASE_TIME.plusSeconds(60)
        ));
        capitalHistoryRepository.save(CapitalHistory.recordAt(
                cycle,
                null,
                CapitalActionType.CYCLE_ACTIVATED,
                null,
                null,
                null,
                "Activated",
                null,
                null,
                null,
                CapitalActorType.USER,
                ACTOR_ID,
                BASE_TIME
        ));
        capitalHistoryRepository.flush();
        entityManager.clear();

        var page = capitalHistoryRepository.findByCapitalCycleIdAndActionType(
                cycle.getId(),
                CapitalActionType.ALLOCATE,
                PageRequest.of(0, 10)
        );

        assertThat(page.getContent())
                .extracting(CapitalHistory::getId)
                .containsExactly(allocation.getId());
    }

    @Test
    void filtersReferenceHistoryWithinCycleScope() {
        CapitalCycle firstCycle = capitalCycleRepository.save(dailyCycle("August 5", LocalDate.of(2026, 8, 5)));
        CapitalCycle secondCycle = capitalCycleRepository.save(dailyCycle("August 6", LocalDate.of(2026, 8, 6)));
        capitalCycleRepository.flush();
        CapitalHistory visible = capitalHistoryRepository.save(capitalHistory(
                firstCycle,
                CapitalKind.TIME,
                CapitalActionType.ALLOCATE,
                CapitalReferenceType.TASK,
                TASK_ID,
                BASE_TIME.plusSeconds(60)
        ));
        capitalHistoryRepository.save(capitalHistory(
                secondCycle,
                CapitalKind.TIME,
                CapitalActionType.ALLOCATE,
                CapitalReferenceType.TASK,
                TASK_ID,
                BASE_TIME.plusSeconds(120)
        ));
        capitalHistoryRepository.flush();
        entityManager.clear();

        var page = capitalHistoryRepository.findByCapitalCycleIdAndReferenceTypeAndReferenceId(
                firstCycle.getId(),
                CapitalReferenceType.TASK,
                TASK_ID,
                PageRequest.of(0, 10)
        );

        assertThat(page.getContent())
                .extracting(CapitalHistory::getId)
                .containsExactly(visible.getId());
    }

    @Test
    void filtersCreatedAtRangeWithInclusiveFromAndExclusiveTo() {
        CapitalCycle cycle = capitalCycleRepository.saveAndFlush(dailyCycle("August 7", LocalDate.of(2026, 8, 7)));
        CapitalHistory fromBoundary = capitalHistoryRepository.save(cycleAction(
                cycle,
                CapitalActionType.CYCLE_CREATED,
                BASE_TIME
        ));
        CapitalHistory inside = capitalHistoryRepository.save(cycleAction(
                cycle,
                CapitalActionType.CYCLE_UPDATED,
                BASE_TIME.plusSeconds(60)
        ));
        capitalHistoryRepository.save(cycleAction(
                cycle,
                CapitalActionType.CYCLE_ACTIVATED,
                BASE_TIME.plusSeconds(120)
        ));
        capitalHistoryRepository.flush();
        entityManager.clear();

        var page = capitalHistoryRepository.findByCapitalCycleIdAndCreatedAtRange(
                cycle.getId(),
                BASE_TIME,
                BASE_TIME.plusSeconds(120),
                PageRequest.of(0, 10)
        );

        assertThat(page.getContent())
                .extracting(CapitalHistory::getId)
                .containsExactly(inside.getId(), fromBoundary.getId());
    }

    @Test
    void restrictsDeletingCycleWhenHistoryExists() {
        CapitalCycle cycle = capitalCycleRepository.saveAndFlush(dailyCycle("August 8", LocalDate.of(2026, 8, 8)));
        capitalHistoryRepository.saveAndFlush(cycleAction(
                cycle,
                CapitalActionType.CYCLE_CREATED,
                BASE_TIME
        ));
        entityManager.clear();

        CapitalCycle foundCycle = capitalCycleRepository.findById(cycle.getId()).orElseThrow();
        capitalCycleRepository.delete(foundCycle);

        assertThatThrownBy(() -> capitalCycleRepository.flush())
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void databaseRejectsInvalidReferencePair() {
        CapitalCycle cycle = capitalCycleRepository.saveAndFlush(dailyCycle("August 9", LocalDate.of(2026, 8, 9)));

        assertThatThrownBy(() -> {
            entityManager.createNativeQuery("""
                            insert into resourcecapital.capital_histories (
                                id,
                                capital_cycle_id,
                                action_type,
                                reference_type,
                                actor_type,
                                actor_id,
                                created_at
                            ) values (
                                :id,
                                :cycleId,
                                :actionType,
                                :referenceType,
                                :actorType,
                                :actorId,
                                :createdAt
                            )
                            """)
                    .setParameter("id", UUID.fromString("55555555-5555-5555-5555-555555555551"))
                    .setParameter("cycleId", cycle.getId())
                    .setParameter("actionType", CapitalActionType.CYCLE_CREATED.name())
                    .setParameter("referenceType", CapitalReferenceType.TASK.name())
                    .setParameter("actorType", CapitalActorType.USER.name())
                    .setParameter("actorId", ACTOR_ID)
                    .setParameter("createdAt", BASE_TIME)
                    .executeUpdate();
            entityManager.flush();
        }).isInstanceOf(RuntimeException.class);
    }

    private CapitalHistory capitalHistory(
            CapitalCycle cycle,
            CapitalKind capitalType,
            CapitalActionType actionType,
            CapitalReferenceType referenceType,
            UUID referenceId,
            Instant createdAt
    ) {
        return CapitalHistory.recordAt(
                cycle,
                capitalType,
                actionType,
                new BigDecimal("30.0000"),
                BigDecimal.ZERO,
                new BigDecimal("30.0000"),
                actionType.name(),
                null,
                referenceType,
                referenceId,
                CapitalActorType.USER,
                ACTOR_ID,
                createdAt
        );
    }

    private CapitalHistory cycleAction(CapitalCycle cycle, CapitalActionType actionType, Instant createdAt) {
        return CapitalHistory.recordAt(
                cycle,
                null,
                actionType,
                null,
                null,
                null,
                actionType.name(),
                null,
                null,
                null,
                CapitalActorType.USER,
                ACTOR_ID,
                createdAt
        );
    }

    private void insertHistoryRow(
            UUID id,
            UUID cycleId,
            CapitalActionType actionType,
            CapitalKind capitalType,
            Instant createdAt
    ) {
        entityManager.createNativeQuery("""
                        insert into resourcecapital.capital_histories (
                            id,
                            capital_cycle_id,
                            capital_type,
                            action_type,
                            actor_type,
                            actor_id,
                            created_at
                        ) values (
                            :id,
                            :cycleId,
                            :capitalType,
                            :actionType,
                            :actorType,
                            :actorId,
                            :createdAt
                        )
                        """)
                .setParameter("id", id)
                .setParameter("cycleId", cycleId)
                .setParameter("capitalType", capitalType == null ? null : capitalType.name())
                .setParameter("actionType", actionType.name())
                .setParameter("actorType", CapitalActorType.USER.name())
                .setParameter("actorId", ACTOR_ID)
                .setParameter("createdAt", createdAt)
                .executeUpdate();
        entityManager.flush();
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
