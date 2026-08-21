package com.lifebalance.notification.dto;

import jakarta.validation.constraints.Size;

public record RetryDeliveryRequest(
        @Size(max = 1000) String reason
) {
}
