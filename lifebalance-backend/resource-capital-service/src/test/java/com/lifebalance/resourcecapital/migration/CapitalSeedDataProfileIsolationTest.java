package com.lifebalance.resourcecapital.migration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "eureka.client.enabled=false"
})
class CapitalSeedDataProfileIsolationTest {

    private static final UUID ACTIVE_AUGUST_CYCLE_ID = UUID.fromString("81100000-0000-4000-8000-000000082026");
    private static final UUID DRAFT_SEPTEMBER_CYCLE_ID = UUID.fromString("81100000-0000-4000-8000-000000092026");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void defaultTestProfileDoesNotLoadSampleSeedData() {
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM resourcecapital.capital_cycles
                WHERE id IN (?, ?)
                """, Long.class, ACTIVE_AUGUST_CYCLE_ID, DRAFT_SEPTEMBER_CYCLE_ID);

        assertThat(count).isZero();
    }
}
