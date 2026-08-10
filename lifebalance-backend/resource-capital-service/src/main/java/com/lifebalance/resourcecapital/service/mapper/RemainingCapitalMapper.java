package com.lifebalance.resourcecapital.service.mapper;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capital.exception.CapitalAllocationDataIntegrityException;
import com.lifebalance.resourcecapital.dto.CapitalBalanceResponse;
import com.lifebalance.resourcecapital.dto.CapitalBalanceSummaryDto;
import com.lifebalance.resourcecapital.dto.RemainingCapitalResponse;
import com.lifebalance.resourcecapital.dto.RemainingMoneyCapitalResponse;
import com.lifebalance.resourcecapital.dto.RemainingTimeCapitalResponse;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class RemainingCapitalMapper {

    public RemainingCapitalResponse toRemainingCapitalResponse(
            CapitalBalanceResponse balance,
            CapitalKind type
    ) {
        return new RemainingCapitalResponse(
                balance.cycleId(),
                shouldInclude(CapitalKind.TIME, type) ? toTimeCapital(balance.cycleId(), balance.timeCapital()) : null,
                shouldInclude(CapitalKind.MONEY, type) ? toMoneyCapital(balance.moneyCapital()) : null
        );
    }

    private boolean shouldInclude(CapitalKind capitalKind, CapitalKind requestedType) {
        return requestedType == null || requestedType == capitalKind;
    }

    private RemainingTimeCapitalResponse toTimeCapital(UUID cycleId, CapitalBalanceSummaryDto timeCapital) {
        return new RemainingTimeCapitalResponse(
                toWholeMinutes(cycleId, timeCapital.total()),
                toWholeMinutes(cycleId, timeCapital.allocated()),
                toWholeMinutes(cycleId, timeCapital.remaining()),
                timeCapital.overAllocated(),
                timeCapital.initialized()
        );
    }

    private RemainingMoneyCapitalResponse toMoneyCapital(CapitalBalanceSummaryDto moneyCapital) {
        return new RemainingMoneyCapitalResponse(
                moneyCapital.total(),
                moneyCapital.allocated(),
                moneyCapital.remaining(),
                moneyCapital.currencyCode(),
                moneyCapital.overAllocated(),
                moneyCapital.initialized()
        );
    }

    private Long toWholeMinutes(UUID cycleId, BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        try {
            return amount.longValueExact();
        } catch (ArithmeticException exception) {
            throw new CapitalAllocationDataIntegrityException(cycleId, CapitalKind.TIME);
        }
    }
}
