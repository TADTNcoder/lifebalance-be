package com.lifebalance.resourcecapital.service;

import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.ActiveCapitalCycleAlreadyExistsException;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalCycleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapitalCycleBusinessValidatorTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CURRENT_CYCLE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID OTHER_CYCLE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final Instant NOW = Instant.parse("2026-08-02T10:00:00Z");

    @Mock
    private CapitalCycleRepository capitalCycleRepository;

    @Test
    void validateActivationAllowedPassesWhenNoCycleIsActiveForOwnerAndType() {
        CapitalCycle currentCycle = draftCycle(CURRENT_CYCLE_ID, CapitalCycleType.DAILY);
        when(capitalCycleRepository.findByOwnerIdAndTypeForUpdate(OWNER_ID, CapitalCycleType.DAILY))
                .thenReturn(List.of(currentCycle));

        assertThatCode(() -> createValidator().validateActivationAllowed(
                OWNER_ID,
                CapitalCycleType.DAILY,
                CURRENT_CYCLE_ID
        )).doesNotThrowAnyException();

        verify(capitalCycleRepository).findByOwnerIdAndTypeForUpdate(OWNER_ID, CapitalCycleType.DAILY);
    }

    @Test
    void validateActivationAllowedThrowsWhenAnotherCycleIsActiveForOwnerAndType() {
        CapitalCycle currentCycle = draftCycle(CURRENT_CYCLE_ID, CapitalCycleType.DAILY);
        CapitalCycle activeCycle = activeCycle(OTHER_CYCLE_ID, CapitalCycleType.DAILY);
        when(capitalCycleRepository.findByOwnerIdAndTypeForUpdate(OWNER_ID, CapitalCycleType.DAILY))
                .thenReturn(List.of(currentCycle, activeCycle));

        assertThatThrownBy(() -> createValidator().validateActivationAllowed(
                OWNER_ID,
                CapitalCycleType.DAILY,
                CURRENT_CYCLE_ID
        ))
                .isInstanceOf(ActiveCapitalCycleAlreadyExistsException.class)
                .satisfies(exception -> {
                    ActiveCapitalCycleAlreadyExistsException appException =
                            (ActiveCapitalCycleAlreadyExistsException) exception;
                    assertThat(appException.getCode())
                            .isEqualTo(ActiveCapitalCycleAlreadyExistsException.ERROR_CODE);
                    assertThat(appException.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                });
    }

    @Test
    void validateActivationAllowedIgnoresCurrentCycleWhenItIsAlreadyActive() {
        CapitalCycle currentCycle = activeCycle(CURRENT_CYCLE_ID, CapitalCycleType.DAILY);
        when(capitalCycleRepository.findByOwnerIdAndTypeForUpdate(OWNER_ID, CapitalCycleType.DAILY))
                .thenReturn(List.of(currentCycle));

        assertThatCode(() -> createValidator().validateActivationAllowed(
                OWNER_ID,
                CapitalCycleType.DAILY,
                CURRENT_CYCLE_ID
        )).doesNotThrowAnyException();
    }

    @Test
    void validateActivationAllowedUsesOwnerAndTypeScope() {
        CapitalCycle currentCycle = draftCycle(CURRENT_CYCLE_ID, CapitalCycleType.WEEKLY);
        when(capitalCycleRepository.findByOwnerIdAndTypeForUpdate(OWNER_ID, CapitalCycleType.WEEKLY))
                .thenReturn(List.of(currentCycle));

        assertThatCode(() -> createValidator().validateActivationAllowed(
                OWNER_ID,
                CapitalCycleType.WEEKLY,
                CURRENT_CYCLE_ID
        )).doesNotThrowAnyException();

        verify(capitalCycleRepository).findByOwnerIdAndTypeForUpdate(OWNER_ID, CapitalCycleType.WEEKLY);
    }

    private CapitalCycleBusinessValidator createValidator() {
        return new CapitalCycleBusinessValidator(capitalCycleRepository);
    }

    private static CapitalCycle draftCycle(UUID id, CapitalCycleType type) {
        CapitalCycle cycle = CapitalCycle.create(
                OWNER_ID,
                type + " cycle",
                "Resource cycle",
                type,
                startDate(type),
                endDate(type)
        );
        setField(cycle, "id", id);
        return cycle;
    }

    private static CapitalCycle activeCycle(UUID id, CapitalCycleType type) {
        CapitalCycle cycle = draftCycle(id, type);
        cycle.activate(NOW);
        return cycle;
    }

    private static LocalDate startDate(CapitalCycleType type) {
        return switch (type) {
            case DAILY -> LocalDate.of(2026, 8, 1);
            case WEEKLY -> LocalDate.of(2026, 8, 3);
            case MONTHLY -> LocalDate.of(2026, 8, 1);
        };
    }

    private static LocalDate endDate(CapitalCycleType type) {
        LocalDate startDate = startDate(type);
        return switch (type) {
            case DAILY -> startDate;
            case WEEKLY -> startDate.plusDays(6);
            case MONTHLY -> startDate.withDayOfMonth(startDate.lengthOfMonth());
        };
    }

    private static void setField(CapitalCycle cycle, String fieldName, Object value) {
        try {
            Field field = CapitalCycle.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(cycle, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
