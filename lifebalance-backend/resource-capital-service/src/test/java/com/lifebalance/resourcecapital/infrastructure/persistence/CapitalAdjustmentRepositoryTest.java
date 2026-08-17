package com.lifebalance.resourcecapital.infrastructure.persistence;

import com.lifebalance.resourcecapital.domain.capital.CapitalAdjustmentType;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitaladjustment.AdjustmentType;
import com.lifebalance.resourcecapital.domain.capitaladjustment.CapitalAdjustment;
import com.lifebalance.resourcecapital.domain.capitaladjustment.CapitalType;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "eureka.client.enabled=false"
})
class CapitalAdjustmentRepositoryTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_OWNER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 8, 1, 10, 0);

    @Autowired
    private CapitalCycleRepository capitalCycleRepository;

    @Autowired
    private CapitalAdjustmentRepository capitalAdjustmentRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findsAdjustmentsByOwnerAndCycleWithoutLeakingOtherUsers() {
        CapitalCycle cycle = capitalCycleRepository.save(dailyCycle(OWNER_ID, "Owner cycle", LocalDate.of(2026, 8, 1)));
        CapitalCycle otherCycle = capitalCycleRepository.save(dailyCycle(OTHER_OWNER_ID, "Other cycle", LocalDate.of(2026, 8, 2)));
        capitalCycleRepository.flush();
        CapitalAdjustment visible = capitalAdjustmentRepository.save(adjustment(
                cycle,
                OWNER_ID,
                CapitalKind.TIME,
                CapitalAdjustmentType.INCREASE,
                "0.0000",
                "60.0000",
                "Increase planned time"
        ));
        capitalAdjustmentRepository.save(adjustment(
                otherCycle,
                OTHER_OWNER_ID,
                CapitalKind.TIME,
                CapitalAdjustmentType.INCREASE,
                "0.0000",
                "45.0000",
                "Other owner increase"
        ));
        capitalAdjustmentRepository.flush();
        entityManager.clear();

        var page = capitalAdjustmentRepository.findByUserIdAndCapitalCycleId(
                OWNER_ID,
                cycle.getId(),
                PageRequest.of(0, 10)
        );
        var leakedPage = capitalAdjustmentRepository.findByUserIdAndCapitalCycleId(
                OWNER_ID,
                otherCycle.getId(),
                PageRequest.of(0, 10)
        );

        assertThat(page.getContent())
                .extracting(CapitalAdjustment::getId)
                .containsExactly(visible.getId());
        assertThat(leakedPage.getContent()).isEmpty();
    }

    @Test
    void filtersByOwnerCycleCapitalTypeAndAdjustmentType() {
        CapitalCycle cycle = capitalCycleRepository.save(dailyCycle(OWNER_ID, "Owner cycle", LocalDate.of(2026, 8, 3)));
        CapitalCycle otherCycle = capitalCycleRepository.save(dailyCycle(OTHER_OWNER_ID, "Other cycle", LocalDate.of(2026, 8, 4)));
        capitalCycleRepository.flush();
        CapitalAdjustment timeIncrease = capitalAdjustmentRepository.save(adjustment(
                cycle,
                OWNER_ID,
                CapitalKind.TIME,
                CapitalAdjustmentType.INCREASE,
                "0.0000",
                "60.0000",
                "Increase planned time"
        ));
        CapitalAdjustment timeDecrease = capitalAdjustmentRepository.save(adjustment(
                cycle,
                OWNER_ID,
                CapitalKind.TIME,
                CapitalAdjustmentType.DECREASE,
                "60.0000",
                "30.0000",
                "Decrease planned time"
        ));
        capitalAdjustmentRepository.save(adjustment(
                cycle,
                OWNER_ID,
                CapitalKind.MONEY,
                CapitalAdjustmentType.INCREASE,
                "0.0000",
                "100.0000",
                "Increase planned money"
        ));
        capitalAdjustmentRepository.save(adjustment(
                otherCycle,
                OTHER_OWNER_ID,
                CapitalKind.TIME,
                CapitalAdjustmentType.INCREASE,
                "0.0000",
                "90.0000",
                "Other owner increase"
        ));
        capitalAdjustmentRepository.flush();
        entityManager.clear();

        var timePage = capitalAdjustmentRepository.findByUserIdAndCapitalCycleIdAndCapitalType(
                OWNER_ID,
                cycle.getId(),
                CapitalType.TIME,
                PageRequest.of(0, 10)
        );
        var increasePage = capitalAdjustmentRepository.findAll(
                CapitalAdjustmentSpecification.filter(
                        OWNER_ID,
                        cycle.getId(),
                        CapitalType.TIME,
                        AdjustmentType.INCREASE,
                        null,
                        null
                ),
                PageRequest.of(0, 10)
        );

        assertThat(timePage.getContent())
                .extracting(CapitalAdjustment::getId)
                .containsExactly(timeDecrease.getId(), timeIncrease.getId());
        assertThat(increasePage.getContent())
                .extracting(CapitalAdjustment::getId)
                .containsExactly(timeIncrease.getId());
    }

    @Test
    void findsAdjustmentsByOwnerAndCreatedAtRange() {
        CapitalCycle cycle = capitalCycleRepository.save(dailyCycle(OWNER_ID, "Owner cycle", LocalDate.of(2026, 8, 5)));
        CapitalCycle otherCycle = capitalCycleRepository.save(dailyCycle(OTHER_OWNER_ID, "Other cycle", LocalDate.of(2026, 8, 6)));
        capitalCycleRepository.flush();
        CapitalAdjustment beforeRange = capitalAdjustmentRepository.save(adjustment(
                cycle,
                OWNER_ID,
                CapitalKind.MONEY,
                CapitalAdjustmentType.INCREASE,
                "0.0000",
                "50.0000",
                "Before range"
        ));
        CapitalAdjustment insideRange = capitalAdjustmentRepository.save(adjustment(
                cycle,
                OWNER_ID,
                CapitalKind.MONEY,
                CapitalAdjustmentType.INCREASE,
                "50.0000",
                "75.0000",
                "Inside range"
        ));
        CapitalAdjustment afterRange = capitalAdjustmentRepository.save(adjustment(
                cycle,
                OWNER_ID,
                CapitalKind.MONEY,
                CapitalAdjustmentType.INCREASE,
                "75.0000",
                "90.0000",
                "After range"
        ));
        CapitalAdjustment otherOwnerInsideRange = capitalAdjustmentRepository.save(adjustment(
                otherCycle,
                OTHER_OWNER_ID,
                CapitalKind.MONEY,
                CapitalAdjustmentType.INCREASE,
                "0.0000",
                "25.0000",
                "Other owner inside range"
        ));
        capitalAdjustmentRepository.flush();
        updateCreatedAt(beforeRange.getId(), BASE_TIME.minusMinutes(1));
        updateCreatedAt(insideRange.getId(), BASE_TIME.plusMinutes(30));
        updateCreatedAt(afterRange.getId(), BASE_TIME.plusHours(2));
        updateCreatedAt(otherOwnerInsideRange.getId(), BASE_TIME.plusMinutes(45));
        entityManager.flush();
        entityManager.clear();

        var page = capitalAdjustmentRepository.findByUserIdAndCreatedAtBetween(
                OWNER_ID,
                BASE_TIME,
                BASE_TIME.plusHours(1),
                PageRequest.of(0, 10)
        );

        assertThat(page.getContent())
                .extracting(CapitalAdjustment::getId)
                .containsExactly(insideRange.getId());
    }

    private CapitalAdjustment adjustment(
            CapitalCycle cycle,
            UUID userId,
            CapitalKind capitalType,
            CapitalAdjustmentType adjustmentType,
            String previousAmount,
            String newAmount,
            String reason
    ) {
        return CapitalAdjustment.record(
                cycle,
                userId,
                capitalType,
                adjustmentType,
                new BigDecimal(previousAmount),
                new BigDecimal(newAmount),
                reason
        );
    }

    private void updateCreatedAt(Long adjustmentId, LocalDateTime createdAt) {
        entityManager.createNativeQuery("""
                        update resourcecapital.capital_adjustments
                        set created_at = :createdAt
                        where id = :id
                        """)
                .setParameter("createdAt", createdAt)
                .setParameter("id", adjustmentId)
                .executeUpdate();
    }

    private CapitalCycle dailyCycle(UUID ownerId, String name, LocalDate date) {
        return CapitalCycle.create(
                ownerId,
                name,
                "Daily resource cycle",
                CapitalCycleType.DAILY,
                date,
                date
        );
    }
}
