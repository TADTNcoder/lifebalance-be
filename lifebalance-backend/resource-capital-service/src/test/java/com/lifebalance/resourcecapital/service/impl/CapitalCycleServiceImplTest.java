package com.lifebalance.resourcecapital.service.impl;

import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.ActiveCapitalCycleAlreadyExistsException;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.CapitalCycleNotFoundException;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.CapitalCycleOverlapException;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.InvalidCapitalCycleStateException;
import com.lifebalance.resourcecapital.dto.CapitalCycleResponse;
import com.lifebalance.resourcecapital.dto.CloseCapitalCycleRequest;
import com.lifebalance.resourcecapital.dto.CreateCapitalCycleRequest;
import com.lifebalance.resourcecapital.dto.ReopenCapitalCycleRequest;
import com.lifebalance.resourcecapital.dto.UpdateCapitalCycleRequest;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalCycleRepository;
import com.lifebalance.resourcecapital.service.CapitalCycleBusinessValidator;
import com.lifebalance.resourcecapital.service.mapper.CapitalCycleMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapitalCycleServiceImplTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_OWNER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID CYCLE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID ACTIVE_CYCLE_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final Instant NOW = Instant.parse("2026-08-02T10:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    @Mock
    private CapitalCycleRepository capitalCycleRepository;

    @Mock
    private CapitalCycleBusinessValidator capitalCycleBusinessValidator;

    @Test
    void createCycleCreatesDraftCycleWhenPeriodDoesNotOverlap() {
        CreateCapitalCycleRequest request = createRequest(
                "August 1",
                "Daily resource cycle",
                CapitalCycleType.DAILY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 1)
        );
        when(capitalCycleRepository.existsOverlappingCycle(
                OWNER_ID,
                CapitalCycleType.DAILY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 1),
                null
        )).thenReturn(false);
        when(capitalCycleRepository.save(any(CapitalCycle.class))).thenAnswer(invocation -> {
            CapitalCycle cycle = invocation.getArgument(0);
            setField(cycle, "id", CYCLE_ID);
            return cycle;
        });

        CapitalCycleResponse response = createService().createCycle(OWNER_ID, request);

        ArgumentCaptor<CapitalCycle> cycleCaptor = ArgumentCaptor.forClass(CapitalCycle.class);
        verify(capitalCycleRepository).save(cycleCaptor.capture());
        assertThat(cycleCaptor.getValue().getOwnerId()).isEqualTo(OWNER_ID);
        assertThat(cycleCaptor.getValue().getStatus()).isEqualTo(CapitalCycleStatus.DRAFT);
        assertThat(response.getId()).isEqualTo(CYCLE_ID);
        assertThat(response.getStatus()).isEqualTo(CapitalCycleStatus.DRAFT);
        assertThat(response.getName()).isEqualTo("August 1");
    }

    @Test
    void createCycleRejectsOverlappingCycle() {
        CreateCapitalCycleRequest request = createRequest(
                "August week",
                null,
                CapitalCycleType.WEEKLY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 7)
        );
        when(capitalCycleRepository.existsOverlappingCycle(
                OWNER_ID,
                CapitalCycleType.WEEKLY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 7),
                null
        )).thenReturn(true);

        assertThatThrownBy(() -> createService().createCycle(OWNER_ID, request))
                .isInstanceOf(CapitalCycleOverlapException.class);
        verify(capitalCycleRepository, never()).save(any());
    }

    @Test
    void updateCycleUpdatesDraftCycleUsingRequestedTypeAndPeriodForOverlapCheck() throws Exception {
        CapitalCycle cycle = dailyCycle();
        setField(cycle, "id", CYCLE_ID);
        UpdateCapitalCycleRequest request = updateRequest(
                "August week",
                "Updated",
                CapitalCycleType.WEEKLY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 7)
        );
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));
        when(capitalCycleRepository.existsOverlappingCycle(
                OWNER_ID,
                CapitalCycleType.WEEKLY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 7),
                CYCLE_ID
        )).thenReturn(false);

        CapitalCycleResponse response = createService().updateCycle(OWNER_ID, CYCLE_ID, request);

        assertThat(cycle.getName()).isEqualTo("August week");
        assertThat(cycle.getDescription()).isEqualTo("Updated");
        assertThat(cycle.getType()).isEqualTo(CapitalCycleType.WEEKLY);
        assertThat(response.getType()).isEqualTo(CapitalCycleType.WEEKLY);
    }

    @Test
    void updateCycleAllowsReopenedCycle() throws Exception {
        CapitalCycle cycle = closedCycle();
        setField(cycle, "id", CYCLE_ID);
        cycle.reopen("Need correction", NOW.minusSeconds(60));
        UpdateCapitalCycleRequest request = updateRequest(
                "August 2",
                "Reopened update",
                CapitalCycleType.DAILY,
                LocalDate.of(2026, 8, 2),
                LocalDate.of(2026, 8, 2)
        );
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));

        CapitalCycleResponse response = createService().updateCycle(OWNER_ID, CYCLE_ID, request);

        assertThat(response.getStatus()).isEqualTo(CapitalCycleStatus.REOPENED);
        assertThat(response.getStartDate()).isEqualTo(LocalDate.of(2026, 8, 2));
    }

    @Test
    void updateCycleAllowsActiveCycleToUpdateNameAndDescriptionOnly() throws Exception {
        CapitalCycle cycle = dailyCycle();
        setField(cycle, "id", CYCLE_ID);
        cycle.activate(NOW.minusSeconds(60));
        UpdateCapitalCycleRequest request = updateRequest(
                "August 1 active update",
                "Active cycle description update",
                CapitalCycleType.DAILY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 1)
        );
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));

        CapitalCycleResponse response = createService().updateCycle(OWNER_ID, CYCLE_ID, request);

        assertThat(response.getStatus()).isEqualTo(CapitalCycleStatus.ACTIVE);
        assertThat(response.getName()).isEqualTo("August 1 active update");
        assertThat(response.getDescription()).isEqualTo("Active cycle description update");
        assertThat(response.getType()).isEqualTo(CapitalCycleType.DAILY);
        verify(capitalCycleRepository, never()).existsOverlappingCycle(any(), any(), any(), any(), any());
    }

    @Test
    void updateCycleRejectsActiveCycleStructuralChanges() throws Exception {
        CapitalCycle cycle = dailyCycle();
        setField(cycle, "id", CYCLE_ID);
        cycle.activate(NOW.minusSeconds(60));
        UpdateCapitalCycleRequest request = updateRequest(
                "August week",
                "Active cycle description update",
                CapitalCycleType.WEEKLY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 7)
        );
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));

        assertThatThrownBy(() -> createService().updateCycle(OWNER_ID, CYCLE_ID, request))
                .isInstanceOf(InvalidCapitalCycleStateException.class)
                .hasMessageContaining("ACTIVE")
                .hasMessageContaining("update structural information");
        assertThat(cycle.getType()).isEqualTo(CapitalCycleType.DAILY);
        assertThat(cycle.getStartDate()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(cycle.getEndDate()).isEqualTo(LocalDate.of(2026, 8, 1));
        verify(capitalCycleRepository, never()).existsOverlappingCycle(any(), any(), any(), any(), any());
    }

    @Test
    void updateCycleRejectsClosedCycle() throws Exception {
        CapitalCycle cycle = closedCycle();
        setField(cycle, "id", CYCLE_ID);
        UpdateCapitalCycleRequest request = dailyUpdateRequest();
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));

        assertThatThrownBy(() -> createService().updateCycle(OWNER_ID, CYCLE_ID, request))
                .isInstanceOf(InvalidCapitalCycleStateException.class)
                .hasMessageContaining("CLOSED")
                .hasMessageContaining("update information");
    }

    @Test
    void updateCycleRejectsOverlappingCycle() throws Exception {
        CapitalCycle cycle = dailyCycle();
        setField(cycle, "id", CYCLE_ID);
        UpdateCapitalCycleRequest request = dailyUpdateRequest();
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));
        when(capitalCycleRepository.existsOverlappingCycle(
                OWNER_ID,
                CapitalCycleType.DAILY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 1),
                CYCLE_ID
        )).thenReturn(true);

        assertThatThrownBy(() -> createService().updateCycle(OWNER_ID, CYCLE_ID, request))
                .isInstanceOf(CapitalCycleOverlapException.class);
    }

    @Test
    void activateCycleActivatesDraftCycleWhenNoSameTypeCycleIsActive() throws Exception {
        CapitalCycle cycle = dailyCycle();
        setField(cycle, "id", CYCLE_ID);
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));
        doAnswer(invocation -> {
            assertThat(cycle.getStatus()).isEqualTo(CapitalCycleStatus.DRAFT);
            return null;
        }).when(capitalCycleBusinessValidator)
                .validateActivationAllowed(OWNER_ID, CapitalCycleType.DAILY, CYCLE_ID);

        CapitalCycleResponse response = createService().activateCycle(OWNER_ID, CYCLE_ID);

        assertThat(response.getStatus()).isEqualTo(CapitalCycleStatus.ACTIVE);
        assertThat(response.getActivatedAt()).isEqualTo(NOW);
        verify(capitalCycleBusinessValidator).validateActivationAllowed(OWNER_ID, CapitalCycleType.DAILY, CYCLE_ID);
    }

    @Test
    void activateCycleActivatesReopenedCycle() throws Exception {
        CapitalCycle cycle = closedCycle();
        setField(cycle, "id", CYCLE_ID);
        cycle.reopen("Need correction", NOW.minusSeconds(60));
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));

        CapitalCycleResponse response = createService().activateCycle(OWNER_ID, CYCLE_ID);

        assertThat(response.getStatus()).isEqualTo(CapitalCycleStatus.ACTIVE);
        assertThat(response.getActivatedAt()).isEqualTo(NOW);
        verify(capitalCycleBusinessValidator).validateActivationAllowed(OWNER_ID, CapitalCycleType.DAILY, CYCLE_ID);
    }

    @Test
    void activateCycleRejectsAlreadyActiveCycle() throws Exception {
        CapitalCycle cycle = dailyCycle();
        setField(cycle, "id", CYCLE_ID);
        cycle.activate(NOW.minusSeconds(60));
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));

        assertThatThrownBy(() -> createService().activateCycle(OWNER_ID, CYCLE_ID))
                .isInstanceOf(InvalidCapitalCycleStateException.class)
                .hasMessageContaining("ACTIVE")
                .hasMessageContaining("activate");
        verify(capitalCycleBusinessValidator, never()).validateActivationAllowed(any(), any(), any());
    }

    @Test
    void activateCycleAllowsActiveCycleWithDifferentType() throws Exception {
        CapitalCycle cycle = dailyCycle();
        setField(cycle, "id", CYCLE_ID);
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));

        CapitalCycleResponse response = createService().activateCycle(OWNER_ID, CYCLE_ID);

        assertThat(response.getStatus()).isEqualTo(CapitalCycleStatus.ACTIVE);
        verify(capitalCycleBusinessValidator).validateActivationAllowed(OWNER_ID, CapitalCycleType.DAILY, CYCLE_ID);
    }

    @Test
    void activateCycleRejectsActiveCycleWithSameOwnerAndType() throws Exception {
        CapitalCycle cycle = dailyCycle();
        setField(cycle, "id", CYCLE_ID);
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));
        doThrow(new ActiveCapitalCycleAlreadyExistsException(OWNER_ID, CapitalCycleType.DAILY))
                .when(capitalCycleBusinessValidator)
                .validateActivationAllowed(OWNER_ID, CapitalCycleType.DAILY, CYCLE_ID);

        assertThatThrownBy(() -> createService().activateCycle(OWNER_ID, CYCLE_ID))
                .isInstanceOf(ActiveCapitalCycleAlreadyExistsException.class);
        assertThat(cycle.getStatus()).isEqualTo(CapitalCycleStatus.DRAFT);
    }

    @Test
    void activateCycleConvertsDatabaseActiveCycleUniqueViolationToConflict() throws Exception {
        CapitalCycle cycle = dailyCycle();
        setField(cycle, "id", CYCLE_ID);
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));
        doThrow(new DataIntegrityViolationException(
                "duplicate key value violates unique constraint \"uq_capital_cycles_owner_type_active\""
        )).when(capitalCycleRepository).saveAndFlush(cycle);

        assertThatThrownBy(() -> createService().activateCycle(OWNER_ID, CYCLE_ID))
                .isInstanceOf(ActiveCapitalCycleAlreadyExistsException.class)
                .satisfies(exception -> {
                    ActiveCapitalCycleAlreadyExistsException appException =
                            (ActiveCapitalCycleAlreadyExistsException) exception;
                    assertThat(appException.getCode()).isEqualTo(ActiveCapitalCycleAlreadyExistsException.ERROR_CODE);
                });
    }

    @Test
    void closeCycleClosesActiveCycleWithReasonAndTimestamp() throws Exception {
        CapitalCycle cycle = dailyCycle();
        setField(cycle, "id", CYCLE_ID);
        cycle.activate(NOW.minusSeconds(60));
        CloseCapitalCycleRequest request = closeRequest("Finished");
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));

        CapitalCycleResponse response = createService().closeCycle(OWNER_ID, CYCLE_ID, request);

        assertThat(response.getStatus()).isEqualTo(CapitalCycleStatus.CLOSED);
        assertThat(response.getCloseReason()).isEqualTo("Finished");
        assertThat(response.getClosedAt()).isEqualTo(NOW);
    }

    @Test
    void closeCycleRejectsDraftCycle() throws Exception {
        CapitalCycle cycle = dailyCycle();
        setField(cycle, "id", CYCLE_ID);
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));

        assertThatThrownBy(() -> createService().closeCycle(OWNER_ID, CYCLE_ID, closeRequest("Finished")))
                .isInstanceOf(InvalidCapitalCycleStateException.class)
                .hasMessageContaining("DRAFT")
                .hasMessageContaining("close");
    }

    @Test
    void closeCycleClosesReopenedCycleWithReasonAndTimestamp() throws Exception {
        CapitalCycle cycle = closedCycle();
        setField(cycle, "id", CYCLE_ID);
        cycle.reopen("Need correction", NOW.minusSeconds(60));
        CloseCapitalCycleRequest request = closeRequest("Finished after correction");
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));

        CapitalCycleResponse response = createService().closeCycle(OWNER_ID, CYCLE_ID, request);

        assertThat(response.getStatus()).isEqualTo(CapitalCycleStatus.CLOSED);
        assertThat(response.getCloseReason()).isEqualTo("Finished after correction");
        assertThat(response.getClosedAt()).isEqualTo(NOW);
    }

    @Test
    void reopenCycleTransitionsClosedCycleToReopenedOnly() throws Exception {
        CapitalCycle cycle = closedCycle();
        setField(cycle, "id", CYCLE_ID);
        Instant closedAt = cycle.getClosedAt();
        ReopenCapitalCycleRequest request = reopenRequest("Need correction");
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));

        CapitalCycleResponse response = createService().reopenCycle(OWNER_ID, CYCLE_ID, request);

        assertThat(response.getStatus()).isEqualTo(CapitalCycleStatus.REOPENED);
        assertThat(response.getStatus()).isNotEqualTo(CapitalCycleStatus.ACTIVE);
        assertThat(response.getClosedAt()).isEqualTo(closedAt);
        assertThat(response.getReopenedAt()).isEqualTo(NOW);
        assertThat(response.getReopenReason()).isEqualTo("Need correction");
    }

    @Test
    void reopenCycleRejectsActiveCycle() throws Exception {
        CapitalCycle cycle = dailyCycle();
        setField(cycle, "id", CYCLE_ID);
        cycle.activate(NOW.minusSeconds(60));
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OWNER_ID)).thenReturn(Optional.of(cycle));

        assertThatThrownBy(() -> createService().reopenCycle(OWNER_ID, CYCLE_ID, reopenRequest("Need correction")))
                .isInstanceOf(InvalidCapitalCycleStateException.class)
                .hasMessageContaining("ACTIVE")
                .hasMessageContaining("reopen");
    }

    @Test
    void ownerCannotOperateAnotherOwnersCycle() {
        when(capitalCycleRepository.findByIdAndOwnerId(CYCLE_ID, OTHER_OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createService().activateCycle(OTHER_OWNER_ID, CYCLE_ID))
                .isInstanceOf(CapitalCycleNotFoundException.class)
                .hasMessageContaining(CYCLE_ID.toString())
                .hasMessageContaining(OTHER_OWNER_ID.toString());
        verify(capitalCycleBusinessValidator, never()).validateActivationAllowed(any(), any(), any());
    }

    private CapitalCycleServiceImpl createService() {
        return new CapitalCycleServiceImpl(
                capitalCycleRepository,
                new CapitalCycleMapper(),
                capitalCycleBusinessValidator,
                CLOCK
        );
    }

    private static CapitalCycle dailyCycle() {
        return CapitalCycle.create(
                OWNER_ID,
                "August 1",
                "Daily resource cycle",
                CapitalCycleType.DAILY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 1)
        );
    }

    private static CapitalCycle closedCycle() {
        CapitalCycle cycle = dailyCycle();
        cycle.activate(NOW.minusSeconds(120));
        cycle.close("Finished", NOW.minusSeconds(90));
        return cycle;
    }

    private static CreateCapitalCycleRequest createRequest(
            String name,
            String description,
            CapitalCycleType type,
            LocalDate startDate,
            LocalDate endDate
    ) {
        CreateCapitalCycleRequest request = new CreateCapitalCycleRequest();
        request.setName(name);
        request.setDescription(description);
        request.setType(type);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        return request;
    }

    private static UpdateCapitalCycleRequest dailyUpdateRequest() {
        return updateRequest(
                "August 1 updated",
                "Daily resource cycle updated",
                CapitalCycleType.DAILY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 1)
        );
    }

    private static UpdateCapitalCycleRequest updateRequest(
            String name,
            String description,
            CapitalCycleType type,
            LocalDate startDate,
            LocalDate endDate
    ) {
        UpdateCapitalCycleRequest request = new UpdateCapitalCycleRequest();
        request.setName(name);
        request.setDescription(description);
        request.setType(type);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        return request;
    }

    private static CloseCapitalCycleRequest closeRequest(String reason) {
        CloseCapitalCycleRequest request = new CloseCapitalCycleRequest();
        request.setReason(reason);
        return request;
    }

    private static ReopenCapitalCycleRequest reopenRequest(String reason) {
        ReopenCapitalCycleRequest request = new ReopenCapitalCycleRequest();
        request.setReason(reason);
        return request;
    }

    private static void setField(CapitalCycle cycle, String fieldName, Object value) throws Exception {
        Field field = CapitalCycle.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(cycle, value);
    }
}
