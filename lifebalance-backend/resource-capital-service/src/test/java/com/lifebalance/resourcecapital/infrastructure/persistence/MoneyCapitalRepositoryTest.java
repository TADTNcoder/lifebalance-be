package com.lifebalance.resourcecapital.infrastructure.persistence;

import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import com.lifebalance.resourcecapital.domain.moneycapital.MoneyCapital;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.lang.reflect.Field;
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
class MoneyCapitalRepositoryTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private CapitalCycleRepository capitalCycleRepository;

    @Autowired
    private MoneyCapitalRepository moneyCapitalRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesAndFindsMoneyCapitalById() {
        CapitalCycle cycle = capitalCycleRepository.saveAndFlush(dailyCycle(
                "August 1",
                LocalDate.of(2026, 8, 1)
        ));
        MoneyCapital moneyCapital = moneyCapitalRepository.saveAndFlush(MoneyCapital.create(
                cycle,
                new BigDecimal("1250000.5000"),
                "vnd"
        ));
        entityManager.clear();

        assertThat(moneyCapitalRepository.findById(moneyCapital.getId()))
                .isPresent()
                .get()
                .satisfies(found -> {
                    assertThat(found.getCapitalCycle().getId()).isEqualTo(cycle.getId());
                    assertThat(found.getPlannedAmount()).isEqualByComparingTo("1250000.5000");
                    assertThat(found.getPlannedAmount().scale()).isEqualTo(4);
                    assertThat(found.getCurrencyCode()).isEqualTo("VND");
                    assertThat(found.getCreatedAt()).isNotNull();
                    assertThat(found.getUpdatedAt()).isNotNull();
                    assertThat(found.getVersion()).isNotNull();
                });
    }

    @Test
    void findsMoneyCapitalByCapitalCycleId() {
        CapitalCycle cycle = capitalCycleRepository.saveAndFlush(dailyCycle(
                "August 2",
                LocalDate.of(2026, 8, 2)
        ));
        MoneyCapital moneyCapital = moneyCapitalRepository.saveAndFlush(MoneyCapital.create(
                cycle,
                new BigDecimal("250000.0000"),
                "USD"
        ));
        entityManager.clear();

        assertThat(moneyCapitalRepository.findByCapitalCycleId(cycle.getId()))
                .isPresent()
                .get()
                .extracting(MoneyCapital::getId)
                .isEqualTo(moneyCapital.getId());
    }

    @Test
    void returnsEmptyWhenCapitalCycleHasNoMoneyCapital() {
        CapitalCycle cycle = capitalCycleRepository.saveAndFlush(dailyCycle(
                "August 3",
                LocalDate.of(2026, 8, 3)
        ));
        entityManager.clear();

        assertThat(moneyCapitalRepository.findByCapitalCycleId(cycle.getId())).isEmpty();
        assertThat(moneyCapitalRepository.findByCapitalCycleId(
                UUID.fromString("22222222-2222-2222-2222-222222222222")
        )).isEmpty();
    }

    @Test
    void detectsMoneyCapitalByCapitalCycleId() {
        CapitalCycle cycle = capitalCycleRepository.saveAndFlush(dailyCycle(
                "August 4",
                LocalDate.of(2026, 8, 4)
        ));
        CapitalCycle emptyCycle = capitalCycleRepository.saveAndFlush(dailyCycle(
                "August 5",
                LocalDate.of(2026, 8, 5)
        ));
        moneyCapitalRepository.saveAndFlush(MoneyCapital.create(
                cycle,
                new BigDecimal("500000.0000"),
                "VND"
        ));
        entityManager.clear();

        assertThat(moneyCapitalRepository.existsByCapitalCycleId(cycle.getId())).isTrue();
        assertThat(moneyCapitalRepository.existsByCapitalCycleId(emptyCycle.getId())).isFalse();
        assertThat(moneyCapitalRepository.existsByCapitalCycleId(
                UUID.fromString("33333333-3333-3333-3333-333333333333")
        )).isFalse();
    }

    @Test
    void rejectsMoreThanOneMoneyCapitalForSameCycle() {
        CapitalCycle cycle = capitalCycleRepository.saveAndFlush(dailyCycle(
                "August 6",
                LocalDate.of(2026, 8, 6)
        ));

        MoneyCapital first = MoneyCapital.create(cycle, new BigDecimal("100.0000"), "VND");
        MoneyCapital second = MoneyCapital.create(cycle, new BigDecimal("200.0000"), "VND");

        assertThatThrownBy(() -> {
            moneyCapitalRepository.save(first);
            moneyCapitalRepository.flush();

            moneyCapitalRepository.save(second);
            moneyCapitalRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void incrementsVersionWhenMoneyCapitalIsUpdated() throws Exception {
        CapitalCycle cycle = capitalCycleRepository.saveAndFlush(dailyCycle(
                "August 7",
                LocalDate.of(2026, 8, 7)
        ));
        MoneyCapital moneyCapital = moneyCapitalRepository.saveAndFlush(MoneyCapital.create(
                cycle,
                new BigDecimal("100.0000"),
                "VND"
        ));
        UUID moneyCapitalId = moneyCapital.getId();
        Long initialVersion = moneyCapital.getVersion();
        entityManager.clear();

        MoneyCapital found = moneyCapitalRepository.findById(moneyCapitalId).orElseThrow();
        setPlannedAmount(found, new BigDecimal("250.0000"));
        moneyCapitalRepository.flush();
        entityManager.clear();

        assertThat(moneyCapitalRepository.findById(moneyCapitalId))
                .isPresent()
                .get()
                .satisfies(updated -> {
                    assertThat(updated.getPlannedAmount()).isEqualByComparingTo("250.0000");
                    assertThat(updated.getVersion()).isGreaterThan(initialVersion);
                });
    }

    private CapitalCycle dailyCycle(String name, LocalDate date) {
        return CapitalCycle.create(
                OWNER_ID,
                name,
                "Daily resource cycle",
                CapitalCycleType.DAILY,
                date,
                date
        );
    }

    private void setPlannedAmount(MoneyCapital moneyCapital, BigDecimal plannedAmount) throws Exception {
        Field plannedAmountField = MoneyCapital.class.getDeclaredField("plannedAmount");
        plannedAmountField.setAccessible(true);
        plannedAmountField.set(moneyCapital, plannedAmount);
    }
}
