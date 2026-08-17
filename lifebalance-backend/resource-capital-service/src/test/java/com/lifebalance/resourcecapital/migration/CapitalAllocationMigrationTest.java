package com.lifebalance.resourcecapital.migration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "eureka.client.enabled=false"
})
class CapitalAllocationMigrationTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_OWNER_ID = UUID.fromString("11111111-1111-1111-1111-222222222222");
    private static final UUID CYCLE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID SOURCE_ALLOCATION_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID TARGET_ALLOCATION_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID TASK_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID PROJECT_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    @AfterEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM resourcecapital.capital_releases");
        jdbcTemplate.update("DELETE FROM resourcecapital.capital_reallocations");
        jdbcTemplate.update("DELETE FROM resourcecapital.capital_allocations");
        jdbcTemplate.update("DELETE FROM resourcecapital.capital_adjustments");
        jdbcTemplate.update("DELETE FROM resourcecapital.capital_histories");
        jdbcTemplate.update("DELETE FROM resourcecapital.money_capitals");
        jdbcTemplate.update("DELETE FROM resourcecapital.time_capitals");
        jdbcTemplate.update("DELETE FROM resourcecapital.capital_cycles");
    }

    @Test
    void flywayCreatesCapitalAllocationLifecycleStorageContract() {
        assertThat(capitalAllocationColumns()).contains(
                "ID",
                "CAPITAL_CYCLE_ID",
                "USER_ID",
                "CAPITAL_TYPE",
                "TARGET_TYPE",
                "TARGET_ID",
                "ALLOCATED_AMOUNT",
                "SPENT_AMOUNT",
                "RELEASED_AMOUNT",
                "STATUS",
                "IS_OVER_ALLOCATED",
                "OVER_ALLOCATION_CONFIRMED",
                "NOTE",
                "CREATED_AT",
                "UPDATED_AT",
                "VERSION"
        );
        insertCycle();
        insertAllocation(
                SOURCE_ALLOCATION_ID,
                "TIME",
                "TASK",
                TASK_ID,
                new BigDecimal("100.0000"),
                new BigDecimal("20.0000"),
                new BigDecimal("30.0000"),
                "ACTIVE",
                false,
                false
        );
        insertAllocation(
                TARGET_ALLOCATION_ID,
                "TIME",
                "PROJECT",
                PROJECT_ID,
                new BigDecimal("25.0000"),
                BigDecimal.ZERO.setScale(4),
                BigDecimal.ZERO.setScale(4),
                "ACTIVE",
                true,
                true
        );
        insertReallocation();
        insertRelease();
        insertHistory(
                "ALLOCATE",
                SOURCE_ALLOCATION_ID,
                new BigDecimal("100.0000"),
                BigDecimal.ZERO,
                new BigDecimal("100.0000")
        );
        insertHistory(
                "REALLOCATE",
                SOURCE_ALLOCATION_ID,
                new BigDecimal("10.0000"),
                new BigDecimal("100.0000"),
                new BigDecimal("90.0000")
        );
        insertHistory(
                "RELEASE",
                SOURCE_ALLOCATION_ID,
                new BigDecimal("5.0000"),
                new BigDecimal("90.0000"),
                new BigDecimal("85.0000")
        );

        AllocationStorageRow source = loadAllocation(SOURCE_ALLOCATION_ID);
        AllocationStorageRow target = loadAllocation(TARGET_ALLOCATION_ID);
        Integer lifecycleHistoryRows = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM resourcecapital.capital_histories
                WHERE reference_type = 'ALLOCATION'
                  AND reference_id = ?
                  AND action_type IN ('ALLOCATE', 'REALLOCATE', 'RELEASE')
                """, Integer.class, SOURCE_ALLOCATION_ID);

        assertThat(source).satisfies(row -> {
            assertThat(row.capitalCycleId()).isEqualTo(CYCLE_ID);
            assertThat(row.userId()).isEqualTo(OWNER_ID);
            assertThat(row.capitalType()).isEqualTo("TIME");
            assertThat(row.targetType()).isEqualTo("TASK");
            assertThat(row.targetId()).isEqualTo(TASK_ID);
            assertThat(row.allocatedAmount()).isEqualByComparingTo("100.0000");
            assertThat(row.spentAmount()).isEqualByComparingTo("20.0000");
            assertThat(row.releasedAmount()).isEqualByComparingTo("30.0000");
            assertThat(row.status()).isEqualTo("ACTIVE");
            assertThat(row.isOverAllocated()).isFalse();
            assertThat(row.overAllocationConfirmed()).isFalse();
        });
        assertThat(target).satisfies(row -> {
            assertThat(row.targetType()).isEqualTo("PROJECT");
            assertThat(row.isOverAllocated()).isTrue();
            assertThat(row.overAllocationConfirmed()).isTrue();
        });
        assertThat(lifecycleHistoryRows).isEqualTo(3);
    }

    @Test
    void flywayRejectsAllocationWhenOwnerDoesNotMatchCycleOwner() {
        insertCycle();

        assertThatThrownBy(() -> insertAllocation(
                SOURCE_ALLOCATION_ID,
                "MONEY",
                "TASK",
                TASK_ID,
                new BigDecimal("50.0000"),
                BigDecimal.ZERO.setScale(4),
                BigDecimal.ZERO.setScale(4),
                "ACTIVE",
                false,
                false,
                OTHER_OWNER_ID
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void flywayRejectsSpentAmountGreaterThanEffectiveAllocatedAmount() {
        insertCycle();

        assertThatThrownBy(() -> insertAllocation(
                SOURCE_ALLOCATION_ID,
                "TIME",
                "TASK",
                TASK_ID,
                new BigDecimal("20.0000"),
                new BigDecimal("30.0000"),
                BigDecimal.ZERO.setScale(4),
                "ACTIVE",
                false,
                false
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    private List<String> capitalAllocationColumns() {
        return jdbcTemplate.queryForList("""
                SELECT UPPER(column_name)
                FROM information_schema.columns
                WHERE UPPER(table_schema) = 'RESOURCECAPITAL'
                  AND UPPER(table_name) = 'CAPITAL_ALLOCATIONS'
                """, String.class);
    }

    private void insertCycle() {
        jdbcTemplate.update("""
                INSERT INTO resourcecapital.capital_cycles (
                    id, owner_id, cycle_type, start_date, end_date, status
                )
                VALUES (?, ?, 'DAILY', ?, ?, 'ACTIVE')
                """, CYCLE_ID, OWNER_ID, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 1));
    }

    private void insertAllocation(
            UUID allocationId,
            String capitalType,
            String targetType,
            UUID targetId,
            BigDecimal allocatedAmount,
            BigDecimal spentAmount,
            BigDecimal releasedAmount,
            String status,
            boolean overAllocated,
            boolean overAllocationConfirmed
    ) {
        insertAllocation(
                allocationId,
                capitalType,
                targetType,
                targetId,
                allocatedAmount,
                spentAmount,
                releasedAmount,
                status,
                overAllocated,
                overAllocationConfirmed,
                OWNER_ID
        );
    }

    private void insertAllocation(
            UUID allocationId,
            String capitalType,
            String targetType,
            UUID targetId,
            BigDecimal allocatedAmount,
            BigDecimal spentAmount,
            BigDecimal releasedAmount,
            String status,
            boolean overAllocated,
            boolean overAllocationConfirmed,
            UUID userId
    ) {
        jdbcTemplate.update("""
                INSERT INTO resourcecapital.capital_allocations (
                    id,
                    capital_cycle_id,
                    user_id,
                    capital_type,
                    target_type,
                    target_id,
                    allocated_amount,
                    spent_amount,
                    released_amount,
                    status,
                    is_over_allocated,
                    over_allocation_confirmed,
                    created_at,
                    updated_at,
                    version
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
                """,
                allocationId,
                CYCLE_ID,
                userId,
                capitalType,
                targetType,
                targetId,
                allocatedAmount,
                spentAmount,
                releasedAmount,
                status,
                overAllocated,
                overAllocationConfirmed
        );
    }

    private void insertReallocation() {
        jdbcTemplate.update("""
                INSERT INTO resourcecapital.capital_reallocations (
                    from_allocation_id,
                    to_allocation_id,
                    amount,
                    reason
                )
                VALUES (?, ?, ?, ?)
                """,
                SOURCE_ALLOCATION_ID,
                TARGET_ALLOCATION_ID,
                new BigDecimal("10.0000"),
                "Move budget to project"
        );
    }

    private void insertRelease() {
        jdbcTemplate.update("""
                INSERT INTO resourcecapital.capital_releases (
                    allocation_id,
                    released_amount,
                    reason
                )
                VALUES (?, ?, ?)
                """,
                SOURCE_ALLOCATION_ID,
                new BigDecimal("5.0000"),
                "Unused focus block"
        );
    }

    private void insertHistory(
            String actionType,
            UUID allocationId,
            BigDecimal amount,
            BigDecimal beforeAmount,
            BigDecimal afterAmount
    ) {
        jdbcTemplate.update("""
                INSERT INTO resourcecapital.capital_histories (
                    id,
                    capital_cycle_id,
                    capital_type,
                    action_type,
                    amount,
                    before_amount,
                    after_amount,
                    reference_type,
                    reference_id,
                    actor_type,
                    actor_id,
                    created_at
                )
                VALUES (?, ?, 'TIME', ?, ?, ?, ?, 'ALLOCATION', ?, 'USER', ?, CURRENT_TIMESTAMP)
                """,
                UUID.randomUUID(),
                CYCLE_ID,
                actionType,
                amount,
                beforeAmount,
                afterAmount,
                allocationId,
                OWNER_ID
        );
    }

    private AllocationStorageRow loadAllocation(UUID allocationId) {
        return jdbcTemplate.queryForObject("""
                SELECT capital_cycle_id,
                       user_id,
                       capital_type,
                       target_type,
                       target_id,
                       allocated_amount,
                       spent_amount,
                       released_amount,
                       status,
                       is_over_allocated,
                       over_allocation_confirmed
                FROM resourcecapital.capital_allocations
                WHERE id = ?
                """, (resultSet, rowNum) -> new AllocationStorageRow(
                resultSet.getObject("capital_cycle_id", UUID.class),
                resultSet.getObject("user_id", UUID.class),
                resultSet.getString("capital_type"),
                resultSet.getString("target_type"),
                resultSet.getObject("target_id", UUID.class),
                resultSet.getBigDecimal("allocated_amount"),
                resultSet.getBigDecimal("spent_amount"),
                resultSet.getBigDecimal("released_amount"),
                resultSet.getString("status"),
                resultSet.getBoolean("is_over_allocated"),
                resultSet.getBoolean("over_allocation_confirmed")
        ), allocationId);
    }

    private record AllocationStorageRow(
            UUID capitalCycleId,
            UUID userId,
            String capitalType,
            String targetType,
            UUID targetId,
            BigDecimal allocatedAmount,
            BigDecimal spentAmount,
            BigDecimal releasedAmount,
            String status,
            boolean isOverAllocated,
            boolean overAllocationConfirmed
    ) {
    }
}
