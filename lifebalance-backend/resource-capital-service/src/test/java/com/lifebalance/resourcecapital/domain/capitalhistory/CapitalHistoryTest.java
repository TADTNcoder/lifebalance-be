package com.lifebalance.resourcecapital.domain.capitalhistory;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import jakarta.persistence.Version;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CapitalHistoryTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ACTOR_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID REFERENCE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Test
    void actionTypeContainsCapitalHistoryPolicyActions() {
        Set<String> actionNames = Arrays.stream(CapitalActionType.values())
                .map(Enum::name)
                .collect(Collectors.toSet());

        assertThat(actionNames).containsExactlyInAnyOrder(
                "CYCLE_CREATED",
                "CYCLE_UPDATED",
                "CYCLE_ACTIVATED",
                "CYCLE_CLOSED",
                "CYCLE_REOPENED",
                "CAPITAL_SET",
                "ADJUSTMENT_INCREASE",
                "ADJUSTMENT_DECREASE",
                "ALLOCATE",
                "REALLOCATE",
                "RELEASE",
                "OVER_ALLOCATION_APPROVED",
                "TRANSFER_REMAINING"
        );
    }

    @Test
    void createsCycleActionWithoutCapitalTypeOrAmounts() {
        CapitalHistory history = CapitalHistory.record(
                createCycle(),
                null,
                CapitalActionType.CYCLE_CREATED,
                null,
                null,
                null,
                "Created cycle",
                "Initial cycle creation",
                null,
                null,
                CapitalActorType.USER,
                ACTOR_ID
        );

        assertThat(history.getCapitalType()).isNull();
        assertThat(history.getAmount()).isNull();
        assertThat(history.getBeforeAmount()).isNull();
        assertThat(history.getAfterAmount()).isNull();
        assertThat(history.getReason()).isEqualTo("Created cycle");
        assertThat(history.getActorType()).isEqualTo(CapitalActorType.USER);
        assertThat(history.getActorId()).isEqualTo(ACTOR_ID);
    }

    @Test
    void rejectsCapitalTypeForCycleAction() {
        assertThatThrownBy(() -> CapitalHistory.record(
                createCycle(),
                CapitalKind.TIME,
                CapitalActionType.CYCLE_CLOSED,
                null,
                null,
                null,
                "Closed",
                null,
                null,
                null,
                CapitalActorType.USER,
                ACTOR_ID
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Capital type");
    }

    @Test
    void rejectsAmountsForCycleAction() {
        assertThatThrownBy(() -> CapitalHistory.record(
                createCycle(),
                null,
                CapitalActionType.CYCLE_UPDATED,
                new BigDecimal("1.0000"),
                null,
                null,
                "Updated",
                null,
                null,
                null,
                CapitalActorType.USER,
                ACTOR_ID
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amounts");
    }

    @Test
    void createsCapitalSetWithZeroAmountAndNormalizesScale() {
        CapitalHistory history = CapitalHistory.record(
                createCycle(),
                CapitalKind.MONEY,
                CapitalActionType.CAPITAL_SET,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("100"),
                "Setup",
                null,
                null,
                null,
                CapitalActorType.USER,
                ACTOR_ID
        );

        assertThat(history.getAmount()).isEqualByComparingTo("0.0000");
        assertThat(history.getAmount().scale()).isEqualTo(4);
        assertThat(history.getAfterAmount()).isEqualByComparingTo("100.0000");
        assertThat(history.getAfterAmount().scale()).isEqualTo(4);
    }

    @Test
    void capitalAmountActionsRequireCapitalType() {
        assertThatThrownBy(() -> CapitalHistory.record(
                createCycle(),
                null,
                CapitalActionType.ADJUSTMENT_INCREASE,
                new BigDecimal("10.0000"),
                new BigDecimal("100.0000"),
                new BigDecimal("110.0000"),
                "Increase",
                null,
                null,
                null,
                CapitalActorType.USER,
                ACTOR_ID
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Capital type");
    }

    @Test
    void adjustmentAllocateReleaseAndTransferRequirePositiveAmount() {
        for (CapitalActionType actionType : new CapitalActionType[]{
                CapitalActionType.ADJUSTMENT_INCREASE,
                CapitalActionType.ADJUSTMENT_DECREASE,
                CapitalActionType.ALLOCATE,
                CapitalActionType.REALLOCATE,
                CapitalActionType.RELEASE,
                CapitalActionType.OVER_ALLOCATION_APPROVED,
                CapitalActionType.TRANSFER_REMAINING
        }) {
            assertThatThrownBy(() -> CapitalHistory.record(
                    createCycle(),
                    CapitalKind.TIME,
                    actionType,
                    BigDecimal.ZERO,
                    new BigDecimal("100.0000"),
                    new BigDecimal("100.0000"),
                    actionType.name(),
                    null,
                    null,
                    null,
                    CapitalActorType.USER,
                    ACTOR_ID
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("amount");
        }
    }

    @Test
    void rejectsNegativeBeforeOrAfterForRegularCapitalActions() {
        assertThatThrownBy(() -> CapitalHistory.record(
                createCycle(),
                CapitalKind.TIME,
                CapitalActionType.ALLOCATE,
                new BigDecimal("30.0000"),
                new BigDecimal("0.0000"),
                new BigDecimal("-30.0000"),
                "Allocate",
                null,
                CapitalReferenceType.TASK,
                REFERENCE_ID,
                CapitalActorType.USER,
                ACTOR_ID
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("afterAmount");
    }

    @Test
    void overAllocationApprovalAllowsNegativeBalanceSnapshot() {
        CapitalHistory history = CapitalHistory.record(
                createCycle(),
                CapitalKind.TIME,
                CapitalActionType.OVER_ALLOCATION_APPROVED,
                new BigDecimal("30.0000"),
                BigDecimal.ZERO,
                new BigDecimal("-30.0000"),
                "Approved",
                null,
                CapitalReferenceType.TASK,
                REFERENCE_ID,
                CapitalActorType.USER,
                ACTOR_ID
        );

        assertThat(history.getAfterAmount()).isEqualByComparingTo("-30.0000");
    }

    @Test
    void validatesReferencePair() {
        assertThatThrownBy(() -> CapitalHistory.record(
                createCycle(),
                CapitalKind.TIME,
                CapitalActionType.ALLOCATE,
                new BigDecimal("30.0000"),
                BigDecimal.ZERO,
                new BigDecimal("30.0000"),
                "Allocate",
                null,
                CapitalReferenceType.TASK,
                null,
                CapitalActorType.USER,
                ACTOR_ID
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Reference");
    }

    @Test
    void validatesUserAndSystemActors() {
        assertThatThrownBy(() -> CapitalHistory.record(
                createCycle(),
                null,
                CapitalActionType.CYCLE_ACTIVATED,
                null,
                null,
                null,
                "Activate",
                null,
                null,
                null,
                CapitalActorType.USER,
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Actor id");

        CapitalHistory systemHistory = CapitalHistory.record(
                createCycle(),
                null,
                CapitalActionType.CYCLE_ACTIVATED,
                null,
                null,
                null,
                "Activate",
                null,
                null,
                null,
                CapitalActorType.SYSTEM,
                null
        );

        assertThat(systemHistory.getActorType()).isEqualTo(CapitalActorType.SYSTEM);
        assertThat(systemHistory.getActorId()).isNull();
    }

    @Test
    void normalizesBlankText() {
        CapitalHistory history = CapitalHistory.record(
                createCycle(),
                null,
                CapitalActionType.CYCLE_UPDATED,
                null,
                null,
                null,
                " ",
                " ",
                null,
                null,
                CapitalActorType.USER,
                ACTOR_ID
        );

        assertThat(history.getReason()).isNull();
        assertThat(history.getDescription()).isNull();
    }

    @Test
    void isAppendOnlyWithoutVersionOrPublicSetters() {
        assertThat(Arrays.stream(CapitalHistory.class.getDeclaredFields())
                .noneMatch(field -> field.isAnnotationPresent(Version.class))).isTrue();
        assertThat(Arrays.stream(CapitalHistory.class.getMethods())
                .map(Method::getName)
                .noneMatch(name -> name.startsWith("set"))).isTrue();
    }

    private CapitalCycle createCycle() {
        return CapitalCycle.create(
                OWNER_ID,
                "August 1",
                "Daily resource cycle",
                CapitalCycleType.DAILY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 1)
        );
    }
}
