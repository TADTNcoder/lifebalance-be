package com.lifebalance.resourcecapital.service.mapper;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capital.exception.CapitalAllocationDataIntegrityException;
import com.lifebalance.resourcecapital.dto.AvailableCapitalResponse;
import com.lifebalance.resourcecapital.dto.AvailableMoneyCapitalResponse;
import com.lifebalance.resourcecapital.dto.AvailableTimeCapitalResponse;
import com.lifebalance.resourcecapital.dto.CapitalBalanceResponse;
import com.lifebalance.resourcecapital.dto.CapitalBalanceSummaryDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class AvailableCapitalMapper {

    public AvailableCapitalResponse toAvailableCapitalResponse(
            CapitalBalanceResponse balance,
            CapitalKind type
    ) {
        return new AvailableCapitalResponse(
                balance.cycleId(),
                shouldInclude(CapitalKind.TIME, type) ? toTimeCapital(balance.cycleId(), balance.timeCapital()) : null,
                shouldInclude(CapitalKind.MONEY, type) ? toMoneyCapital(balance.moneyCapital()) : null
        );
    }

    private boolean shouldInclude(CapitalKind capitalKind, CapitalKind requestedType) {
        return requestedType == null || requestedType == capitalKind;
    }

    private AvailableTimeCapitalResponse toTimeCapital(UUID cycleId, CapitalBalanceSummaryDto timeCapital) {
        return new AvailableTimeCapitalResponse(
                toWholeMinutes(cycleId, timeCapital.available()),
                timeCapital.initialized()
        );
    }

    private AvailableMoneyCapitalResponse toMoneyCapital(CapitalBalanceSummaryDto moneyCapital) {
        return new AvailableMoneyCapitalResponse(
                moneyCapital.available(),
                moneyCapital.currencyCode(),
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
