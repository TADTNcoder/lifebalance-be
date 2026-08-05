package com.lifebalance.resourcecapital.service;

import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;

import java.util.UUID;

public interface AllocationTargetValidator {

    void validateTarget(UUID ownerId, AllocationTargetType targetType, UUID targetId);
}
