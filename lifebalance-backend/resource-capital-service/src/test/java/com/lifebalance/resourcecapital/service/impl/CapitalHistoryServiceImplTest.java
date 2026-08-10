package com.lifebalance.resourcecapital.service.impl;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.CapitalCycleNotFoundException;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActionType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActorType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalHistory;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalReferenceType;
import com.lifebalance.resourcecapital.domain.capitalhistory.exception.InvalidCapitalHistoryFilterException;
import com.lifebalance.resourcecapital.dto.CapitalHistoryResponse;
import com.lifebalance.resourcecapital.dto.CapitalHistoryResponseDTO;
import com.lifebalance.resourcecapital.dto.HistoryFilterRequest;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalCycleRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapitalHistoryServiceImplTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_OWNER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID CYCLE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID HISTORY_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID TASK_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final Instant CREATED_AT = Instant.parse("2026-08-01T00:00:00Z");

    @Mock
    private CapitalCycleRepository capitalCycleRepository;

    @Mock
    private CapitalHistoryRepository capitalHistoryRepository;

    @Test
    void getHistorySearchesAcrossOwnedCyclesWhenCycleFilterIsMissing() {
        CapitalCycle cycle = draftCycle();
        CapitalHistory history = history(cycle);
        when(capitalHistoryRepository.findAll(
                ArgumentMatchers.<Specification<CapitalHistory>>argThat(specification -> specification != null),
                any(Pageable.class)
        ))
                .thenReturn(new PageImpl<>(List.of(history)));

        Page<CapitalHistoryResponseDTO> response = createService().getHistory(
                OWNER_ID,
                null,
                new HistoryFilterRequest(
                        CapitalKind.TIME,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                PageRequest.of(0, 10)
        );

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).id()).isEqualTo(HISTORY_ID);
        assertThat(response.getContent().get(0).capitalCycleId()).isEqualTo(CYCLE_ID);
        verifyNoInteractions(capitalCycleRepository);
    }

    @Test
    void getHistoryByResourceChecksOwnershipAndMapsResponse() {
        CapitalCycle cycle = draftCycle();
        CapitalHistory history = history(cycle);
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));
        when(capitalHistoryRepository.findAll(
                ArgumentMatchers.<Specification<CapitalHistory>>argThat(specification -> specification != null),
                any(Pageable.class)
        ))
                .thenReturn(new PageImpl<>(List.of(history)));

        Page<CapitalHistoryResponse> response = createService().getHistoryByResource(
                OWNER_ID,
                CYCLE_ID,
                CapitalKind.TIME,
                PageRequest.of(0, 10)
        );

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(capitalHistoryRepository).findAll(
                ArgumentMatchers.<Specification<CapitalHistory>>argThat(specification -> specification != null),
                pageableCaptor.capture()
        );
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("createdAt"))
                .extracting(Sort.Order::getDirection)
                .isEqualTo(Sort.Direction.DESC);
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).id()).isEqualTo(HISTORY_ID);
        assertThat(response.getContent().get(0).capitalCycleId()).isEqualTo(CYCLE_ID);
        assertThat(response.getContent().get(0).capitalType()).isEqualTo(CapitalKind.TIME);
        assertThat(response.getContent().get(0).actionType()).isEqualTo(CapitalActionType.ALLOCATE);
        assertThat(response.getContent().get(0).referenceId()).isEqualTo(TASK_ID);
    }

    @Test
    void ownerCannotReadAnotherOwnersHistory() {
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OTHER_OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createService().getHistoryByResource(
                OTHER_OWNER_ID,
                CYCLE_ID,
                CapitalKind.TIME,
                PageRequest.of(0, 10)
        )).isInstanceOf(CapitalCycleNotFoundException.class);
        verifyNoInteractions(capitalHistoryRepository);
    }

    @Test
    void getHistoryRejectsInvalidDateRange() {
        HistoryFilterRequest filter = new HistoryFilterRequest(
                null,
                null,
                CREATED_AT,
                CREATED_AT,
                null,
                null,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> createService().getHistory(
                OWNER_ID,
                null,
                filter,
                PageRequest.of(0, 10)
        )).isInstanceOf(InvalidCapitalHistoryFilterException.class)
                .hasMessageContaining("fromDate");
        verifyNoInteractions(capitalHistoryRepository);
    }

    private CapitalHistoryServiceImpl createService() {
        return new CapitalHistoryServiceImpl(capitalCycleRepository, capitalHistoryRepository);
    }

    private static CapitalHistory history(CapitalCycle cycle) {
        CapitalHistory history = CapitalHistory.recordAt(
                cycle,
                CapitalKind.TIME,
                CapitalActionType.ALLOCATE,
                new BigDecimal("30.0000"),
                BigDecimal.ZERO.setScale(4),
                new BigDecimal("30.0000"),
                "Plan task",
                "Initial allocation",
                CapitalReferenceType.TASK,
                TASK_ID,
                CapitalActorType.USER,
                OWNER_ID,
                CREATED_AT
        );
        setField(history, "id", HISTORY_ID);
        return history;
    }

    private static CapitalCycle draftCycle() {
        CapitalCycle cycle = CapitalCycle.create(
                OWNER_ID,
                "August 1",
                "Daily resource cycle",
                CapitalCycleType.DAILY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 1)
        );
        setField(cycle, "id", CYCLE_ID);
        return cycle;
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to set test field " + fieldName, exception);
        }
    }
}
