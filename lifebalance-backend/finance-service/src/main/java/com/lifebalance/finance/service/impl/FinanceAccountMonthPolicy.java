package com.lifebalance.finance.service.impl;

import com.lifebalance.finance.domain.FinanceAccount;
import com.lifebalance.finance.domain.FinanceAccountType;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;

/**
 * Defines when a finance account can be used.
 *
 * <p>Finance timestamps are persisted in UTC, while the product currently uses the
 * Asia/Bangkok business timezone (UTC+7) for task and finance dates. The main pool
 * is a lifetime account; jars are effective only in their creation month.</p>
 */
final class FinanceAccountMonthPolicy {

    static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Bangkok");

    private FinanceAccountMonthPolicy() {
    }

    static MonthRange currentMonth() {
        return monthContaining(OffsetDateTime.now());
    }

    static MonthRange monthContaining(OffsetDateTime value) {
        YearMonth month = YearMonth.from(value.atZoneSameInstant(BUSINESS_ZONE));
        return monthRange(month, month);
    }

    static MonthRange monthsCovering(OffsetDateTime from, OffsetDateTime to) {
        YearMonth firstMonth = YearMonth.from(from.atZoneSameInstant(BUSINESS_ZONE));
        YearMonth lastMonth = YearMonth.from(to.atZoneSameInstant(BUSINESS_ZONE));
        return monthRange(firstMonth, lastMonth);
    }

    static boolean isEffectiveAt(FinanceAccount account, OffsetDateTime transactionDate) {
        if (account == null || account.getCreatedAt() == null || transactionDate == null) {
            return false;
        }

        if (account.getAccountType() == FinanceAccountType.MAIN_POOL) {
            return true;
        }

        YearMonth accountMonth = YearMonth.from(account.getCreatedAt().atZoneSameInstant(BUSINESS_ZONE));
        YearMonth transactionMonth = YearMonth.from(transactionDate.atZoneSameInstant(BUSINESS_ZONE));
        return accountMonth.equals(transactionMonth);
    }

    private static MonthRange monthRange(YearMonth firstMonth, YearMonth lastMonth) {
        OffsetDateTime startInclusive = firstMonth
                .atDay(1)
                .atStartOfDay(BUSINESS_ZONE)
                .toOffsetDateTime();
        OffsetDateTime endExclusive = lastMonth
                .plusMonths(1)
                .atDay(1)
                .atStartOfDay(BUSINESS_ZONE)
                .toOffsetDateTime();
        return new MonthRange(startInclusive, endExclusive);
    }

    record MonthRange(OffsetDateTime startInclusive, OffsetDateTime endExclusive) {
    }
}
