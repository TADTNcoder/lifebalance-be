package com.lifebalance.resourcecapital.service.mapper;

import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.dto.CapitalCycleResponse;
import org.springframework.stereotype.Component;

@Component
public class CapitalCycleMapper {

    public CapitalCycleResponse toResponse(CapitalCycle cycle) {
        CapitalCycleResponse response = new CapitalCycleResponse();

        response.setId(cycle.getId());
        response.setName(cycle.getName());
        response.setDescription(cycle.getDescription());
        response.setType(cycle.getType());
        response.setStartDate(cycle.getStartDate());
        response.setEndDate(cycle.getEndDate());
        response.setStatus(cycle.getStatus());
        response.setOverAllocationAllowed(cycle.isOverAllocationAllowed());
        response.setActivatedAt(cycle.getActivatedAt());
        response.setClosedAt(cycle.getClosedAt());
        response.setReopenedAt(cycle.getReopenedAt());
        response.setCloseReason(cycle.getCloseReason());
        response.setReopenReason(cycle.getReopenReason());
        response.setCreatedAt(cycle.getCreatedAt());
        response.setUpdatedAt(cycle.getUpdatedAt());

        return response;
    }
}
