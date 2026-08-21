package com.lifebalance.notification.dto;

import jakarta.validation.constraints.Size;

public record MarkDeliverySentRequest(
        @Size(max = 200) String providerMessageId
) {
}
