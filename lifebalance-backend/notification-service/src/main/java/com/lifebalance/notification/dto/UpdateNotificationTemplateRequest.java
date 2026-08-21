package com.lifebalance.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateNotificationTemplateRequest(
        @NotBlank @Size(max = 200) String titleTemplate,
        @NotBlank @Size(max = 2000) String messageTemplate,
        Boolean enabled
) {
}
