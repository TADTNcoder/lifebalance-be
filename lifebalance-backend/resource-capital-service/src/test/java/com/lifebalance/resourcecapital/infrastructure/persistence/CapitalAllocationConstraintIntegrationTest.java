package com.lifebalance.resourcecapital.infrastructure.persistence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "eureka.client.enabled=false"
})
class CapitalAllocationConstraintIntegrationTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    @AfterEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM resourcecapital.capital_allocations");
        jdbcTemplate.update("DELETE FROM resourcecapital.money_capitals");
        jdbcTemplate.update("DELETE FROM resourcecapital.time_capitals");
        jdbcTemplate.update("DELETE FROM resourcecapital.capital_cycles");
    }

    @Test
    void rejectsFractionalTimeAllocationAmount() {
        UUID cycleId = insertCycle();

        assertThatThrownBy(() -> jdbcTemplate.update("""
                        INSERT INTO resourcecapital.capital_allocations (
                            id, capital_cycle_id, user_id, capital_type, target_type, target_id, allocated_amount,
                            created_at, updated_at, version
                        )
                        VALUES (?, ?, ?, 'TIME', 'TASK', ?, 120.5000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
                        """, UUID.randomUUID(), cycleId, OWNER_ID, UUID.randomUUID()))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("chk_capital_allocations_time_whole_minutes");
    }

    private UUID insertCycle() {
        UUID cycleId = UUID.randomUUID();
        jdbcTemplate.update("""
                        INSERT INTO resourcecapital.capital_cycles (
                            id, owner_id, cycle_type, start_date, end_date, status
                        )
                        VALUES (?, ?, 'DAILY', ?, ?, 'ACTIVE')
                        """,
                cycleId,
                OWNER_ID,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 1)
        );
        return cycleId;
    }
}
