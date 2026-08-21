package com.lifebalance.resourcecapital.integration;

import com.lifebalance.resourcecapital.dto.AllocationResponse;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class AfterCommitCapitalIntegrationPublisher implements CapitalIntegrationPublisher {

    private static final String IN_APP_CHANNEL = "IN_APP";

    private final CapitalIntegrationProperties properties;
    private final CapitalIntegrationClient client;

    public AfterCommitCapitalIntegrationPublisher(
            CapitalIntegrationProperties properties,
            CapitalIntegrationClient client
    ) {
        this.properties = properties;
        this.client = client;
    }

    @Override
    public void publishOverAllocationApproved(
            UUID actorId,
            AllocationResponse response,
            String action,
            String reason
    ) {
        if (actorId == null
                || response == null
                || !response.overAllocated()
                || !properties.isNotificationSyncEnabled()) {
            return;
        }
        String authorizationHeader = currentAuthorizationHeader();
        afterCommit(() -> client.createNotification(
                toCapitalAlert(response, action, reason),
                authorizationHeader
        ));
    }

    private CapitalNotificationRequest toCapitalAlert(
            AllocationResponse response,
            String action,
            String reason
    ) {
        String title = "Capital over allocation confirmed";
        String message = "Allocation for " + response.capitalType()
                + " target " + response.targetType()
                + " is over available capital. Remaining amount is "
                + response.remainingAmount() + ".";
        return new CapitalNotificationRequest(
                "CAPITAL_ALERT",
                Set.of(IN_APP_CHANNEL),
                "HIGH",
                title,
                message,
                "CAPITAL_ALLOCATION_TARGET",
                response.targetId(),
                "Warn user that a confirmed allocation leaves remaining capital negative.",
                true,
                null,
                integrationReason(action, reason)
        );
    }

    @Override
    public void publishAdjustmentOverAllocationApproved(
            UUID actorId,
            UUID cycleId,
            String capitalType,
            String action,
            BigDecimal requestedAmount,
            BigDecimal remainingAmount,
            String reason
    ) {
        if (actorId == null
                || cycleId == null
                || remainingAmount == null
                || remainingAmount.compareTo(BigDecimal.ZERO) >= 0
                || !properties.isNotificationSyncEnabled()) {
            return;
        }
        String authorizationHeader = currentAuthorizationHeader();
        afterCommit(() -> client.createNotification(
                toAdjustmentAlert(cycleId, capitalType, requestedAmount, remainingAmount, action, reason),
                authorizationHeader
        ));
    }

    private CapitalNotificationRequest toAdjustmentAlert(
            UUID cycleId,
            String capitalType,
            BigDecimal requestedAmount,
            BigDecimal remainingAmount,
            String action,
            String reason
    ) {
        String title = "Capital adjustment leaves negative remaining";
        String message = "Adjustment for " + capitalType
                + " capital was confirmed for amount " + requestedAmount
                + " even though remaining amount becomes "
                + remainingAmount + ".";
        return new CapitalNotificationRequest(
                "CAPITAL_ALERT",
                Set.of(IN_APP_CHANNEL),
                "HIGH",
                title,
                message,
                "CAPITAL_CYCLE",
                cycleId,
                "Warn user that a confirmed capital adjustment leaves remaining capital negative.",
                true,
                null,
                integrationReason(action, reason)
        );
    }

    private static String integrationReason(String action, String reason) {
        boolean hasAction = action != null && !action.isBlank();
        boolean hasReason = reason != null && !reason.isBlank();
        if (hasAction && hasReason) {
            return action + ": " + reason;
        }
        if (hasAction) {
            return action;
        }
        return hasReason ? reason : null;
    }

    private static void afterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }

    private static String currentAuthorizationHeader() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return null;
        }
        return servletRequestAttributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
    }
}
