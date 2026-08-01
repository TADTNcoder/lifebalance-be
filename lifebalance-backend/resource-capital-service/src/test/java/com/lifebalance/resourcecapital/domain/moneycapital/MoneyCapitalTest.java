package com.lifebalance.resourcecapital.domain.moneycapital;

import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.InvalidCapitalCycleStateException;
import jakarta.persistence.Version;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyCapitalTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final Instant NOW = Instant.parse("2026-08-01T00:00:00Z");

    @Test
    void createsMoneyCapitalWithZeroPlannedAmount() {
        CapitalCycle cycle = createDailyCycle();

        MoneyCapital moneyCapital = MoneyCapital.create(cycle, new BigDecimal("0"), "vnd");

        assertThat(moneyCapital.getCapitalCycle()).isSameAs(cycle);
        assertThat(moneyCapital.getPlannedAmount()).isEqualByComparingTo("0.0000");
        assertThat(moneyCapital.getPlannedAmount().scale()).isEqualTo(4);
        assertThat(moneyCapital.getCurrencyCode()).isEqualTo("VND");
        assertThat(moneyCapital.hasCapital()).isFalse();
    }

    @Test
    void createsMoneyCapitalWithPositivePlannedAmount() {
        CapitalCycle cycle = createDailyCycle();

        MoneyCapital moneyCapital = MoneyCapital.create(cycle, new BigDecimal("10000000.1200"), "usd");

        assertThat(moneyCapital.getCapitalCycle()).isSameAs(cycle);
        assertThat(moneyCapital.getPlannedAmount()).isEqualByComparingTo("10000000.1200");
        assertThat(moneyCapital.getCurrencyCode()).isEqualTo("USD");
        assertThat(moneyCapital.hasCapital()).isTrue();
    }

    @Test
    void rejectsNullCapitalCycle() {
        assertThatThrownBy(() -> MoneyCapital.create(null, new BigDecimal("100.0000"), "VND"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Capital cycle");
    }

    @Test
    void rejectsNullPlannedAmount() {
        CapitalCycle cycle = createDailyCycle();

        assertThatThrownBy(() -> MoneyCapital.create(cycle, null, "VND"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Planned amount");
    }

    @Test
    void rejectsNegativePlannedAmount() {
        CapitalCycle cycle = createDailyCycle();

        assertThatThrownBy(() -> MoneyCapital.create(cycle, new BigDecimal("-0.0001"), "VND"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("greater than or equal to zero");
    }

    @Test
    void rejectsPlannedAmountWithMoreThanFourDecimalPlaces() {
        CapitalCycle cycle = createDailyCycle();

        assertThatThrownBy(() -> MoneyCapital.create(cycle, new BigDecimal("100.00001"), "VND"))
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("Rounding necessary");
    }

    @Test
    void acceptsZeroWithFourDecimalPlacesAsNoCapital() {
        CapitalCycle cycle = createDailyCycle();

        MoneyCapital moneyCapital = MoneyCapital.create(cycle, new BigDecimal("0.0000"), "VND");

        assertThat(moneyCapital.hasCapital()).isFalse();
    }

    @Test
    void rejectsInvalidCurrencyCode() {
        CapitalCycle cycle = createDailyCycle();

        assertThatThrownBy(() -> MoneyCapital.create(cycle, new BigDecimal("100.0000"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Currency code");
        assertThatThrownBy(() -> MoneyCapital.create(cycle, new BigDecimal("100.0000"), " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Currency code");
        assertThatThrownBy(() -> MoneyCapital.create(cycle, new BigDecimal("100.0000"), "VN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Currency code");
        assertThatThrownBy(() -> MoneyCapital.create(cycle, new BigDecimal("100.0000"), "VN1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Currency code");
    }

    @Test
    void rejectsCycleThatCannotInitializeMoneyCapital() {
        CapitalCycle cycle = createDailyCycle();
        cycle.activate(NOW);

        assertThatThrownBy(() -> MoneyCapital.create(cycle, new BigDecimal("100.0000"), "VND"))
                .isInstanceOf(InvalidCapitalCycleStateException.class)
                .hasMessageContaining(CapitalCycleStatus.ACTIVE.name())
                .hasMessageContaining("initialize money capital");
    }

    @Test
    void versionFieldExists() throws NoSuchFieldException {
        Field versionField = MoneyCapital.class.getDeclaredField("version");

        assertThat(versionField.getType()).isEqualTo(Long.class);
        assertThat(versionField.isAnnotationPresent(Version.class)).isTrue();
    }

    @Test
    void equalityUsesPersistedIdOnly() throws Exception {
        MoneyCapital first = MoneyCapital.create(createDailyCycle(), new BigDecimal("100.0000"), "VND");
        MoneyCapital second = MoneyCapital.create(createDailyCycle(), new BigDecimal("100.0000"), "VND");
        UUID id = UUID.fromString("22222222-2222-2222-2222-222222222222");

        assertThat(first).isNotEqualTo(second);

        setId(first, id);
        setId(second, id);

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    private CapitalCycle createDailyCycle() {
        return CapitalCycle.create(
                OWNER_ID,
                "August 1",
                "Daily resource cycle",
                CapitalCycleType.DAILY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 1)
        );
    }

    private void setId(MoneyCapital moneyCapital, UUID id) throws Exception {
        Field idField = MoneyCapital.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(moneyCapital, id);
    }
}
