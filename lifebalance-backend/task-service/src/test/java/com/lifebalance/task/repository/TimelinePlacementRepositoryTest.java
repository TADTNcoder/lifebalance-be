package com.lifebalance.task.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.lifebalance.task.model.Task;
import com.lifebalance.task.model.TimelinePlacement;
import com.lifebalance.task.model.enums.TaskStatus;
import com.lifebalance.task.model.enums.TimelinePlacementStatus;
import jakarta.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:timeline_placement_repository;MODE=PostgreSQL;DATABASE_TO_UPPER=false;INIT=CREATE SCHEMA IF NOT EXISTS task",
        "spring.datasource.driver-class-name=org.h2.Driver"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TimelinePlacementRepositoryTest {

    private static final UUID OWNER_ID = UUID.randomUUID();
    private static final OffsetDateTime START_AT = OffsetDateTime.parse("2026-09-03T09:00:00+07:00");
    private static final OffsetDateTime END_AT = OffsetDateTime.parse("2026-09-03T10:00:00+07:00");

    @Autowired
    private TimelinePlacementRepository timelinePlacementRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void ignoresActivePlacementOwnedByCompletedTask() {
        persistPlacement(TaskStatus.COMPLETED);
        entityManager.flush();
        entityManager.clear();

        boolean conflict = timelinePlacementRepository.existsOverlappingPlacement(
                OWNER_ID,
                TimelinePlacementStatus.ACTIVE,
                null,
                START_AT.plusMinutes(15),
                END_AT.plusMinutes(15)
        );

        assertThat(conflict).isFalse();
    }

    @Test
    void detectsScheduledTaskConflictAndCanExcludeCurrentPlacement() {
        TimelinePlacement placement = persistPlacement(TaskStatus.SCHEDULED);
        entityManager.flush();
        entityManager.clear();

        assertThat(timelinePlacementRepository.existsOverlappingPlacement(
                OWNER_ID,
                TimelinePlacementStatus.ACTIVE,
                null,
                START_AT.plusMinutes(15),
                END_AT.plusMinutes(15)
        )).isTrue();
        assertThat(timelinePlacementRepository.existsOverlappingPlacement(
                OWNER_ID,
                TimelinePlacementStatus.ACTIVE,
                placement.getId(),
                START_AT.plusMinutes(15),
                END_AT.plusMinutes(15)
        )).isFalse();
    }

    private TimelinePlacement persistPlacement(TaskStatus taskStatus) {
        Task task = Task.builder()
                .ownerId(OWNER_ID)
                .userId(OWNER_ID)
                .name("Timeline task " + taskStatus)
                .status(taskStatus)
                .build();
        entityManager.persist(task);

        TimelinePlacement placement = TimelinePlacement.builder()
                .ownerId(OWNER_ID)
                .userId(OWNER_ID)
                .task(task)
                .startAt(START_AT)
                .endAt(END_AT)
                .timezone("Asia/Bangkok")
                .source("MANUAL")
                .status(TimelinePlacementStatus.ACTIVE)
                .build();
        entityManager.persist(placement);
        return placement;
    }
}
