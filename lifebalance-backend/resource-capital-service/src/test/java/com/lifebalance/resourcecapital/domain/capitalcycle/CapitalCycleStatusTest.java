package com.lifebalance.resourcecapital.domain.capitalcycle;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CapitalCycleStatusTest {

    @Test
    void allowsOnlyApprovedCapitalCycleStatusTransitions() {
        assertThat(CapitalCycleStatus.DRAFT.canTransitionTo(CapitalCycleStatus.ACTIVE)).isTrue();
        assertThat(CapitalCycleStatus.ACTIVE.canTransitionTo(CapitalCycleStatus.CLOSED)).isTrue();
        assertThat(CapitalCycleStatus.CLOSED.canTransitionTo(CapitalCycleStatus.REOPENED)).isTrue();
        assertThat(CapitalCycleStatus.REOPENED.canTransitionTo(CapitalCycleStatus.ACTIVE)).isTrue();
        assertThat(CapitalCycleStatus.REOPENED.canTransitionTo(CapitalCycleStatus.CLOSED)).isTrue();
    }

    @Test
    void rejectsInvalidCapitalCycleStatusTransitions() {
        for (CapitalCycleStatus source : CapitalCycleStatus.values()) {
            for (CapitalCycleStatus target : CapitalCycleStatus.values()) {
                boolean expectedAllowed = (source == CapitalCycleStatus.DRAFT && target == CapitalCycleStatus.ACTIVE)
                        || (source == CapitalCycleStatus.ACTIVE && target == CapitalCycleStatus.CLOSED)
                        || (source == CapitalCycleStatus.CLOSED && target == CapitalCycleStatus.REOPENED)
                        || (source == CapitalCycleStatus.REOPENED && target == CapitalCycleStatus.ACTIVE)
                        || (source == CapitalCycleStatus.REOPENED && target == CapitalCycleStatus.CLOSED);

                assertThat(source.canTransitionTo(target))
                        .as("%s -> %s", source, target)
                        .isEqualTo(expectedAllowed);
            }
        }

        assertThat(CapitalCycleStatus.DRAFT.canTransitionTo(null)).isFalse();
    }
}
