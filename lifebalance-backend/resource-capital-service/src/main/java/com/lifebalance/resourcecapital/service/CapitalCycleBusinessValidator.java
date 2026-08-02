package com.lifebalance.resourcecapital.service;

import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.ActiveCapitalCycleAlreadyExistsException;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalCycleRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
public class CapitalCycleBusinessValidator {

    private final CapitalCycleRepository capitalCycleRepository;

    public CapitalCycleBusinessValidator(CapitalCycleRepository capitalCycleRepository) {
        this.capitalCycleRepository = capitalCycleRepository;
    }

    public void validateActivationAllowed(UUID ownerId, CapitalCycleType type, UUID currentCycleId) {
        boolean anotherActiveCycleExists = capitalCycleRepository.findByOwnerIdAndTypeForUpdate(ownerId, type)
                .stream()
                .anyMatch(cycle -> cycle.getStatus() == CapitalCycleStatus.ACTIVE
                        && !Objects.equals(cycle.getId(), currentCycleId));

        if (anotherActiveCycleExists) {
            throw new ActiveCapitalCycleAlreadyExistsException(ownerId, type);
        }
    }
}
