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
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "eureka.client.enabled=false"
})
class CapitalForeignKeyAndIndexMigrationTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_OWNER_ID = UUID.fromString("11111111-1111-1111-1111-222222222222");
    private static final UUID CYCLE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID ALLOCATION_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID INVALID_ALLOCATION_ID = UUID.fromString("33333333-3333-3333-3333-444444444444");
    private static final UUID TASK_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID PROJECT_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");

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
    void flywayAddsSprint4ForeignKeysAndSearchIndexes() {
        assertThat(tableConstraints("CAPITAL_ADJUSTMENTS"))
                .contains("FK_CAPITAL_ADJUSTMENTS_OWNER_CYCLE");
        assertThat(indexes("CAPITAL_ADJUSTMENTS"))
                .contains(
                        "IDX_CAP_ADJ_USER_CYCLE",
                        "IDX_CAP_ADJ_TYPE_CREATED",
                        "IDX_CAP_ADJ_ACTION_CREATED"
                );
        assertThat(indexes("CAPITAL_ALLOCATIONS"))
                .contains(
                        "IDX_CAP_ALLOC_TARGET",
                        "IDX_CAP_ALLOC_STATUS",
                        "IDX_CAP_ALLOC_USER_CYCLE_STATUS_UPDATED",
                        "IDX_CAP_ALLOC_OVER_ALLOCATED"
                );
        assertThat(indexes("CAPITAL_HISTORIES"))
                .contains(
                        "IDX_CAP_HIST_CYCLE_ACTION_TIME",
                        "IDX_CAP_HIST_TYPE_TIME",
                        "IDX_CAP_HIST_ACTOR_TIME",
                        "IDX_CAP_HIST_REFERENCE_TIME"
                );
        assertThat(indexes("CAPITAL_REALLOCATIONS")).contains("IDX_CAP_REALLOC_TIME");
        assertThat(indexes("CAPITAL_RELEASES")).contains("IDX_CAP_RELEASE_TIME");
    }

    @Test
    void flywayRejectsCapitalAdjustmentWhenOwnerDoesNotMatchCycleOwner() {
        insertCycle();

        assertThatThrownBy(() -> insertAdjustment(OTHER_OWNER_ID))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void constraintsAllowConfirmedOverAllocationButRejectInvalidAmounts() {
        insertCycle();

        insertAllocation(
                ALLOCATION_ID,
                TASK_ID,
                new BigDecimal("1000000.0000"),
                BigDecimal.ZERO.setScale(4),
                BigDecimal.ZERO.setScale(4),
                true,
                true
        );
        Boolean overAllocated = jdbcTemplate.queryForObject("""
                SELECT is_over_allocated
                FROM resourcecapital.capital_allocations
                WHERE id = ?
                """, Boolean.class, ALLOCATION_ID);
        Boolean overAllocationConfirmed = jdbcTemplate.queryForObject("""
                SELECT over_allocation_confirmed
                FROM resourcecapital.capital_allocations
                WHERE id = ?
                """, Boolean.class, ALLOCATION_ID);

        assertThat(overAllocated).isTrue();
        assertThat(overAllocationConfirmed).isTrue();
        assertThatThrownBy(() -> insertAllocation(
                INVALID_ALLOCATION_ID,
                PROJECT_ID,
                new BigDecimal("100.0000"),
                new BigDecimal("-1.0000"),
                BigDecimal.ZERO.setScale(4),
                false,
                false
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    private Set<String> tableConstraints(String tableName) {
        return new TreeSet<>(jdbcTemplate.queryForList("""
                SELECT UPPER(constraint_name)
                FROM information_schema.table_constraints
                WHERE UPPER(table_schema) = 'RESOURCECAPITAL'
                  AND UPPER(table_name) = ?
                """, String.class, tableName));
    }

    private Set<String> indexes(String tableName) {
        return new TreeSet<>(jdbcTemplate.queryForList("""
                SELECT DISTINCT UPPER(index_name)
                FROM information_schema.index_columns
                WHERE UPPER(table_schema) = 'RESOURCECAPITAL'
                  AND UPPER(table_name) = ?
                """, String.class, tableName));
    }

    private void insertCycle() {
        jdbcTemplate.update("""
                INSERT INTO resourcecapital.capital_cycles (
                    id, owner_id, cycle_type, start_date, end_date, status
                )
                VALUES (?, ?, 'DAILY', ?, ?, 'ACTIVE')
                """, CYCLE_ID, OWNER_ID, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 1));
    }

    private void insertAdjustment(UUID userId) {
        jdbcTemplate.update("""
                INSERT INTO resourcecapital.capital_adjustments (
                    capital_cycle_id,
                    user_id,
                    capital_type,
                    adjustment_type,
                    amount_delta,
                    previous_amount,
                    new_amount,
                    reason
                )
                VALUES (?, ?, 'TIME', 'INCREASE', ?, ?, ?, ?)
                """,
                CYCLE_ID,
                userId,
                new BigDecimal("30.0000"),
                new BigDecimal("60.0000"),
                new BigDecimal("90.0000"),
                "Sprint 4 FK check"
        );
    }

    private void insertAllocation(
            UUID allocationId,
            UUID targetId,
            BigDecimal allocatedAmount,
            BigDecimal spentAmount,
            BigDecimal releasedAmount,
            boolean overAllocated,
            boolean overAllocationConfirmed
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
                VALUES (?, ?, ?, 'TIME', 'TASK', ?, ?, ?, ?, 'ACTIVE', ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
                """,
                allocationId,
                CYCLE_ID,
                OWNER_ID,
                targetId,
                allocatedAmount,
                spentAmount,
                releasedAmount,
                overAllocated,
                overAllocationConfirmed
        );
    }
}
