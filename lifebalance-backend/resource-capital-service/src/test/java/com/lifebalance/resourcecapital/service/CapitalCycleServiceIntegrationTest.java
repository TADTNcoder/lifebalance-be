package com.lifebalance.resourcecapital.service;

import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.ActiveCapitalCycleAlreadyExistsException;
import com.lifebalance.resourcecapital.dto.CapitalCycleResponse;
import com.lifebalance.resourcecapital.dto.CreateCapitalCycleRequest;
import com.lifebalance.resourcecapital.infrastructure.persistence.CapitalCycleRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "eureka.client.enabled=false"
})
class CapitalCycleServiceIntegrationTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private CapitalCycleService capitalCycleService;

    @Autowired
    private CapitalCycleRepository capitalCycleRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void activateCyclePersistsStateThroughTransactionalDirtyChecking() {
        CapitalCycleResponse created = capitalCycleService.createCycle(
                OWNER_ID,
                createRequest("August 1", CapitalCycleType.DAILY, LocalDate.of(2026, 8, 1))
        );

        CapitalCycleResponse activated = capitalCycleService.activateCycle(OWNER_ID, created.getId());
        entityManager.flush();
        entityManager.clear();

        CapitalCycle found = capitalCycleRepository.findById(created.getId()).orElseThrow();

        assertThat(activated.getStatus()).isEqualTo(CapitalCycleStatus.ACTIVE);
        assertThat(found.getStatus()).isEqualTo(CapitalCycleStatus.ACTIVE);
        assertThat(found.getActivatedAt()).isNotNull();
    }

    @Test
    void activateCycleRejectsSecondActiveCycleForSameOwnerAndType() {
        CapitalCycleResponse first = capitalCycleService.createCycle(
                OWNER_ID,
                createRequest("August 1", CapitalCycleType.DAILY, LocalDate.of(2026, 8, 1))
        );
        CapitalCycleResponse second = capitalCycleService.createCycle(
                OWNER_ID,
                createRequest("August 2", CapitalCycleType.DAILY, LocalDate.of(2026, 8, 2))
        );
        capitalCycleService.activateCycle(OWNER_ID, first.getId());

        assertThatThrownBy(() -> capitalCycleService.activateCycle(OWNER_ID, second.getId()))
                .isInstanceOf(ActiveCapitalCycleAlreadyExistsException.class);
    }

    @Test
    void activateCycleAllowsDifferentTypesForSameOwner() {
        CapitalCycleResponse daily = capitalCycleService.createCycle(
                OWNER_ID,
                createRequest("August 3", CapitalCycleType.DAILY, LocalDate.of(2026, 8, 3))
        );
        CapitalCycleResponse weekly = capitalCycleService.createCycle(
                OWNER_ID,
                createRequest("August week 1", CapitalCycleType.WEEKLY, LocalDate.of(2026, 8, 1))
        );

        CapitalCycleResponse activeDaily = capitalCycleService.activateCycle(OWNER_ID, daily.getId());
        CapitalCycleResponse activeWeekly = capitalCycleService.activateCycle(OWNER_ID, weekly.getId());

        assertThat(activeDaily.getStatus()).isEqualTo(CapitalCycleStatus.ACTIVE);
        assertThat(activeWeekly.getStatus()).isEqualTo(CapitalCycleStatus.ACTIVE);
    }

    private CreateCapitalCycleRequest createRequest(String name, CapitalCycleType type, LocalDate startDate) {
        CreateCapitalCycleRequest request = new CreateCapitalCycleRequest();
        request.setName(name);
        request.setDescription("Resource cycle");
        request.setType(type);
        request.setStartDate(startDate);
        request.setEndDate(endDate(type, startDate));
        return request;
    }

    private LocalDate endDate(CapitalCycleType type, LocalDate startDate) {
        return switch (type) {
            case DAILY -> startDate;
            case WEEKLY -> startDate.plusDays(6);
            case MONTHLY -> startDate.withDayOfMonth(startDate.lengthOfMonth());
        };
    }
}
