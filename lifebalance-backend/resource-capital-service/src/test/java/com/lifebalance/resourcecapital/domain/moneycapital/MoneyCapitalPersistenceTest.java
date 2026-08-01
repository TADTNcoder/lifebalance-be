package com.lifebalance.resourcecapital.domain.moneycapital;

import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "eureka.client.enabled=false"
})
class MoneyCapitalPersistenceTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsMoneyCapitalWithCycleRelationAndDefaults() {
        CapitalCycle cycle = createDailyCycle("August 1", LocalDate.of(2026, 8, 1));
        entityManager.persist(cycle);

        MoneyCapital moneyCapital = MoneyCapital.create(cycle, new BigDecimal("123456.7890"), "vnd");
        entityManager.persist(moneyCapital);
        entityManager.flush();
        UUID moneyCapitalId = moneyCapital.getId();
        UUID cycleId = cycle.getId();

        assertThat(moneyCapitalId).isNotNull();
        assertThat(moneyCapital.getCreatedAt()).isNotNull();
        assertThat(moneyCapital.getUpdatedAt()).isNotNull();
        assertThat(moneyCapital.getVersion()).isNotNull();

        entityManager.clear();

        MoneyCapital found = entityManager.find(MoneyCapital.class, moneyCapitalId);

        assertThat(found).isNotNull();
        assertThat(found.getPlannedAmount()).isEqualByComparingTo("123456.7890");
        assertThat(found.getPlannedAmount().scale()).isEqualTo(4);
        assertThat(found.getCurrencyCode()).isEqualTo("VND");
        assertThat(found.getCapitalCycle().getId()).isEqualTo(cycleId);
        assertThat(found.hasCapital()).isTrue();
    }

    @Test
    void rejectsMoreThanOneMoneyCapitalForSameCycle() {
        CapitalCycle cycle = createDailyCycle("August 2", LocalDate.of(2026, 8, 2));
        entityManager.persist(cycle);

        MoneyCapital first = MoneyCapital.create(cycle, new BigDecimal("100.0000"), "VND");
        entityManager.persist(first);
        entityManager.flush();

        MoneyCapital second = MoneyCapital.create(cycle, new BigDecimal("200.0000"), "VND");
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
