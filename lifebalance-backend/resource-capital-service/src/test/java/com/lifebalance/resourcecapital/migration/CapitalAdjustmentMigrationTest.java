package com.lifebalance.resourcecapital.migration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "eureka.client.enabled=false"
})
class CapitalAdjustmentMigrationTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CYCLE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

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
    void flywayCreatesCapitalAdjustmentAppendOnlyStorageContract() {
        assertThat(capitalAdjustmentColumns()).contains(
                "ID",
                "CAPITAL_CYCLE_ID",
                "USER_ID",
                "CAPITAL_TYPE",
                "ADJUSTMENT_TYPE",
                "AMOUNT_DELTA",
                "PREVIOUS_AMOUNT",
                "NEW_AMOUNT",
                "REASON",
                "CREATED_AT"
        );
        insertCycle();

        insertAdjustment(
                "TIME",
                "INCREASE",
                new BigDecimal("45.0000"),
                new BigDecimal("120.0000"),
                new BigDecimal("165.0000"),
                "Extend focused work"
        );
        insertAdjustment(
                "TIME",
                "DECREASE",
                new BigDecimal("-15.0000"),
                new BigDecimal("165.0000"),
                new BigDecimal("150.0000"),
                null
        );

        List<AdjustmentRow> rows = jdbcTemplate.query("""
                SELECT capital_cycle_id,
                       user_id,
                       capital_type,
                       adjustment_type,
                       amount_delta,
                       previous_amount,
                       new_amount,
                       reason,
                       created_at
                FROM resourcecapital.capital_adjustments
                ORDER BY id ASC
                """, (resultSet, rowNum) -> new AdjustmentRow(
                resultSet.getObject("capital_cycle_id", UUID.class),
                resultSet.getObject("user_id", UUID.class),
                resultSet.getString("capital_type"),
                resultSet.getString("adjustment_type"),
                resultSet.getBigDecimal("amount_delta"),
                resultSet.getBigDecimal("previous_amount"),
                resultSet.getBigDecimal("new_amount"),
                resultSet.getString("reason"),
                resultSet.getTimestamp("created_at")
        ));

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0)).satisfies(row -> {
            assertThat(row.capitalCycleId()).isEqualTo(CYCLE_ID);
            assertThat(row.userId()).isEqualTo(OWNER_ID);
            assertThat(row.capitalType()).isEqualTo("TIME");
            assertThat(row.adjustmentType()).isEqualTo("INCREASE");
            assertThat(row.amountDelta()).isEqualByComparingTo("45.0000");
            assertThat(row.previousAmount()).isEqualByComparingTo("120.0000");
            assertThat(row.newAmount()).isEqualByComparingTo("165.0000");
            assertThat(row.reason()).isEqualTo("Extend focused work");
            assertThat(row.createdAt()).isNotNull();
        });
        assertThat(rows.get(1)).satisfies(row -> {
            assertThat(row.adjustmentType()).isEqualTo("DECREASE");
            assertThat(row.amountDelta()).isEqualByComparingTo("-15.0000");
            assertThat(row.reason()).isNull();
            assertThat(row.createdAt()).isNotNull();
        });
    }

    @Test
    void flywayRejectsCapitalAdjustmentDeltaThatDoesNotMatchBeforeAndAfterAmounts() {
        insertCycle();

        assertThatThrownBy(() -> insertAdjustment(
                "MONEY",
                "INCREASE",
                new BigDecimal("99.0000"),
                new BigDecimal("10.0000"),
                new BigDecimal("20.0000"),
                "Invalid delta"
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    private List<String> capitalAdjustmentColumns() {
        return jdbcTemplate.queryForList("""
                SELECT UPPER(column_name)
                FROM information_schema.columns
                WHERE UPPER(table_schema) = 'RESOURCECAPITAL'
                  AND UPPER(table_name) = 'CAPITAL_ADJUSTMENTS'
                """, String.class);
    }

    private void insertCycle() {
        jdbcTemplate.update("""
                INSERT INTO resourcecapital.capital_cycles (
                    id, owner_id, cycle_type, start_date, end_date, status
                )
                VALUES (?, ?, 'DAILY', ?, ?, 'DRAFT')
                """, CYCLE_ID, OWNER_ID, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 1));
    }

    private void insertAdjustment(
            String capitalType,
            String adjustmentType,
            BigDecimal amountDelta,
            BigDecimal previousAmount,
            BigDecimal newAmount,
            String reason
    ) {
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
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                CYCLE_ID,
                OWNER_ID,
                capitalType,
                adjustmentType,
                amountDelta,
                previousAmount,
                newAmount,
                reason
        );
    }

    private record AdjustmentRow(
            UUID capitalCycleId,
            UUID userId,
            String capitalType,
            String adjustmentType,
            BigDecimal amountDelta,
            BigDecimal previousAmount,
            BigDecimal newAmount,
            String reason,
            Timestamp createdAt
    ) {
    }
}
