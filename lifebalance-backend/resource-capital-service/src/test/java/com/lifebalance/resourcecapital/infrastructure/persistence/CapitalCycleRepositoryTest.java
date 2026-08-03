package com.lifebalance.resourcecapital.infrastructure.persistence;

import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

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
class CapitalCycleRepositoryTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_OWNER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final Instant NOW = Instant.parse("2026-08-01T00:00:00Z");

    @Autowired
    private CapitalCycleRepository capitalCycleRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesAndFindsCycleById() {
        CapitalCycle cycle = capitalCycleRepository.saveAndFlush(dailyCycle(
                OWNER_ID,
                "August 1",
                LocalDate.of(2026, 8, 1)
        ));
        entityManager.clear();

        assertThat(capitalCycleRepository.findById(cycle.getId()))
                .isPresent()
                .get()
                .satisfies(found -> {
                    assertThat(found.getOwnerId()).isEqualTo(OWNER_ID);
                    assertThat(found.getType()).isEqualTo(CapitalCycleType.DAILY);
                    assertThat(found.getStatus()).isEqualTo(CapitalCycleStatus.DRAFT);
                    assertThat(found.getStartDate()).isEqualTo(LocalDate.of(2026, 8, 1));
                    assertThat(found.getEndDate()).isEqualTo(LocalDate.of(2026, 8, 1));
                    assertThat(found.getCreatedAt()).isNotNull();
                    assertThat(found.getUpdatedAt()).isNotNull();
                    assertThat(found.getVersion()).isNotNull();
                });
    }

    @Test
    void findByIdAndOwnerIdDoesNotLeakOtherOwnersCycles() {
        CapitalCycle cycle = capitalCycleRepository.saveAndFlush(dailyCycle(
                OWNER_ID,
                "Private daily cycle",
                LocalDate.of(2026, 8, 2)
        ));
        entityManager.clear();

        assertThat(capitalCycleRepository.findByIdAndOwnerId(cycle.getId(), OWNER_ID))
                .isPresent()
                .get()
                .extracting(CapitalCycle::getId)
                .isEqualTo(cycle.getId());
        assertThat(capitalCycleRepository.findByIdAndOwnerId(cycle.getId(), OTHER_OWNER_ID)).isEmpty();
    }

    @Test
    void findByOwnerIdAndStatusReturnsPagedOwnerScopedCycles() {
        CapitalCycle firstDraft = capitalCycleRepository.save(dailyCycle(
                OWNER_ID,
                "August 1",
                LocalDate.of(2026, 8, 1)
        ));
        CapitalCycle secondDraft = capitalCycleRepository.save(dailyCycle(
                OWNER_ID,
                "August 2",
                LocalDate.of(2026, 8, 2)
        ));
        capitalCycleRepository.save(active(dailyCycle(
                OWNER_ID,
                "August 3",
                LocalDate.of(2026, 8, 3)
        )));
        capitalCycleRepository.save(dailyCycle(
                OTHER_OWNER_ID,
                "Other owner August 1",
                LocalDate.of(2026, 8, 1)
        ));
        capitalCycleRepository.flush();
        entityManager.clear();

        var page = capitalCycleRepository.findByOwnerIdAndStatus(
                OWNER_ID,
                CapitalCycleStatus.DRAFT,
                PageRequest.of(0, 1, Sort.by("startDate").ascending())
        );

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent())
                .extracting(CapitalCycle::getId)
                .containsExactly(firstDraft.getId());

        var secondPage = capitalCycleRepository.findByOwnerIdAndStatus(
                OWNER_ID,
                CapitalCycleStatus.DRAFT,
                PageRequest.of(1, 1, Sort.by("startDate").ascending())
        );

        assertThat(secondPage.getContent())
                .extracting(CapitalCycle::getId)
                .containsExactly(secondDraft.getId());
    }

    @Test
    void findsActiveCycleByOwnerTypeAndStatus() {
        CapitalCycle activeDaily = capitalCycleRepository.save(active(dailyCycle(
                OWNER_ID,
                "Active daily",
                LocalDate.of(2026, 8, 4)
        )));
        capitalCycleRepository.save(dailyCycle(
                OWNER_ID,
                "Draft daily",
                LocalDate.of(2026, 8, 5)
        ));
        capitalCycleRepository.save(active(weeklyCycle(
                OWNER_ID,
                "Active weekly",
                LocalDate.of(2026, 8, 1)
        )));
        capitalCycleRepository.save(active(dailyCycle(
                OTHER_OWNER_ID,
                "Other owner active daily",
                LocalDate.of(2026, 8, 4)
        )));
        capitalCycleRepository.flush();
        entityManager.clear();

        assertThat(capitalCycleRepository.findByOwnerIdAndTypeAndStatus(
                OWNER_ID,
                CapitalCycleType.DAILY,
                CapitalCycleStatus.ACTIVE
        )).isPresent()
                .get()
                .extracting(CapitalCycle::getId)
                .isEqualTo(activeDaily.getId());

        assertThat(capitalCycleRepository.findByOwnerIdAndTypeAndStatus(
                OTHER_OWNER_ID,
                CapitalCycleType.WEEKLY,
                CapitalCycleStatus.ACTIVE
        )).isEmpty();
        assertThat(capitalCycleRepository.findByOwnerIdAndTypeAndStatus(
                OWNER_ID,
                CapitalCycleType.MONTHLY,
                CapitalCycleStatus.ACTIVE
        )).isEmpty();
        assertThat(capitalCycleRepository.findByOwnerIdAndTypeAndStatus(
                OWNER_ID,
                CapitalCycleType.DAILY,
                CapitalCycleStatus.REOPENED
        )).isEmpty();
    }

    @Test
    void rejectsTwoActiveCyclesForSameOwnerAndType() {
        capitalCycleRepository.save(active(dailyCycle(
                OWNER_ID,
                "Active daily August 5",
                LocalDate.of(2026, 8, 5)
        )));

        assertThatThrownBy(() -> capitalCycleRepository.saveAndFlush(active(dailyCycle(
                OWNER_ID,
                "Active daily August 6",
                LocalDate.of(2026, 8, 6)
        )))).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void detectsActiveCycleByOwnerTypeAndStatusWithOptionalExclusion() {
        CapitalCycle activeDaily = capitalCycleRepository.saveAndFlush(active(dailyCycle(
                OWNER_ID,
                "Active daily",
                LocalDate.of(2026, 8, 7)
        )));
        entityManager.clear();

        assertThat(capitalCycleRepository.existsByOwnerIdAndTypeAndStatus(
                OWNER_ID,
                CapitalCycleType.DAILY,
                CapitalCycleStatus.ACTIVE
        )).isTrue();
        assertThat(capitalCycleRepository.existsByOwnerIdAndTypeAndStatus(
                OTHER_OWNER_ID,
                CapitalCycleType.DAILY,
                CapitalCycleStatus.ACTIVE
        )).isFalse();
        assertThat(capitalCycleRepository.existsByOwnerIdAndTypeAndStatus(
                OWNER_ID,
                CapitalCycleType.WEEKLY,
                CapitalCycleStatus.ACTIVE
        )).isFalse();
        assertThat(capitalCycleRepository.existsByOwnerIdAndTypeAndStatus(
                OWNER_ID,
                CapitalCycleType.DAILY,
                CapitalCycleStatus.REOPENED
        )).isFalse();
        assertThat(capitalCycleRepository.existsByOwnerIdAndTypeAndStatusAndIdNot(
                OWNER_ID,
                CapitalCycleType.DAILY,
                CapitalCycleStatus.ACTIVE,
                activeDaily.getId()
        )).isFalse();
        assertThat(capitalCycleRepository.existsByOwnerIdAndTypeAndStatusAndIdNot(
                OWNER_ID,
                CapitalCycleType.DAILY,
                CapitalCycleStatus.ACTIVE,
                UUID.fromString("33333333-3333-3333-3333-333333333333")
        )).isTrue();
    }

    @Test
    void detectsActiveCycleExcludingCurrentIdIgnoresDifferentOwnerOrType() {
        CapitalCycle activeDaily = capitalCycleRepository.save(active(dailyCycle(
                OWNER_ID,
                "Active daily",
                LocalDate.of(2026, 8, 8)
        )));
        capitalCycleRepository.save(active(dailyCycle(
                OTHER_OWNER_ID,
                "Other owner active daily",
                LocalDate.of(2026, 8, 8)
        )));
        capitalCycleRepository.save(active(weeklyCycle(
                OWNER_ID,
                "Active weekly",
                LocalDate.of(2026, 8, 8)
        )));
        capitalCycleRepository.flush();
        entityManager.clear();

        assertThat(capitalCycleRepository.existsByOwnerIdAndTypeAndStatusAndIdNot(
                OWNER_ID,
                CapitalCycleType.DAILY,
                CapitalCycleStatus.ACTIVE,
                activeDaily.getId()
        )).isFalse();
    }

    @Test
    void detectsOverlappingCycleForSameOwnerAndType() {
        capitalCycleRepository.saveAndFlush(weeklyCycle(
                OWNER_ID,
                "August week 1",
                LocalDate.of(2026, 8, 1)
        ));
        entityManager.clear();

        assertThat(overlapsWeekly(OWNER_ID, LocalDate.of(2026, 8, 4), LocalDate.of(2026, 8, 10))).isTrue();
        assertThat(overlapsWeekly(OWNER_ID, LocalDate.of(2026, 8, 2), LocalDate.of(2026, 8, 6))).isTrue();
        assertThat(overlapsWeekly(OWNER_ID, LocalDate.of(2026, 7, 30), LocalDate.of(2026, 8, 10))).isTrue();
        assertThat(overlapsWeekly(OWNER_ID, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 7))).isTrue();
        assertThat(overlapsWeekly(OWNER_ID, LocalDate.of(2026, 8, 7), LocalDate.of(2026, 8, 13))).isTrue();
        assertThat(overlapsWeekly(OWNER_ID, LocalDate.of(2026, 8, 8), LocalDate.of(2026, 8, 14))).isFalse();
    }

    @Test
    void detectsCompleteOverlapWhenRequestedPeriodIsInsideExistingPeriod() {
        insertCycleRow(
                UUID.fromString("44444444-4444-4444-4444-444444444441"),
                OWNER_ID,
                CapitalCycleType.MONTHLY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31)
        );

        assertThat(capitalCycleRepository.existsOverlappingCycle(
                OWNER_ID,
                CapitalCycleType.MONTHLY,
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 20),
                null
        )).isTrue();
    }

    @Test
    void detectsContainingOverlapWhenRequestedPeriodWrapsExistingPeriod() {
        insertCycleRow(
                UUID.fromString("44444444-4444-4444-4444-444444444442"),
                OWNER_ID,
                CapitalCycleType.MONTHLY,
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 20)
        );

        assertThat(capitalCycleRepository.existsOverlappingCycle(
                OWNER_ID,
                CapitalCycleType.MONTHLY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31),
                null
        )).isTrue();
    }

    @Test
    void detectsLeadingBoundaryOverlapInclusively() {
        insertCycleRow(
                UUID.fromString("44444444-4444-4444-4444-444444444443"),
                OWNER_ID,
                CapitalCycleType.MONTHLY,
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 20)
        );

        assertThat(capitalCycleRepository.existsOverlappingCycle(
                OWNER_ID,
                CapitalCycleType.MONTHLY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 10),
                null
        )).isTrue();
    }

    @Test
    void detectsTrailingBoundaryOverlapInclusively() {
        insertCycleRow(
                UUID.fromString("44444444-4444-4444-4444-444444444444"),
                OWNER_ID,
                CapitalCycleType.MONTHLY,
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 20)
        );

        assertThat(capitalCycleRepository.existsOverlappingCycle(
                OWNER_ID,
                CapitalCycleType.MONTHLY,
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 8, 30),
                null
        )).isTrue();
    }

    @Test
    void returnsFalseWhenRequestedPeriodDoesNotOverlapExistingPeriod() {
        insertCycleRow(
                UUID.fromString("44444444-4444-4444-4444-444444444445"),
                OWNER_ID,
                CapitalCycleType.MONTHLY,
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 20)
        );

        assertThat(capitalCycleRepository.existsOverlappingCycle(
                OWNER_ID,
                CapitalCycleType.MONTHLY,
                LocalDate.of(2026, 8, 21),
                LocalDate.of(2026, 8, 31),
                null
        )).isFalse();
    }

    @Test
    void ignoresOverlapForDifferentOwnerOrType() {
        capitalCycleRepository.saveAndFlush(weeklyCycle(
                OWNER_ID,
                "August week 1",
                LocalDate.of(2026, 8, 1)
        ));
        entityManager.clear();

        assertThat(overlapsWeekly(OTHER_OWNER_ID, LocalDate.of(2026, 8, 4), LocalDate.of(2026, 8, 10))).isFalse();
        assertThat(capitalCycleRepository.existsOverlappingCycle(
                OWNER_ID,
                CapitalCycleType.MONTHLY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31),
                null
        )).isFalse();
    }

    @Test
    void excludesCurrentCycleFromOverlapCheck() {
        CapitalCycle cycle = capitalCycleRepository.saveAndFlush(weeklyCycle(
                OWNER_ID,
                "August week 2",
                LocalDate.of(2026, 8, 8)
        ));
        entityManager.clear();

        assertThat(capitalCycleRepository.existsOverlappingCycle(
                OWNER_ID,
                CapitalCycleType.WEEKLY,
                LocalDate.of(2026, 8, 8),
                LocalDate.of(2026, 8, 14),
                cycle.getId()
        )).isFalse();
        assertThat(capitalCycleRepository.existsOverlappingCycle(
                OWNER_ID,
                CapitalCycleType.WEEKLY,
                LocalDate.of(2026, 8, 8),
                LocalDate.of(2026, 8, 14),
                null
        )).isTrue();
    }

    @Test
    void detectsOverlapWhenExcludedCycleIsNotTheOnlyConflict() {
        CapitalCycle excludedCycle = capitalCycleRepository.save(weeklyCycle(
                OWNER_ID,
                "August week 3",
                LocalDate.of(2026, 8, 15)
        ));
        capitalCycleRepository.saveAndFlush(weeklyCycle(
                OWNER_ID,
                "Conflicting August week 3",
                LocalDate.of(2026, 8, 16)
        ));
        entityManager.clear();

        assertThat(capitalCycleRepository.existsOverlappingCycle(
                OWNER_ID,
                CapitalCycleType.WEEKLY,
                LocalDate.of(2026, 8, 15),
                LocalDate.of(2026, 8, 21),
                excludedCycle.getId()
        )).isTrue();
    }

    private boolean overlapsWeekly(UUID ownerId, LocalDate startDate, LocalDate endDate) {
        return capitalCycleRepository.existsOverlappingCycle(
                ownerId,
                CapitalCycleType.WEEKLY,
                startDate,
                endDate,
                null
        );
    }

    private void insertCycleRow(
            UUID id,
            UUID ownerId,
            CapitalCycleType type,
            LocalDate startDate,
            LocalDate endDate
    ) {
        entityManager.createNativeQuery("""
                        insert into resourcecapital.capital_cycles (
                            id,
                            owner_id,
                            name,
                            description,
                            cycle_type,
                            start_date,
                            end_date,
                            status,
                            over_allocation_allowed,
                            created_at,
                            updated_at,
                            version
                        ) values (
                            :id,
                            :ownerId,
                            :name,
                            :description,
                            :type,
                            :startDate,
                            :endDate,
                            :status,
                            false,
                            current_timestamp,
                            current_timestamp,
                            0
                        )
                        """)
                .setParameter("id", id)
                .setParameter("ownerId", ownerId)
                .setParameter("name", "Inserted overlap fixture")
                .setParameter("description", "Repository overlap query fixture")
                .setParameter("type", type.name())
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("status", CapitalCycleStatus.DRAFT.name())
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();
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

    private CapitalCycle weeklyCycle(UUID ownerId, String name, LocalDate startDate) {
        return CapitalCycle.create(
                ownerId,
                name,
                "Weekly resource cycle",
                CapitalCycleType.WEEKLY,
                startDate,
                startDate.plusDays(6)
        );
    }

    private CapitalCycle active(CapitalCycle cycle) {
        cycle.activate(NOW);
        return cycle;
    }
}
