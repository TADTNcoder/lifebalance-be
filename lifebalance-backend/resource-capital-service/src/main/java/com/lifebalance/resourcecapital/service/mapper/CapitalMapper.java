package com.lifebalance.resourcecapital.service.mapper;

import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.moneycapital.MoneyCapital;
import com.lifebalance.resourcecapital.domain.timecapital.TimeCapital;
import com.lifebalance.resourcecapital.dto.CapitalOverviewResponse;
import com.lifebalance.resourcecapital.dto.MoneyCapitalResponse;
import com.lifebalance.resourcecapital.dto.TimeCapitalResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Component
public class CapitalMapper {

    private static final int MONEY_SCALE = 4;

    public TimeCapitalResponse toTimeResponse(TimeCapital timeCapital) {
        UUID cycleId = timeCapital.getCapitalCycle().getId();
        long plannedMinutes = timeCapital.getPlannedMinutes();
        long allocatedMinutes = 0L;

        return new TimeCapitalResponse(
                timeCapital.getId(),
                cycleId,
                plannedMinutes,
                allocatedMinutes,
                plannedMinutes - allocatedMinutes,
                plannedMinutes - allocatedMinutes,
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
        BigDecimal plannedAmount = moneyCapital.getPlannedAmount();
        BigDecimal allocatedAmount = zeroMoney();

        return new MoneyCapitalResponse(
                moneyCapital.getId(),
                cycleId,
                plannedAmount,
                allocatedAmount,
                plannedAmount.subtract(allocatedAmount).setScale(MONEY_SCALE, RoundingMode.UNNECESSARY),
                plannedAmount.subtract(allocatedAmount).setScale(MONEY_SCALE, RoundingMode.UNNECESSARY),
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
