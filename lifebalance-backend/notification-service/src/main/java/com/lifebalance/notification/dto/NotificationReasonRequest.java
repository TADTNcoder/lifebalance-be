package com.lifebalance.notification.dto;

import jakarta.validation.constraints.Size;

public record NotificationReasonRequest(
        @Size(max = 1000) String reason
) {
}
