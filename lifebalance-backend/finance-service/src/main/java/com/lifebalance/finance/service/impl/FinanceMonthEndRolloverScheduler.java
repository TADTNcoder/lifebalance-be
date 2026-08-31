package com.lifebalance.finance.service.impl;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FinanceMonthEndRolloverScheduler {

    private static final Logger log = LoggerFactory.getLogger(FinanceMonthEndRolloverScheduler.class);
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Bangkok");

    private final FinanceMonthEndRolloverService rolloverService;

    public FinanceMonthEndRolloverScheduler(FinanceMonthEndRolloverService rolloverService) {
        this.rolloverService = rolloverService;
    }

    @Scheduled(
            cron = "${lifebalance.finance.month-end-rollover-cron:0 10 0 * * *}",
            zone = "Asia/Bangkok"
    )
    public void settleExpiredJars() {
        runSettlement("scheduled");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void settleMissedMonthsAfterStartup() {
        runSettlement("startup catch-up");
    }

    private void runSettlement(String trigger) {
        try {
            int settledCount = rolloverService.settleExpiredJars(OffsetDateTime.now(BUSINESS_ZONE));
            if (settledCount > 0) {
                log.info("Settled {} expired finance jars ({})", settledCount, trigger);
            }
        } catch (RuntimeException exception) {
            log.error("Unable to settle expired finance jars ({})", trigger, exception);
        }
    }
}
