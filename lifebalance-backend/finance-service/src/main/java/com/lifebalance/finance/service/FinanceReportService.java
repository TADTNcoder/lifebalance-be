package com.lifebalance.finance.service;

import com.lifebalance.finance.dto.FinanceSummaryResponse;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface FinanceReportService {

    FinanceSummaryResponse getSummary(UUID ownerId, String currencyCode, OffsetDateTime fromDate, OffsetDateTime toDate);
}
