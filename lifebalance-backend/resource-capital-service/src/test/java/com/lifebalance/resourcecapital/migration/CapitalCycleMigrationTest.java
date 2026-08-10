package com.lifebalance.resourcecapital.migration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "eureka.client.enabled=false"
})
class CapitalCycleMigrationTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String PERIOD_BY_TYPE_CONSTRAINT = "chk_resourcecapital_capital_cycles_period_by_type";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    @AfterEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM resourcecapital.capital_allocations");
        jdbcTemplate.update("DELETE FROM resourcecapital.capital_histories");
        jdbcTemplate.update("DELETE FROM resourcecapital.money_capitals");
        jdbcTemplate.update("DELETE FROM resourcecapital.time_capitals");
        jdbcTemplate.update("DELETE FROM resourcecapital.capital_cycles");
    }

    @Test
    void flywayRejectsCapitalCyclePeriodThatDoesNotMatchCycleType() {
        assertThatThrownBy(() -> insertCycle("DAILY", LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 2)))
                .isInstanceOf(DataIntegrityViolationException.class)
                .satisfies(exception -> assertThat(exception.getMessage())
                        .containsIgnoringCase(PERIOD_BY_TYPE_CONSTRAINT));

        assertThatThrownBy(() -> insertCycle("WEEKLY", LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 6)))
                .isInstanceOf(DataIntegrityViolationException.class)
                .satisfies(exception -> assertThat(exception.getMessage())
                        .containsIgnoringCase(PERIOD_BY_TYPE_CONSTRAINT));

        assertThatThrownBy(() -> insertCycle("MONTHLY", LocalDate.of(2026, 8, 2), LocalDate.of(2026, 8, 31)))
                .isInstanceOf(DataIntegrityViolationException.class)
                .satisfies(exception -> assertThat(exception.getMessage())
                        .containsIgnoringCase(PERIOD_BY_TYPE_CONSTRAINT));
    }

    @Test
    void flywayAllowsCapitalCyclePeriodThatMatchesCycleType() {
        insertCycle("DAILY", LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 1));
        insertCycle("WEEKLY", LocalDate.of(2026, 8, 2), LocalDate.of(2026, 8, 8));
        insertCycle("MONTHLY", LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));

        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM resourcecapital.capital_cycles",
                Long.class
        );

        assertThat(count).isEqualTo(3L);
    }

    private void insertCycle(String cycleType, LocalDate startDate, LocalDate endDate) {
        jdbcTemplate.update("""
                INSERT INTO resourcecapital.capital_cycles (
                    owner_id, cycle_type, start_date, end_date, status
                )
                VALUES (?, ?, ?, ?, 'DRAFT')
                """, OWNER_ID, cycleType, startDate, endDate);
    }
}
