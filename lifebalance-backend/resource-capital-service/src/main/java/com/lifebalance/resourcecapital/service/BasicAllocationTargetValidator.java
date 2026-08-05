package com.lifebalance.resourcecapital.service;

import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.InvalidAllocationTargetException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BasicAllocationTargetValidator implements AllocationTargetValidator {

    @Override
    public void validateTarget(UUID ownerId, AllocationTargetType targetType, UUID targetId) {
        if (ownerId == null) {
            throw new InvalidAllocationTargetException("Allocation owner id is required.");
        }
        if (targetType == null) {
            throw new InvalidAllocationTargetException("Allocation target type is required.");
        }
        if (targetId == null) {
            throw new InvalidAllocationTargetException("Allocation target id is required.");
        }
        if (targetType != AllocationTargetType.TASK) {
            throw new InvalidAllocationTargetException("Only TASK allocation targets are supported.");
        }
        // Existence and ownership validation against task-service is intentionally deferred
        // to the task integration story; this port keeps the boundary explicit.
    }
}
