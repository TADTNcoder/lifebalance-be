package com.lifebalance.resourcecapital.migration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.profiles.active=test,integration-seed",
        "eureka.client.enabled=false"
})
class CapitalTestScenarioSeedMigrationTest {

    private static final UUID TEST_OWNER_ID = UUID.fromString("18300000-0000-4000-8000-000000000001");
    private static final UUID TEST_CYCLE_ID = UUID.fromString("18300000-0000-4000-8000-000000000100");
    private static final UUID WITHIN_LIMIT_ALLOCATION_ID = UUID.fromString("18300000-0000-4000-8000-000000000201");
    private static final UUID OVER_CONFIRMED_ALLOCATION_ID = UUID.fromString("18300000-0000-4000-8000-000000000202");
    private static final UUID OVER_UNCONFIRMED_ALLOCATION_ID = UUID.fromString("18300000-0000-4000-8000-000000000203");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    void flywayLoadsTestOnlyCapitalScenarioSeedData() {
        assertThat(count("""
                SELECT COUNT(*)
                FROM resourcecapital.capital_cycles
                WHERE id = ?
                  AND owner_id = ?
                  AND status = 'ACTIVE'
                  AND over_allocation_allowed = TRUE
                """, TEST_CYCLE_ID, TEST_OWNER_ID)).isEqualTo(1L);

        assertThat(singleDecimal("""
                SELECT planned_minutes
                FROM resourcecapital.time_capitals
                WHERE capital_cycle_id = ?
                """, TEST_CYCLE_ID)).isEqualByComparingTo("480.0000");

        assertThat(singleDecimal("""
                SELECT planned_amount
                FROM resourcecapital.money_capitals
                WHERE capital_cycle_id = ?
                """, TEST_CYCLE_ID)).isEqualByComparingTo("1000.0000");

        AllocationSnapshot withinLimit = allocation(WITHIN_LIMIT_ALLOCATION_ID);
        assertThat(withinLimit.capitalType()).isEqualTo("TIME");
        assertThat(withinLimit.targetType()).isEqualTo("TASK");
        assertThat(withinLimit.allocatedAmount()).isEqualByComparingTo("70.0000");
        assertThat(withinLimit.spentAmount()).isEqualByComparingTo("10.0000");
        assertThat(withinLimit.releasedAmount()).isEqualByComparingTo("20.0000");
        assertThat(withinLimit.status()).isEqualTo("ACTIVE");
        assertThat(withinLimit.overAllocated()).isFalse();
        assertThat(withinLimit.overAllocationConfirmed()).isFalse();

        AllocationSnapshot overConfirmed = allocation(OVER_CONFIRMED_ALLOCATION_ID);
        assertThat(overConfirmed.capitalType()).isEqualTo("TIME");
        assertThat(overConfirmed.targetType()).isEqualTo("PROJECT");
        assertThat(overConfirmed.allocatedAmount()).isEqualByComparingTo("630.0000");
        assertThat(overConfirmed.overAllocated()).isTrue();
        assertThat(overConfirmed.overAllocationConfirmed()).isTrue();

        AllocationSnapshot overUnconfirmed = allocation(OVER_UNCONFIRMED_ALLOCATION_ID);
        assertThat(overUnconfirmed.capitalType()).isEqualTo("MONEY");
        assertThat(overUnconfirmed.targetType()).isEqualTo("TASK_CATALOG");
        assertThat(overUnconfirmed.allocatedAmount()).isEqualByComparingTo("1500.0000");
        assertThat(overUnconfirmed.overAllocated()).isTrue();
        assertThat(overUnconfirmed.overAllocationConfirmed()).isFalse();

        assertThat(count("""
                SELECT COUNT(*)
                FROM resourcecapital.capital_adjustments
                WHERE capital_cycle_id = ?
                  AND adjustment_type = 'INCREASE'
                  AND amount_delta = 60.0000
                  AND created_by = ?
                  AND updated_by = ?
                """, TEST_CYCLE_ID, TEST_OWNER_ID, TEST_OWNER_ID)).isEqualTo(1L);
        assertThat(count("""
                SELECT COUNT(*)
                FROM resourcecapital.capital_adjustments
                WHERE capital_cycle_id = ?
                  AND adjustment_type = 'DECREASE'
                  AND amount_delta = -100.0000
                  AND created_by = ?
                  AND updated_by = ?
                """, TEST_CYCLE_ID, TEST_OWNER_ID, TEST_OWNER_ID)).isEqualTo(1L);

        assertThat(count("""
                SELECT COUNT(*)
                FROM resourcecapital.capital_reallocations
                WHERE from_allocation_id = ?
                  AND to_allocation_id = ?
                  AND amount = 30.0000
                """, WITHIN_LIMIT_ALLOCATION_ID, OVER_CONFIRMED_ALLOCATION_ID)).isEqualTo(1L);
        assertThat(count("""
                SELECT COUNT(*)
                FROM resourcecapital.capital_releases
                WHERE allocation_id = ?
                  AND released_amount = 20.0000
                """, WITHIN_LIMIT_ALLOCATION_ID)).isEqualTo(1L);

        List<String> historyActions = jdbcTemplate.queryForList("""
                SELECT action_type
                FROM resourcecapital.capital_histories
                WHERE capital_cycle_id = ?
                ORDER BY created_at ASC, id ASC
                """, String.class, TEST_CYCLE_ID);

        assertThat(historyActions)
                .contains("ADJUSTMENT_INCREASE", "ADJUSTMENT_DECREASE", "ALLOCATE", "OVER_ALLOCATION_APPROVED",
                        "REALLOCATE", "RELEASE");
        assertThat(count("""
                SELECT COUNT(*)
                FROM resourcecapital.capital_histories
                WHERE capital_cycle_id = ?
                  AND action_type = 'ALLOCATE'
                """, TEST_CYCLE_ID)).isEqualTo(3L);
        assertThat(count("""
                SELECT COUNT(*)
                FROM resourcecapital.capital_histories
                WHERE capital_cycle_id = ?
                  AND action_type = 'REALLOCATE'
                """, TEST_CYCLE_ID)).isEqualTo(2L);
        assertThat(count("""
                SELECT COUNT(*)
                FROM resourcecapital.capital_histories
                WHERE capital_cycle_id = ?
                  AND action_type = 'RELEASE'
                """, TEST_CYCLE_ID)).isEqualTo(1L);
    }

