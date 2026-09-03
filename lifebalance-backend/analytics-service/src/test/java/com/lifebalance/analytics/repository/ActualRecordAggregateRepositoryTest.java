package com.lifebalance.analytics.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.lifebalance.analytics.domain.ActualRecord;
import com.lifebalance.analytics.domain.ActualRecordType;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:actual_record_aggregates;MODE=PostgreSQL;DATABASE_TO_UPPER=false;INIT=CREATE SCHEMA IF NOT EXISTS analytics",
        "spring.datasource.driver-class-name=org.h2.Driver"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ActualRecordAggregateRepositoryTest {

    private static final UUID OWNER_ID = UUID.randomUUID();
    private static final UUID OTHER_OWNER_ID = UUID.randomUUID();
    private static final UUID TASK_ID = UUID.randomUUID();
    private static final UUID OTHER_TASK_ID = UUID.randomUUID();
    private static final UUID CAPITAL_CYCLE_ID = UUID.randomUUID();

    @Autowired
    private ActualRecordRepository actualRecordRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void aggregatesWithNullableFiltersWithoutBindingNullIntoIsNullExpressions() {
        persistRecord(OWNER_ID, TASK_ID, null, "2026-09-01", 30, "100.0000", "VND", true);
        persistRecord(OWNER_ID, TASK_ID, CAPITAL_CYCLE_ID, "2026-09-02", 45, "200.0000", "VND", true);
        persistRecord(OWNER_ID, OTHER_TASK_ID, null, "2026-09-02", 60, "300.0000", "USD", true);
        persistRecord(OTHER_OWNER_ID, TASK_ID, null, "2026-09-01", 999, "999.0000", "VND", true);
        persistRecord(OWNER_ID, TASK_ID, null, "2026-09-01", 500, "500.0000", "VND", false);
        entityManager.flush();
        entityManager.clear();

        Long minutes = actualRecordRepository.sumActualMinutes(
                OWNER_ID,
                TASK_ID,
                null,
                LocalDate.parse("2026-09-01"),
                LocalDate.parse("2026-09-02")
        );
        BigDecimal cost = actualRecordRepository.sumActualCost(
                OWNER_ID,
                null,
                null,
                "VND",
                LocalDate.parse("2026-09-01"),
                LocalDate.parse("2026-09-02")
        );

        assertThat(minutes).isEqualTo(75L);
        assertThat(cost).isEqualByComparingTo("300.0000");
    }

    @Test
    void appliesCapitalCycleFilterOnlyWhenItIsProvided() {
        persistRecord(OWNER_ID, TASK_ID, null, "2026-09-01", 30, "100.0000", "VND", true);
        persistRecord(OWNER_ID, TASK_ID, CAPITAL_CYCLE_ID, "2026-09-02", 45, "200.0000", "VND", true);
        entityManager.flush();
        entityManager.clear();

        assertThat(actualRecordRepository.sumActualMinutes(
                OWNER_ID,
                TASK_ID,
                CAPITAL_CYCLE_ID,
                null,
                null
        )).isEqualTo(45L);
        assertThat(actualRecordRepository.sumActualCost(
                OWNER_ID,
                TASK_ID,
                CAPITAL_CYCLE_ID,
                "VND",
                null,
                null
        )).isEqualByComparingTo("200.0000");
    }

    private void persistRecord(
            UUID ownerId,
            UUID taskId,
            UUID capitalCycleId,
            String actualDate,
            int actualMinutes,
            String actualCost,
            String currencyCode,
            boolean active
    ) {
        ActualRecord record = ActualRecord.create(
                ownerId,
                ownerId,
                ActualRecordType.TIME_AND_MONEY,
                taskId,
                capitalCycleId,
                null,
                null,
                actualMinutes,
                new BigDecimal(actualCost),
                currencyCode,
                LocalDate.parse(actualDate),
                null,
                "test"
        );
        if (!active) {
            record.archive(ownerId);
        }
        entityManager.persist(record);
    }
}
