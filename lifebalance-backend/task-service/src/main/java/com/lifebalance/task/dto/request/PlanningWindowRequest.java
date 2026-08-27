package com.lifebalance.task.dto.request;

import java.time.OffsetDateTime;

public interface PlanningWindowRequest {

    OffsetDateTime getPlannedStartAt();

    OffsetDateTime getPlannedEndAt();
}
