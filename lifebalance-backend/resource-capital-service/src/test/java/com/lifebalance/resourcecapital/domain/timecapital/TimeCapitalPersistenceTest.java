package com.lifebalance.resourcecapital.domain.timecapital;

import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "eureka.client.enabled=false"
})
class TimeCapitalPersistenceTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsTimeCapitalWithCycleRelationAndDefaults() {
        CapitalCycle cycle = createDailyCycle("August 1", LocalDate.of(2026, 8, 1));
        entityManager.persist(cycle);

        TimeCapital timeCapital = TimeCapital.create(cycle, 480);
        entityManager.persist(timeCapital);
        entityManager.flush();
        UUID timeCapitalId = timeCapital.getId();
        UUID cycleId = cycle.getId();

        assertThat(timeCapitalId).isNotNull();
        assertThat(timeCapital.getCreatedAt()).isNotNull();
        assertThat(timeCapital.getUpdatedAt()).isNotNull();
        assertThat(timeCapital.getVersion()).isNotNull();

        entityManager.clear();

        TimeCapital found = entityManager.find(TimeCapital.class, timeCapitalId);

        assertThat(found).isNotNull();
        assertThat(found.getPlannedMinutes()).isEqualTo(480);
        assertThat(found.getCapitalCycle().getId()).isEqualTo(cycleId);
        assertThat(found.hasCapital()).isTrue();
    }

    @Test
    void rejectsMoreThanOneTimeCapitalForSameCycle() {
        CapitalCycle cycle = createDailyCycle("August 2", LocalDate.of(2026, 8, 2));
        entityManager.persist(cycle);

        TimeCapital first = TimeCapital.create(cycle, 480);
        entityManager.persist(first);
        entityManager.flush();

        TimeCapital second = TimeCapital.create(cycle, 300);
        entityManager.persist(second);

        assertThatThrownBy(() -> entityManager.flush())
                .isInstanceOf(RuntimeException.class);
    }

    private CapitalCycle createDailyCycle(String name, LocalDate date) {
        return CapitalCycle.create(
                OWNER_ID,
                name,
                "Daily resource cycle",
                CapitalCycleType.DAILY,
                date,
                date
        );
    }
}
