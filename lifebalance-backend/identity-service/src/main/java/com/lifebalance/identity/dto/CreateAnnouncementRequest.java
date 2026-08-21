package com.lifebalance.identity.dto;

import java.time.OffsetDateTime;

import com.lifebalance.identity.model.enums.AnnouncementAudience;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAnnouncementRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    @Size(max = 5000)
    private String message;

    @NotNull
    private AnnouncementAudience audience;

    private OffsetDateTime startsAt;

    private OffsetDateTime endsAt;

    private Boolean publishNow;
}
