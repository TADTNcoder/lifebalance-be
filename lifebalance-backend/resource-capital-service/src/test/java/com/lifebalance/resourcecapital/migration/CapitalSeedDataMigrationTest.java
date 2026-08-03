package com.lifebalance.resourcecapital.migration;

import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;
import com.lifebalance.resourcecapital.dto.CapitalOverviewResponse;
import com.lifebalance.resourcecapital.service.CapitalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.profiles.active=test,integration-seed",
        "eureka.client.enabled=false"
})
class CapitalSeedDataMigrationTest {

    private static final UUID DEMO_OWNER_ID = UUID.fromString("975946b2-90c3-4206-a2de-4f6652fcaa71");
    private static final UUID ACTIVE_AUGUST_CYCLE_ID = UUID.fromString("81100000-0000-4000-8000-000000082026");
    private static final UUID DRAFT_SEPTEMBER_CYCLE_ID = UUID.fromString("81100000-0000-4000-8000-000000092026");

    @Autowired
    private CapitalService capitalService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    void flywayLoadsCapitalSeedDataForIntegrationSeedProfile() {
        assertThat(countActiveMonthlyCyclesForDemoOwner()).isEqualTo(1);

        CapitalOverviewResponse activeOverview = capitalService.getCapitalOverview(
                DEMO_OWNER_ID,
                ACTIVE_AUGUST_CYCLE_ID
        );
        CapitalOverviewResponse draftOverview = capitalService.getCapitalOverview(
                DEMO_OWNER_ID,
                DRAFT_SEPTEMBER_CYCLE_ID
        );

        assertThat(activeOverview.cycleStatus()).isEqualTo(CapitalCycleStatus.ACTIVE);
        assertThat(activeOverview.timeCapital().initialized()).isTrue();
        assertThat(activeOverview.timeCapital().plannedMinutes()).isEqualTo(9600L);
        assertThat(activeOverview.timeCapital().allocatedMinutes()).isZero();
        assertThat(activeOverview.timeCapital().availableMinutes()).isEqualTo(9600L);
        assertThat(activeOverview.timeCapital().remainingMinutes()).isEqualTo(9600L);
        assertThat(activeOverview.moneyCapital().initialized()).isTrue();
        assertThat(activeOverview.moneyCapital().plannedAmount()).isEqualByComparingTo("15000000.0000");
        assertThat(activeOverview.moneyCapital().allocatedAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(activeOverview.moneyCapital().availableAmount()).isEqualByComparingTo("15000000.0000");
        assertThat(activeOverview.moneyCapital().remainingAmount()).isEqualByComparingTo("15000000.0000");
        assertThat(activeOverview.moneyCapital().currencyCode()).isEqualTo("VND");

        assertThat(draftOverview.cycleStatus()).isEqualTo(CapitalCycleStatus.DRAFT);
        assertThat(draftOverview.timeCapital().initialized()).isFalse();
        assertThat(draftOverview.moneyCapital().initialized()).isFalse();
    }

    @Test
    void seedScriptCanRunAgainWithoutCreatingDuplicates() {
        long cyclesBefore = countSeedCycles();
        long timeCapitalsBefore = countSeedTimeCapitals();
        long moneyCapitalsBefore = countSeedMoneyCapitals();

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                resourceLoader.getResource("classpath:db/seed/h2/R__seed_capital_default_data.sql")
        );
        populator.execute(dataSource);

        assertThat(countSeedCycles()).isEqualTo(cyclesBefore);
        assertThat(countSeedTimeCapitals()).isEqualTo(timeCapitalsBefore);
        assertThat(countSeedMoneyCapitals()).isEqualTo(moneyCapitalsBefore);
    }

    private long countActiveMonthlyCyclesForDemoOwner() {
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM resourcecapital.capital_cycles
                WHERE owner_id = ?
                  AND cycle_type = 'MONTHLY'
                  AND status = 'ACTIVE'
                """, Long.class, DEMO_OWNER_ID);
        return count == null ? 0L : count;
    }

    private long countSeedCycles() {
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM resourcecapital.capital_cycles
                WHERE id IN (?, ?)
                """, Long.class, ACTIVE_AUGUST_CYCLE_ID, DRAFT_SEPTEMBER_CYCLE_ID);
        return count == null ? 0L : count;
    }

    private long countSeedTimeCapitals() {
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM resourcecapital.time_capitals
                WHERE capital_cycle_id = ?
                """, Long.class, ACTIVE_AUGUST_CYCLE_ID);
        return count == null ? 0L : count;
    }

    private long countSeedMoneyCapitals() {
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM resourcecapital.money_capitals
                WHERE capital_cycle_id = ?
                """, Long.class, ACTIVE_AUGUST_CYCLE_ID);
        return count == null ? 0L : count;
    }
}
