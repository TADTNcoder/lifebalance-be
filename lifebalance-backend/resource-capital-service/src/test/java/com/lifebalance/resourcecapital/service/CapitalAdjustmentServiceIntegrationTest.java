package com.lifebalance.resourcecapital.service;

import com.lifebalance.resourcecapital.domain.capital.CapitalAdjustmentType;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capital.exception.CapitalCycleNotAdjustableException;
import com.lifebalance.resourcecapital.domain.capital.exception.InvalidAdjustmentAmountException;
import com.lifebalance.resourcecapital.domain.capitaladjustment.CapitalAdjustment;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActionType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActorType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalHistory;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalReferenceType;
import com.lifebalance.resourcecapital.dto.AdjustCapitalRequestDTO;
import com.lifebalance.resourcecapital.dto.AdjustMoneyCapitalRequest;
import com.lifebalance.resourcecapital.dto.AdjustTimeCapitalRequest;
import com.lifebalance.resourcecapital.dto.CapitalAdjustmentResponseDTO;
import com.lifebalance.resourcecapital.dto.MoneyCapitalAdjustmentResponse;
import com.lifebalance.resourcecapital.dto.SetupMoneyCapitalRequest;
import com.lifebalance.resourcecapital.dto.SetupTimeCapitalRequest;
import com.lifebalance.resourcecapital.dto.TimeCapitalAdjustmentResponse;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalAdjustmentRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalCycleRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalHistoryRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.MoneyCapitalRepository;
import com.lifebalance.resourcecapital.infrastructure.persistence.TimeCapitalRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "eureka.client.enabled=false"
})
class CapitalAdjustmentServiceIntegrationTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final Instant NOW = Instant.parse("2026-08-04T10:00:00Z");

    @Autowired
    private CapitalAdjustmentService capitalAdjustmentService;

    @Autowired
    private CapitalService capitalService;

    @Autowired
    private CapitalCycleRepository capitalCycleRepository;

    @Autowired
    private TimeCapitalRepository timeCapitalRepository;

    @Autowired
    private MoneyCapitalRepository moneyCapitalRepository;

    @Autowired
    private CapitalHistoryRepository capitalHistoryRepository;

    @Autowired
    private CapitalAdjustmentRepository capitalAdjustmentRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void adjustTimeCapitalPersistsUpdatedCapitalAndManualHistory() {
        CapitalCycle cycle = capitalCycleRepository.saveAndFlush(dailyCycle("August 4", LocalDate.of(2026, 8, 4)));
        capitalService.setupTimeCapital(OWNER_ID, cycle.getId(), new SetupTimeCapitalRequest(120L));

        TimeCapitalAdjustmentResponse response = capitalAdjustmentService.adjustTimeCapital(
                OWNER_ID,
                cycle.getId(),
                new AdjustTimeCapitalRequest(CapitalAdjustmentType.INCREASE, 45L, "Extend focused work", false)
        );
        entityManager.flush();
        entityManager.clear();

        assertThat(response.afterMinutes()).isEqualTo(165L);
        assertThat(timeCapitalRepository.findByCapitalCycleId(cycle.getId()))
                .isPresent()
                .get()
                .satisfies(timeCapital -> assertThat(timeCapital.getPlannedMinutes()).isEqualTo(165L));
        assertThat(capitalAdjustmentRepository.findByUserIdAndCapitalCycleId(
                OWNER_ID,
                cycle.getId(),
                PageRequest.of(0, 10)
        ).getContent()).singleElement().satisfies(adjustment -> {
            assertThat(adjustment.getCapitalType()).isEqualTo(CapitalKind.TIME);
            assertThat(adjustment.getAdjustmentType()).isEqualTo(CapitalAdjustmentType.INCREASE);
            assertThat(adjustment.getAmountDelta()).isEqualByComparingTo("45.0000");
            assertThat(adjustment.getPreviousAmount()).isEqualByComparingTo("120.0000");
            assertThat(adjustment.getNewAmount()).isEqualByComparingTo("165.0000");
            assertThat(adjustment.getReason()).isEqualTo("Extend focused work");
        });
        assertThat(capitalHistoryRepository.findByCapitalCycleIdAndActionType(
                cycle.getId(),
                CapitalActionType.ADJUSTMENT_INCREASE,
                PageRequest.of(0, 10)
        ).getContent()).singleElement().satisfies(history -> {
            assertThat(history.getId()).isEqualTo(response.historyId());
            assertThat(history.getCapitalType()).isEqualTo(CapitalKind.TIME);
            assertThat(history.getAmount()).isEqualByComparingTo("45.0000");
            assertThat(history.getBeforeAmount()).isEqualByComparingTo("120.0000");
            assertThat(history.getAfterAmount()).isEqualByComparingTo("165.0000");
            assertThat(history.getReason()).isEqualTo("Extend focused work");
            assertThat(history.getReferenceType()).isEqualTo(CapitalReferenceType.MANUAL);
            assertThat(history.getReferenceId()).isNull();
            assertThat(history.getActorType()).isEqualTo(CapitalActorType.USER);
            assertThat(history.getActorId()).isEqualTo(OWNER_ID);
        });
    }

    @Test
    void adjustMoneyCapitalPersistsUpdatedCapitalAndManualHistoryWithCurrencyResponse() {
        CapitalCycle cycle = capitalCycleRepository.saveAndFlush(dailyCycle("August 5", LocalDate.of(2026, 8, 5)));
        capitalService.setupMoneyCapital(
                OWNER_ID,
                cycle.getId(),
                new SetupMoneyCapitalRequest(new BigDecimal("500.0000"), "usd")
        );

        MoneyCapitalAdjustmentResponse response = capitalAdjustmentService.adjustMoneyCapital(
                OWNER_ID,
                cycle.getId(),
                new AdjustMoneyCapitalRequest(
                        CapitalAdjustmentType.DECREASE,
                        new BigDecimal("125.2500"),
                        "Cancel purchase",
                        "usd",
                        false,
                        null // <-- Bổ sung null vào đây
                )
        );
        entityManager.flush();
        entityManager.clear();

        assertThat(response.currencyCode()).isEqualTo("USD");
        assertThat(response.afterAmount()).isEqualByComparingTo("374.7500");
        assertThat(moneyCapitalRepository.findByCapitalCycleId(cycle.getId()))
                .isPresent()
                .get()
                .satisfies(moneyCapital -> assertThat(moneyCapital.getPlannedAmount()).isEqualByComparingTo("374.7500"));
        assertThat(capitalAdjustmentRepository.findByUserIdAndCapitalCycleId(
                OWNER_ID,
                cycle.getId(),
                PageRequest.of(0, 10)
        ).getContent()).singleElement().satisfies(adjustment -> {
            assertThat(adjustment.getCapitalType()).isEqualTo(CapitalKind.MONEY);
            assertThat(adjustment.getAdjustmentType()).isEqualTo(CapitalAdjustmentType.DECREASE);
            assertThat(adjustment.getAmountDelta()).isEqualByComparingTo("-125.2500");
            assertThat(adjustment.getPreviousAmount()).isEqualByComparingTo("500.0000");
            assertThat(adjustment.getNewAmount()).isEqualByComparingTo("374.7500");
        });
        assertThat(capitalHistoryRepository.findByCapitalCycleIdAndActionType(
                cycle.getId(),
                CapitalActionType.ADJUSTMENT_DECREASE,
                PageRequest.of(0, 10)
        ).getContent()).singleElement().satisfies(history -> {
            assertThat(history.getCapitalType()).isEqualTo(CapitalKind.MONEY);
            assertThat(history.getAmount()).isEqualByComparingTo("125.2500");
            assertThat(history.getBeforeAmount()).isEqualByComparingTo("500.0000");
            assertThat(history.getAfterAmount()).isEqualByComparingTo("374.7500");
            assertThat(history.getReferenceType()).isEqualTo(CapitalReferenceType.MANUAL);
        });
    }

    @Test
    void adjustMoneyCapitalRejectsCurrencyMismatchWithoutWritingAdjustmentHistory() {
        CapitalCycle cycle = capitalCycleRepository.saveAndFlush(dailyCycle("August 9", LocalDate.of(2026, 8, 9)));
        capitalService.setupMoneyCapital(
                OWNER_ID,
                cycle.getId(),
                new SetupMoneyCapitalRequest(new BigDecimal("500.0000"), "usd")
        );

        assertThatThrownBy(() -> capitalAdjustmentService.adjustMoneyCapital(
                OWNER_ID,
                cycle.getId(),
                new AdjustMoneyCapitalRequest(
                        CapitalAdjustmentType.INCREASE,
                        new BigDecimal("125.2500"),
                        "Wrong unit",
                        "VND",
                        false,
                        null // <-- Bổ sung null vào đây
                )
        )).isInstanceOf(InvalidAdjustmentAmountException.class)
                .hasMessageContaining("must match cycle money capital currency USD");
        entityManager.flush();
        entityManager.clear();

        assertThat(moneyCapitalRepository.findByCapitalCycleId(cycle.getId()))
                .isPresent()
                .get()
                .satisfies(moneyCapital -> {
                    assertThat(moneyCapital.getPlannedAmount()).isEqualByComparingTo("500.0000");
                    assertThat(moneyCapital.getCurrencyCode()).isEqualTo("USD");
                });
        assertThat(capitalAdjustmentRepository.findByUserIdAndCapitalCycleId(
                OWNER_ID,
                cycle.getId(),
                PageRequest.of(0, 10)
        ).getContent()).isEmpty();
        assertThat(capitalHistoryRepository.findByCapitalCycleIdAndActionType(
                cycle.getId(),
                CapitalActionType.ADJUSTMENT_INCREASE,
                PageRequest.of(0, 10)
        ).getContent()).isEmpty();
    }

    @Test
    void adjustCapitalRejectsClosedCycleWithoutChangingCapitalOrWritingAdjustmentHistory() {
        CapitalCycle cycle = capitalCycleRepository.saveAndFlush(dailyCycle("August 6", LocalDate.of(2026, 8, 6)));
        capitalService.setupTimeCapital(OWNER_ID, cycle.getId(), new SetupTimeCapitalRequest(90L));
        cycle.activate(NOW.minusSeconds(120));
        cycle.close("Finished", NOW.minusSeconds(60));
        capitalCycleRepository.saveAndFlush(cycle);

        assertThatThrownBy(() -> capitalAdjustmentService.adjustTimeCapital(
                OWNER_ID,
                cycle.getId(),
                new AdjustTimeCapitalRequest(CapitalAdjustmentType.INCREASE, 30L, "Late change", false)
        )).isInstanceOf(CapitalCycleNotAdjustableException.class);
        entityManager.flush();
        entityManager.clear();

        assertThat(timeCapitalRepository.findByCapitalCycleId(cycle.getId()))
                .isPresent()
                .get()
                .satisfies(timeCapital -> assertThat(timeCapital.getPlannedMinutes()).isEqualTo(90L));
        assertThat(capitalHistoryRepository.findByCapitalCycleIdAndActionType(
                cycle.getId(),
                CapitalActionType.ADJUSTMENT_INCREASE,
                PageRequest.of(0, 10)
        )).isEmpty();
        assertThat(capitalAdjustmentRepository.findByUserIdAndCapitalCycleId(
                OWNER_ID,
                cycle.getId(),
                PageRequest.of(0, 10)
        ).getContent()).isEmpty();
    }

    @Test
    void adjustCapitalOverridePersistsImmutableAdjustmentWithSignedDelta() {
        CapitalCycle cycle = capitalCycleRepository.saveAndFlush(dailyCycle("August 8", LocalDate.of(2026, 8, 8)));
        capitalService.setupTimeCapital(OWNER_ID, cycle.getId(), new SetupTimeCapitalRequest(90L));

        CapitalAdjustmentResponseDTO response = capitalAdjustmentService.adjustCapital(
                OWNER_ID,
                new AdjustCapitalRequestDTO(
                        cycle.getId(),
                        CapitalKind.TIME,
                        CapitalAdjustmentType.OVERRIDE,
                        BigDecimal.ZERO,
                        "Reset available time",
                        false
                )
        );
        entityManager.flush();
        entityManager.clear();

        assertThat(response.historyActionType()).isEqualTo(CapitalActionType.CAPITAL_SET);
        assertThat(response.amountDelta()).isEqualByComparingTo("-90.0000");
        assertThat(timeCapitalRepository.findByCapitalCycleId(cycle.getId()))
                .isPresent()
                .get()
                .satisfies(timeCapital -> assertThat(timeCapital.getPlannedMinutes()).isZero());
        assertThat(capitalAdjustmentRepository.findByUserIdAndCapitalCycleId(
                OWNER_ID,
                cycle.getId(),
                PageRequest.of(0, 10)
        ).getContent()).singleElement()
                .extracting(CapitalAdjustment::getId)
                .isEqualTo(response.id());
    }

    @Test
    void persistsManualReferenceHistoryWithoutReferenceId() {
        CapitalCycle cycle = capitalCycleRepository.saveAndFlush(dailyCycle("August 7", LocalDate.of(2026, 8, 7)));

        CapitalHistory history = capitalHistoryRepository.saveAndFlush(CapitalHistory.record(
                cycle,
                CapitalKind.TIME,
                CapitalActionType.ADJUSTMENT_INCREASE,
                new BigDecimal("10.0000"),
                BigDecimal.ZERO,
                new BigDecimal("10.0000"),
                "Manual",
                null,
                CapitalReferenceType.MANUAL,
                null,
                CapitalActorType.USER,
                OWNER_ID
        ));
        entityManager.clear();

        assertThat(capitalHistoryRepository.findById(history.getId()))
                .isPresent()
                .get()
                .satisfies(found -> {
                    assertThat(found.getReferenceType()).isEqualTo(CapitalReferenceType.MANUAL);
                    assertThat(found.getReferenceId()).isNull();
                });
    }

    private CapitalCycle dailyCycle(String name, LocalDate date) {
        return CapitalCycle.create(
                OWNER_ID,
                name,
                "Daily resource cycle",
                CapitalCycleType.DAILY,
                date,
                date
        );
    }
}