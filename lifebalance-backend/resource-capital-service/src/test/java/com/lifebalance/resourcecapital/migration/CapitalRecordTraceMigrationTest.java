package com.lifebalance.resourcecapital.migration;

import com.lifebalance.resourcecapital.domain.capital.CapitalAdjustmentType;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitaladjustment.CapitalAdjustment;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;
import com.lifebalance.resourcecapital.domain.capitalallocation.CapitalAllocation;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalAdjustmentRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalAllocationRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalCycleRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "eureka.client.enabled=false"
})
class CapitalRecordTraceMigrationTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TARGET_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CapitalCycleRepository capitalCycleRepository;

    @Autowired
    private CapitalAdjustmentRepository capitalAdjustmentRepository;

    @Autowired
    private CapitalAllocationRepository capitalAllocationRepository;

    @Autowired
    private EntityManager entityManager;

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
    void flywayAddsTechnicalTraceColumnsAndIndexes() {
        assertThat(columns("CAPITAL_ADJUSTMENTS"))
                .contains("UPDATED_AT", "CREATED_BY", "UPDATED_BY");
        assertThat(columns("CAPITAL_ALLOCATIONS"))
                .contains("CREATED_BY", "UPDATED_BY");

        assertThat(indexes("CAPITAL_ADJUSTMENTS"))
                .contains(
                        "IDX_CAP_ADJ_CREATED_BY_CREATED_AT",
                        "IDX_CAP_ADJ_UPDATED_BY_UPDATED_AT"
                );
        assertThat(indexes("CAPITAL_ALLOCATIONS"))
                .contains(
                        "IDX_CAP_ALLOC_CREATED_BY_CREATED_AT",
                        "IDX_CAP_ALLOC_UPDATED_BY_UPDATED_AT"
                );
    }

    @Test
    void jpaPopulatesTechnicalTraceFieldsFromOwnerReference() {
        CapitalCycle cycle = capitalCycleRepository.saveAndFlush(CapitalCycle.create(
                OWNER_ID,
                "Daily capital",
                "Traceability test cycle",
                CapitalCycleType.DAILY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 1)
        ));
        CapitalAdjustment adjustment = capitalAdjustmentRepository.saveAndFlush(CapitalAdjustment.record(
                cycle,
                OWNER_ID,
                CapitalKind.TIME,
                CapitalAdjustmentType.INCREASE,
                new BigDecimal("60.0000"),
                new BigDecimal("90.0000"),
                "Trace adjustment"
        ));
        CapitalAllocation allocation = capitalAllocationRepository.saveAndFlush(CapitalAllocation.create(
                cycle,
                CapitalKind.TIME,
                AllocationTargetType.TASK,
                TARGET_ID,
                new BigDecimal("30.0000")
        ));
        entityManager.clear();

        CapitalAdjustment savedAdjustment = capitalAdjustmentRepository.findById(adjustment.getId()).orElseThrow();
        CapitalAllocation savedAllocation = capitalAllocationRepository.findById(allocation.getId()).orElseThrow();

        assertThat(savedAdjustment.getCreatedBy()).isEqualTo(OWNER_ID);
        assertThat(savedAdjustment.getUpdatedBy()).isEqualTo(OWNER_ID);
        assertThat(savedAdjustment.getCreatedAt()).isNotNull();
        assertThat(savedAdjustment.getUpdatedAt()).isNotNull();
        assertThat(savedAllocation.getCreatedBy()).isEqualTo(OWNER_ID);
        assertThat(savedAllocation.getUpdatedBy()).isEqualTo(OWNER_ID);
        assertThat(savedAllocation.getCreatedAt()).isNotNull();
        assertThat(savedAllocation.getUpdatedAt()).isNotNull();
    }

    private Set<String> columns(String tableName) {
        return new TreeSet<>(jdbcTemplate.queryForList("""
                SELECT UPPER(column_name)
                FROM information_schema.columns
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
}
