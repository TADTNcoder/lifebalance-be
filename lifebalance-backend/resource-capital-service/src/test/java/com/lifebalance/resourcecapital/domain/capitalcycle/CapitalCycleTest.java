package com.lifebalance.resourcecapital.domain.capitalcycle;

import com.lifebalance.resourcecapital.domain.capitalcycle.exception.InvalidCapitalCyclePeriodException;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.InvalidCapitalCycleStateException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import jakarta.persistence.Version;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CapitalCycleTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final Instant NOW = Instant.parse("2026-08-01T00:00:00Z");

    @Test
    void createsValidDailyCycle() {
        CapitalCycle cycle = createDaily();

        assertThat(cycle.getOwnerId()).isEqualTo(OWNER_ID);
        assertThat(cycle.getType()).isEqualTo(CapitalCycleType.DAILY);
        assertThat(cycle.getStartDate()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(cycle.getEndDate()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(cycle.isDraft()).isTrue();
        assertThat(cycle.isOverAllocationAllowed()).isFalse();
    }

    @Test
    void createsCycleWithoutName() {
        CapitalCycle cycle = CapitalCycle.create(
                OWNER_ID,
                " ",
                "Daily resource cycle",
                CapitalCycleType.DAILY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 1)
        );

        assertThat(cycle.getName()).isNull();
        assertThat(cycle.isDraft()).isTrue();
    }

    @Test
    void rejectsDailyCycleWithDifferentDates() {
        assertThatThrownBy(() -> CapitalCycle.create(
                OWNER_ID,
                "Daily",
                null,
                CapitalCycleType.DAILY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 2)
        )).isInstanceOf(InvalidCapitalCyclePeriodException.class)
                .hasMessageContaining("DAILY");
    }

    @Test
    void rejectsWeeklyCycleThatDoesNotCoverSevenDays() {
        assertThatThrownBy(() -> CapitalCycle.create(
                OWNER_ID,
                "Weekly",
                null,
                CapitalCycleType.WEEKLY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 6)
        )).isInstanceOf(InvalidCapitalCyclePeriodException.class)
                .hasMessageContaining("WEEKLY");
    }

    @Test
    void rejectsMonthlyCycleThatDoesNotStartAtFirstDayOfMonth() {
        assertThatThrownBy(() -> CapitalCycle.create(
                OWNER_ID,
                "Monthly",
                null,
                CapitalCycleType.MONTHLY,
                LocalDate.of(2026, 8, 2),
                LocalDate.of(2026, 8, 31)
        )).isInstanceOf(InvalidCapitalCyclePeriodException.class)
                .hasMessageContaining("first day");
    }

    @Test
    void rejectsMonthlyCycleThatDoesNotEndAtLastDayOfMonth() {
        assertThatThrownBy(() -> CapitalCycle.create(
                OWNER_ID,
                "Monthly",
                null,
                CapitalCycleType.MONTHLY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 30)
        )).isInstanceOf(InvalidCapitalCyclePeriodException.class)
                .hasMessageContaining("last day");
    }

    @Test
    void rejectsPeriodWhenStartDateIsAfterEndDate() {
        assertThatThrownBy(() -> CapitalCycle.create(
                OWNER_ID,
                "Invalid",
                null,
                CapitalCycleType.WEEKLY,
                LocalDate.of(2026, 8, 8),
                LocalDate.of(2026, 8, 1)
        )).isInstanceOf(InvalidCapitalCyclePeriodException.class)
                .hasMessageContaining("startDate");
    }

    @Test
    void draftCycleCanActivate() {
        CapitalCycle cycle = createDaily();

        cycle.activate(NOW);

        assertThat(cycle.isActive()).isTrue();
        assertThat(cycle.getActivatedAt()).isEqualTo(NOW);
    }

    @Test
    void activeCycleCannotActivateAgain() {
        CapitalCycle cycle = createDaily();
        cycle.activate(NOW);

        assertThatThrownBy(() -> cycle.activate(NOW.plusSeconds(60)))
                .isInstanceOf(InvalidCapitalCycleStateException.class)
                .hasMessageContaining("ACTIVE")
                .hasMessageContaining("activate");
    }

    @Test
    void activeCycleCanClose() {
        CapitalCycle cycle = createDaily();
        cycle.activate(NOW);

        cycle.close("Finished", NOW.plusSeconds(60));

        assertThat(cycle.isClosed()).isTrue();
        assertThat(cycle.getCloseReason()).isEqualTo("Finished");
        assertThat(cycle.getClosedAt()).isEqualTo(NOW.plusSeconds(60));
    }

    @Test
    void draftCycleCannotClose() {
        CapitalCycle cycle = createDaily();

        assertThatThrownBy(() -> cycle.close("Finished", NOW))
                .isInstanceOf(InvalidCapitalCycleStateException.class)
                .hasMessageContaining("DRAFT")
                .hasMessageContaining("close");
    }

    @Test
    void invalidTransitionUsesBusinessErrorCodeAndBadRequestStatus() {
        CapitalCycle cycle = createDaily();

        assertThatThrownBy(() -> cycle.close("Finished", NOW))
                .isInstanceOf(InvalidCapitalCycleStateException.class)
                .satisfies(exception -> {
                    InvalidCapitalCycleStateException stateException =
                            (InvalidCapitalCycleStateException) exception;
                    assertThat(stateException.getCode())
                            .isEqualTo(InvalidCapitalCycleStateException.ERROR_CODE);
                    assertThat(stateException.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(stateException.getCurrentStatus()).isEqualTo(CapitalCycleStatus.DRAFT);
                    assertThat(stateException.getRequestedStatus()).isEqualTo(CapitalCycleStatus.CLOSED);
                    assertThat(stateException.getAction()).isEqualTo("close");
                });
    }

    @Test
    void closedCycleCanReopen() {
        CapitalCycle cycle = createClosedCycle();

        cycle.reopen("Need correction", NOW.plusSeconds(120));

        assertThat(cycle.isReopened()).isTrue();
        assertThat(cycle.getReopenReason()).isEqualTo("Need correction");
        assertThat(cycle.getReopenedAt()).isEqualTo(NOW.plusSeconds(120));
    }

    @Test
    void reopenedCycleCanCloseAgain() {
        CapitalCycle cycle = createClosedCycle();
        cycle.reopen("Need correction", NOW.plusSeconds(120));

        cycle.close("Finished after correction", NOW.plusSeconds(180));

        assertThat(cycle.isClosed()).isTrue();
        assertThat(cycle.getCloseReason()).isEqualTo("Finished after correction");
        assertThat(cycle.getClosedAt()).isEqualTo(NOW.plusSeconds(180));
    }

    @Test
    void activeCycleCannotReopen() {
        CapitalCycle cycle = createDaily();
        cycle.activate(NOW);

        assertThatThrownBy(() -> cycle.reopen("Need correction", NOW))
                .isInstanceOf(InvalidCapitalCycleStateException.class)
                .hasMessageContaining("ACTIVE")
                .hasMessageContaining("reopen");
    }

    @Test
    void reopenedCycleCanActivate() {
        CapitalCycle cycle = createClosedCycle();
        cycle.reopen("Need correction", NOW.plusSeconds(120));

        cycle.activate(NOW.plusSeconds(180));

        assertThat(cycle.isActive()).isTrue();
        assertThat(cycle.getActivatedAt()).isEqualTo(NOW.plusSeconds(180));
    }

    @Test
    void activeCycleCannotUpdatePeriod() {
        CapitalCycle cycle = createDaily();
        cycle.activate(NOW);

        assertThatThrownBy(() -> cycle.updateInformation(
                "Updated",
                null,
                CapitalCycleType.WEEKLY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 7)
        )).isInstanceOf(InvalidCapitalCycleStateException.class)
                .hasMessageContaining("ACTIVE")
                .hasMessageContaining("update structural information");
    }

    @Test
    void activeCycleCanUpdateNameAndDescriptionWhenStructuralFieldsDoNotChange() {
        CapitalCycle cycle = createDaily();
        cycle.activate(NOW);

        cycle.updateInformation(
                "August 1 active",
                "Active cycle description",
                CapitalCycleType.DAILY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 1)
        );

        assertThat(cycle.getName()).isEqualTo("August 1 active");
        assertThat(cycle.getDescription()).isEqualTo("Active cycle description");
        assertThat(cycle.getStatus()).isEqualTo(CapitalCycleStatus.ACTIVE);
    }

    @Test
    void draftCycleCanUpdateInformation() {
        CapitalCycle cycle = createDaily();

        cycle.updateInformation(
                "August Week 1",
                "Updated description",
                CapitalCycleType.WEEKLY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 7)
        );

        assertThat(cycle.getName()).isEqualTo("August Week 1");
        assertThat(cycle.getDescription()).isEqualTo("Updated description");
        assertThat(cycle.getType()).isEqualTo(CapitalCycleType.WEEKLY);
        assertThat(cycle.getEndDate()).isEqualTo(LocalDate.of(2026, 8, 7));
    }

    @Test
    void containsReturnsTrueAtDateBoundaries() {
        CapitalCycle cycle = createWeekly();

        assertThat(cycle.contains(LocalDate.of(2026, 8, 1))).isTrue();
        assertThat(cycle.contains(LocalDate.of(2026, 8, 7))).isTrue();
        assertThat(cycle.contains(LocalDate.of(2026, 7, 31))).isFalse();
        assertThat(cycle.contains(LocalDate.of(2026, 8, 8))).isFalse();
    }

    @Test
    void overlapsReturnsTrueForIntersectingPeriod() {
        CapitalCycle cycle = createWeekly();

        assertThat(cycle.overlaps(LocalDate.of(2026, 8, 7), LocalDate.of(2026, 8, 13))).isTrue();
    }

    @Test
    void adjacentCyclesDoNotOverlap() {
        CapitalCycle cycle = createWeekly();

        assertThat(cycle.overlaps(LocalDate.of(2026, 8, 8), LocalDate.of(2026, 8, 14))).isFalse();
    }

    @Test
    void optimisticLockingFieldExists() throws NoSuchFieldException {
        Field versionField = CapitalCycle.class.getDeclaredField("version");

        assertThat(versionField.getType()).isEqualTo(Long.class);
        assertThat(versionField.isAnnotationPresent(Version.class)).isTrue();
    }

    @Test
    void validatesCloseAndReopenReasons() {
        CapitalCycle activeCycle = createDaily();
        activeCycle.activate(NOW);
        assertThatThrownBy(() -> activeCycle.close(" ", NOW.plusSeconds(60)))
                .isInstanceOf(InvalidCapitalCyclePeriodException.class)
                .hasMessageContaining("close reason");

        CapitalCycle closedCycle = createClosedCycle();
        assertThatThrownBy(() -> closedCycle.reopen("x".repeat(1001), NOW.plusSeconds(120)))
                .isInstanceOf(InvalidCapitalCyclePeriodException.class)
                .hasMessageContaining("reopen reason");
    }

    @Test
    void togglesOverAllocationPolicy() {
        CapitalCycle cycle = createDaily();

        cycle.allowOverAllocation();
        assertThat(cycle.isOverAllocationAllowed()).isTrue();

        cycle.disallowOverAllocation();
        assertThat(cycle.isOverAllocationAllowed()).isFalse();
    }

    @Test
    void checksOwnership() {
        CapitalCycle cycle = createDaily();

        assertThat(cycle.belongsTo(OWNER_ID)).isTrue();
        assertThat(cycle.belongsTo(UUID.fromString("22222222-2222-2222-2222-222222222222"))).isFalse();
    }

    private CapitalCycle createDaily() {
        return CapitalCycle.create(
                OWNER_ID,
                "August 1",
                "Daily resource cycle",
                CapitalCycleType.DAILY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 1)
        );
    }

    private CapitalCycle createWeekly() {
        return CapitalCycle.create(
                OWNER_ID,
                "August Week 1",
                null,
                CapitalCycleType.WEEKLY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 7)
        );
    }

    private CapitalCycle createClosedCycle() {
        CapitalCycle cycle = createDaily();
        cycle.activate(NOW);
        cycle.close("Finished", NOW.plusSeconds(60));
        return cycle;
    }
}
