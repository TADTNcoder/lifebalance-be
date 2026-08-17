package com.lifebalance.resourcecapital.service.mapper;

import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.moneycapital.MoneyCapital;
import com.lifebalance.resourcecapital.domain.timecapital.TimeCapital;
import com.lifebalance.resourcecapital.dto.CapitalOverviewResponse;
import com.lifebalance.resourcecapital.dto.MoneyCapitalResponse;
import com.lifebalance.resourcecapital.dto.TimeCapitalResponse;
import com.lifebalance.resourcecapital.service.CapitalAllocationReader;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Component
public class CapitalMapper {

    private static final int MONEY_SCALE = 4;

    private final CapitalAllocationReader capitalAllocationReader;

    public CapitalMapper(CapitalAllocationReader capitalAllocationReader) {
        this.capitalAllocationReader = capitalAllocationReader;
    }

    public TimeCapitalResponse toTimeResponse(TimeCapital timeCapital) {
        UUID cycleId = timeCapital.getCapitalCycle().getId();
        UUID ownerId = timeCapital.getCapitalCycle().getOwnerId();
        long plannedMinutes = timeCapital.getPlannedMinutes();
        long allocatedMinutes = capitalAllocationReader.getAllocatedMinutes(ownerId, cycleId);
        long remainingMinutes = plannedMinutes - allocatedMinutes;

        return new TimeCapitalResponse(
                timeCapital.getId(),
                cycleId,
                plannedMinutes,
                allocatedMinutes,
                remainingMinutes,
                remainingMinutes,
                true
        );
    }

    public TimeCapitalResponse uninitializedTimeResponse(UUID cycleId) {
        return new TimeCapitalResponse(
                null,
                cycleId,
                null,
                null,
                null,
                null,
                false
        );
    }

    public MoneyCapitalResponse toMoneyResponse(MoneyCapital moneyCapital) {
        UUID cycleId = moneyCapital.getCapitalCycle().getId();
        UUID ownerId = moneyCapital.getCapitalCycle().getOwnerId();
        BigDecimal plannedAmount = moneyCapital.getPlannedAmount();
        BigDecimal allocatedAmount = capitalAllocationReader.getAllocatedAmount(ownerId, cycleId);
        if (allocatedAmount == null) {
            allocatedAmount = zeroMoney();
        }
        BigDecimal remainingAmount = plannedAmount.subtract(allocatedAmount).setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);

        return new MoneyCapitalResponse(
                moneyCapital.getId(),
                cycleId,
                plannedAmount,
                allocatedAmount,
                remainingAmount,
                remainingAmount,
                moneyCapital.getCurrencyCode(),
                true
        );
    }

    public MoneyCapitalResponse uninitializedMoneyResponse(UUID cycleId) {
        return new MoneyCapitalResponse(
                null,
                cycleId,
                null,
                null,
                null,
                null,
                null,
                false
        );
    }

    public CapitalOverviewResponse toOverview(
            CapitalCycle cycle,
            TimeCapitalResponse timeCapital,
            MoneyCapitalResponse moneyCapital
    ) {
        return new CapitalOverviewResponse(
                cycle.getId(),
                cycle.getStatus(),
                timeCapital,
                moneyCapital
        );
    }

    private BigDecimal zeroMoney() {
        return BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
    }
}
