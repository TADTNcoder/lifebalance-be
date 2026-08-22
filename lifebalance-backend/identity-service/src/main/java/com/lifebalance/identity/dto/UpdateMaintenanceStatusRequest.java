package com.lifebalance.identity.dto;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateMaintenanceStatusRequest {

    private Boolean maintenanceMode;

    private Boolean enabled;

    @Size(max = 5000)
    private String message;

    private OffsetDateTime startsAt;

    private OffsetDateTime endsAt;

    @Size(max = 1000)
    private String reason;

    private Boolean confirmed;
}