    @Test
    void testScenarioSeedScriptCanRunAgainWithoutCreatingDuplicates() {
        long cyclesBefore = countTestCycles();
        long allocationsBefore = countTestAllocations();
        long adjustmentsBefore = countTestAdjustments();
        long reallocationsBefore = countTestReallocations();
        long releasesBefore = countTestReleases();
        long historiesBefore = countTestHistories();

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                resourceLoader.getResource("classpath:db/seed/h2/R__seed_capital_test_scenario_data.sql")
        );
        populator.execute(dataSource);

        assertThat(countTestCycles()).isEqualTo(cyclesBefore);
        assertThat(countTestAllocations()).isEqualTo(allocationsBefore);
        assertThat(countTestAdjustments()).isEqualTo(adjustmentsBefore);
        assertThat(countTestReallocations()).isEqualTo(reallocationsBefore);
        assertThat(countTestReleases()).isEqualTo(releasesBefore);
        assertThat(countTestHistories()).isEqualTo(historiesBefore);
    }

    private AllocationSnapshot allocation(UUID allocationId) {
        return jdbcTemplate.queryForObject("""
                SELECT capital_type,
                       target_type,
                       allocated_amount,
                       spent_amount,
                       released_amount,
                       status,
                       is_over_allocated,
                       over_allocation_confirmed
                FROM resourcecapital.capital_allocations
                WHERE id = ?
                """, (rs, rowNum) -> new AllocationSnapshot(
                rs.getString("capital_type"),
                rs.getString("target_type"),
                rs.getBigDecimal("allocated_amount"),
                rs.getBigDecimal("spent_amount"),
                rs.getBigDecimal("released_amount"),
                rs.getString("status"),
                rs.getBoolean("is_over_allocated"),
                rs.getBoolean("over_allocation_confirmed")
        ), allocationId);
    }

    private BigDecimal singleDecimal(String sql, Object... args) {
        BigDecimal value = jdbcTemplate.queryForObject(sql, BigDecimal.class, args);
        return value == null ? BigDecimal.ZERO : value;
    }

    private long countTestCycles() {
        return count("""
                SELECT COUNT(*)
                FROM resourcecapital.capital_cycles
                WHERE id = ?
                """, TEST_CYCLE_ID);
    }

    private long countTestAllocations() {
        return count("""
                SELECT COUNT(*)
                FROM resourcecapital.capital_allocations
                WHERE capital_cycle_id = ?
                """, TEST_CYCLE_ID);
    }

    private long countTestAdjustments() {
        return count("""
                SELECT COUNT(*)
                FROM resourcecapital.capital_adjustments
                WHERE capital_cycle_id = ?
                """, TEST_CYCLE_ID);
    }

    private long countTestReallocations() {
        return count("""
                SELECT COUNT(*)
                FROM resourcecapital.capital_reallocations
                WHERE from_allocation_id = ?
                   OR to_allocation_id = ?
                """, WITHIN_LIMIT_ALLOCATION_ID, OVER_CONFIRMED_ALLOCATION_ID);
    }

    private long countTestReleases() {
        return count("""
                SELECT COUNT(*)
                FROM resourcecapital.capital_releases
                WHERE allocation_id = ?
                """, WITHIN_LIMIT_ALLOCATION_ID);
    }

    private long countTestHistories() {
        return count("""
                SELECT COUNT(*)
                FROM resourcecapital.capital_histories
                WHERE capital_cycle_id = ?
                """, TEST_CYCLE_ID);
    }

    private long count(String sql, Object... args) {
        Long count = jdbcTemplate.queryForObject(sql, Long.class, args);
        return count == null ? 0L : count;
    }

    private record AllocationSnapshot(
            String capitalType,
            String targetType,
            BigDecimal allocatedAmount,
            BigDecimal spentAmount,
            BigDecimal releasedAmount,
            String status,
            boolean overAllocated,
            boolean overAllocationConfirmed
    ) {
    }
}
