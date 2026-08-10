package com.lifebalance.resourcecapital.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload used to reopen a closed capital cycle")
public class ReopenCapitalCycleRequest {

    @Schema(
            description = "Business reason required by the current API contract for reopening a capital cycle.",
            example = "Need correction",
            maxLength = 1000
    )
    @NotBlank
    @Size(max = 1000)
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
