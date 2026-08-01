package com.lifebalance.resourcecapital.domain.timecapital;

import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.InvalidCapitalCycleStateException;
import jakarta.persistence.Version;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimeCapitalTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final Instant NOW = Instant.parse("2026-08-01T00:00:00Z");

    @Test
    void createsTimeCapitalWithZeroPlannedMinutes() {
        CapitalCycle cycle = createDailyCycle();

        TimeCapital timeCapital = TimeCapital.create(cycle, 0);

        assertThat(timeCapital.getCapitalCycle()).isSameAs(cycle);
        assertThat(timeCapital.getPlannedMinutes()).isZero();
        assertThat(timeCapital.hasCapital()).isFalse();
    }

    @Test
    void createsTimeCapitalWithPositivePlannedMinutes() {
        CapitalCycle cycle = createDailyCycle();

        TimeCapital timeCapital = TimeCapital.create(cycle, 480);

        assertThat(timeCapital.getCapitalCycle()).isSameAs(cycle);
        assertThat(timeCapital.getPlannedMinutes()).isEqualTo(480);
        assertThat(timeCapital.hasCapital()).isTrue();
    }

    @Test
    void rejectsNullCapitalCycle() {
        assertThatThrownBy(() -> TimeCapital.create(null, 480))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Capital cycle");
    }

    @Test
    void rejectsNegativePlannedMinutes() {
        CapitalCycle cycle = createDailyCycle();

        assertThatThrownBy(() -> TimeCapital.create(cycle, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Planned minutes");
    }

    @Test
    void rejectsCycleThatCannotInitializeTimeCapital() {
        CapitalCycle cycle = createDailyCycle();
        cycle.activate(NOW);

        assertThatThrownBy(() -> TimeCapital.create(cycle, 480))
                .isInstanceOf(InvalidCapitalCycleStateException.class)
                .hasMessageContaining(CapitalCycleStatus.ACTIVE.name())
                .hasMessageContaining("initialize time capital");
    }

    @Test
    void versionFieldExists() throws NoSuchFieldException {
        Field versionField = TimeCapital.class.getDeclaredField("version");

        assertThat(versionField.getType()).isEqualTo(Long.class);
        assertThat(versionField.isAnnotationPresent(Version.class)).isTrue();
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
}
