package com.lifebalance.notification.dto;

import jakarta.validation.constraints.Size;

public record MarkDeliveryFailedRequest(
        @Size(max = 1000) String errorMessage
) {
}
