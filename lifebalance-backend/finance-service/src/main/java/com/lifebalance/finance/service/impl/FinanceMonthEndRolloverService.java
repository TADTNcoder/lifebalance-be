package com.lifebalance.finance.service.impl;

import com.lifebalance.finance.domain.FinanceAccount;
import com.lifebalance.finance.repository.FinanceAccountRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FinanceMonthEndRolloverService {

    private static final Logger log = LoggerFactory.getLogger(FinanceMonthEndRolloverService.class);

    private final FinanceAccountRepository accountRepository;
    private final FinanceMonthEndJarSettlementWorker settlementWorker;

    public FinanceMonthEndRolloverService(
            FinanceAccountRepository accountRepository,
            FinanceMonthEndJarSettlementWorker settlementWorker
    ) {
        this.accountRepository = accountRepository;
        this.settlementWorker = settlementWorker;
    }

    public int settleExpiredJars(OffsetDateTime now) {
        OffsetDateTime currentMonthStart = FinanceAccountMonthPolicy
                .monthContaining(now)
                .startInclusive();
        List<FinanceAccount> candidates = accountRepository.findExpiredActiveJars(currentMonthStart);
        int settledCount = 0;

        for (FinanceAccount candidate : candidates) {
            try {
                if (settlementWorker.settleJar(
                        candidate.getId(),
                        candidate.getOwnerId(),
                        currentMonthStart,
                        now
                )) {
                    settledCount++;
                }
            } catch (RuntimeException exception) {
                // Each jar runs in REQUIRES_NEW, so a broken legacy jar cannot
                // roll back or starve the remaining month-end settlements.
                log.error(
                        "Failed to settle finance jar {} for owner {}; continuing with remaining jars",
                        candidate.getId(),
                        candidate.getOwnerId(),
                        exception
                );
            }
        }

        return settledCount;
    }
}
