package com.lifebalance.resourcecapital.integration;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;
import com.lifebalance.resourcecapital.dto.AllocationResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AfterCommitCapitalIntegrationPublisherTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CYCLE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID TARGET_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Mock
    private CapitalIntegrationClient client;

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void publishesAllocationAlertOnlyAfterCommitWhenPolicyApproved() {
        CapitalIntegrationProperties properties = approvedProperties();
        AfterCommitCapitalIntegrationPublisher publisher = new AfterCommitCapitalIntegrationPublisher(
                properties,
                client);
        TransactionSynchronizationManager.initSynchronization();

        publisher.publishOverAllocationApproved(
                OWNER_ID,
                allocationResponse(true, new BigDecimal("-15.0000")),
                "ALLOCATE",
                "Accepted shortfall");

        verifyNoInteractions(client);

        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCommit();
        }

        ArgumentCaptor<CapitalNotificationRequest> requestCaptor =
                ArgumentCaptor.forClass(CapitalNotificationRequest.class);
        verify(client).createNotification(requestCaptor.capture(), isNull());
        CapitalNotificationRequest request = requestCaptor.getValue();
        assertThat(request.eventType()).isEqualTo("CAPITAL_ALERT");
        assertThat(request.priority()).isEqualTo("HIGH");
        assertThat(request.referenceType()).isEqualTo("CAPITAL_ALLOCATION_TARGET");
        assertThat(request.referenceId()).isEqualTo(TARGET_ID);
        assertThat(request.policyApproved()).isTrue();
        assertThat(request.message()).contains("Remaining amount is -15.0000");
        assertThat(request.reason()).isEqualTo("ALLOCATE: Accepted shortfall");
    }

    @Test
    void publishesAdjustmentAlertOnlyWhenRemainingIsNegativeAndPolicyApproved() {
        CapitalIntegrationProperties properties = approvedProperties();
        AfterCommitCapitalIntegrationPublisher publisher = new AfterCommitCapitalIntegrationPublisher(
                properties,
                client);

        publisher.publishAdjustmentOverAllocationApproved(
                OWNER_ID,
                CYCLE_ID,
                CapitalKind.MONEY.name(),
                "ADJUSTMENT_DECREASE",
                new BigDecimal("40.0000"),
                new BigDecimal("-5.0000"),
                "Approved budget cut");

        ArgumentCaptor<CapitalNotificationRequest> requestCaptor =
                ArgumentCaptor.forClass(CapitalNotificationRequest.class);
        verify(client).createNotification(requestCaptor.capture(), isNull());
        CapitalNotificationRequest request = requestCaptor.getValue();
        assertThat(request.referenceType()).isEqualTo("CAPITAL_CYCLE");
        assertThat(request.referenceId()).isEqualTo(CYCLE_ID);
        assertThat(request.message()).contains("MONEY", "40.0000", "-5.0000");
        assertThat(request.reason()).isEqualTo("ADJUSTMENT_DECREASE: Approved budget cut");
    }

    @Test
    void skipsAlertsWhenPolicyIsNotApproved() {
        AfterCommitCapitalIntegrationPublisher publisher = new AfterCommitCapitalIntegrationPublisher(
                new CapitalIntegrationProperties(),
                client);

        publisher.publishOverAllocationApproved(
                OWNER_ID,
                allocationResponse(true, new BigDecimal("-15.0000")),
                "ALLOCATE",
                null);
        publisher.publishAdjustmentOverAllocationApproved(
                OWNER_ID,
                CYCLE_ID,
                CapitalKind.TIME.name(),
                "ADJUSTMENT_DECREASE",
                new BigDecimal("10.0000"),
                new BigDecimal("-1.0000"),
                null);

        verify(client, never()).createNotification(any(), any());
    }

    private static CapitalIntegrationProperties approvedProperties() {
        CapitalIntegrationProperties properties = new CapitalIntegrationProperties();
        properties.getNotificationService().setPolicyApproved(true);
        return properties;
    }

    private static AllocationResponse allocationResponse(boolean overAllocated, BigDecimal remainingAmount) {
        return new AllocationResponse(
                CYCLE_ID,
                CapitalKind.MONEY,
                AllocationTargetType.PROJECT,
                TARGET_ID,
                new BigDecimal("115.0000"),
                new BigDecimal("100.0000"),
                new BigDecimal("115.0000"),
                remainingAmount,
                overAllocated,
                List.of(UUID.fromString("44444444-4444-4444-4444-444444444444")));
    }
}
