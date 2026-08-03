package com.lifebalance.resourcecapital.infrastructure.persistence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "eureka.client.enabled=false",
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.flyway.locations=classpath:db/migration/postgresql"
})
class CapitalCyclePostgresConstraintTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_OWNER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16.4-alpine")
            .withDatabaseName("resourcecapital_test")
            .withUsername("resourcecapital")
            .withPassword("resourcecapital");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @BeforeEach
    @AfterEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM resourcecapital.money_capitals");
        jdbcTemplate.update("DELETE FROM resourcecapital.time_capitals");
        jdbcTemplate.update("DELETE FROM resourcecapital.capital_cycles");
    }

    @Test
    void rejectsTwoActiveCyclesForSameOwnerAndType() {
        insertCycle(OWNER_ID, "DAILY", LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 1), "ACTIVE");

        assertThatThrownBy(() -> insertCycle(
                OWNER_ID,
                "DAILY",
                LocalDate.of(2026, 8, 2),
                LocalDate.of(2026, 8, 2),
                "ACTIVE"
        ))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("uq_capital_cycles_owner_type_active");
    }

    @Test
    void allowsActiveCyclesForSameOwnerWhenTypeDiffers() {
        insertCycle(OWNER_ID, "DAILY", LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 1), "ACTIVE");
        insertCycle(OWNER_ID, "WEEKLY", LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 7), "ACTIVE");

        assertThat(countCycles()).isEqualTo(2);
    }

    @Test
    void allowsActiveCyclesForDifferentOwnersWithSameType() {
        insertCycle(OWNER_ID, "DAILY", LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 1), "ACTIVE");
        insertCycle(OTHER_OWNER_ID, "DAILY", LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 1), "ACTIVE");

        assertThat(countCycles()).isEqualTo(2);
    }

    @ParameterizedTest
    @ValueSource(strings = {"DRAFT", "CLOSED", "REOPENED"})
    void allowsMultipleNonActiveCyclesForSameOwnerAndType(String status) {
        insertCycle(OWNER_ID, "WEEKLY", LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 9), status);
        insertCycle(OWNER_ID, "WEEKLY", LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 16), status);

        assertThat(countCycles()).isEqualTo(2);
    }

    @Test
    void allowsOnlyOneConcurrentActiveInsertForSameOwnerAndType() throws Exception {
        CountDownLatch firstInsertCompleted = new CountDownLatch(1);
        CountDownLatch secondInsertStarted = new CountDownLatch(1);
        CountDownLatch releaseFirstCommit = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
            Future<Boolean> firstInsert = executorService.submit(() -> insertActiveCycleInTransaction(
                    OWNER_ID,
                    "DAILY",
                    LocalDate.of(2026, 8, 1),
                    LocalDate.of(2026, 8, 1),
                    firstInsertCompleted,
                    releaseFirstCommit
            ));

            assertThat(firstInsertCompleted.await(5, TimeUnit.SECONDS)).isTrue();

            Future<Boolean> secondInsert = executorService.submit(() -> insertActiveCycle(
                    OWNER_ID,
                    "DAILY",
                    LocalDate.of(2026, 8, 2),
                    LocalDate.of(2026, 8, 2),
                    secondInsertStarted
            ));

            assertThat(secondInsertStarted.await(5, TimeUnit.SECONDS)).isTrue();
            releaseFirstCommit.countDown();

            assertThat(firstInsert.get(5, TimeUnit.SECONDS)).isTrue();
            assertThat(secondInsert.get(5, TimeUnit.SECONDS)).isFalse();
            assertThat(countCycles()).isEqualTo(1);
        } finally {
            releaseFirstCommit.countDown();
            executorService.shutdownNow();
        }
    }

    @Test
    void rejectsNegativePlannedMinutes() {
        UUID cycleId = insertCycle(OWNER_ID, "DAILY", LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 1), "DRAFT");

        assertThatThrownBy(() -> jdbcTemplate.update("""
                        INSERT INTO resourcecapital.time_capitals (
                            id, capital_cycle_id, planned_minutes, created_at, updated_at, version
                        )
                        VALUES (gen_random_uuid(), ?, -1, now(), now(), 0)
                        """, cycleId))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("chk_time_capitals_planned_minutes");
    }

    @Test
    void rejectsNegativePlannedAmount() {
        UUID cycleId = insertCycle(OWNER_ID, "DAILY", LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 1), "DRAFT");

        assertThatThrownBy(() -> jdbcTemplate.update("""
                        INSERT INTO resourcecapital.money_capitals (
                            id, capital_cycle_id, planned_amount, currency_code, created_at, updated_at, version
                        )
                        VALUES (gen_random_uuid(), ?, -0.0001, 'VND', now(), now(), 0)
                        """, cycleId))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("chk_money_capitals_planned_amount");
    }

    private UUID insertCycle(UUID ownerId, String type, LocalDate startDate, LocalDate endDate, String status) {
        return jdbcTemplate.queryForObject("""
                        INSERT INTO resourcecapital.capital_cycles (
                            owner_id, cycle_type, start_date, end_date, status
                        )
                        VALUES (?, ?, ?, ?, ?)
                        RETURNING id
                        """,
                UUID.class,
                ownerId,
                type,
                startDate,
                endDate,
                status
        );
    }

    private long countCycles() {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM resourcecapital.capital_cycles",
                Long.class
        );
        return count == null ? 0L : count;
    }

    private boolean insertActiveCycle(
            UUID ownerId,
            String type,
            LocalDate startDate,
            LocalDate endDate,
            CountDownLatch insertStarted
    ) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            insertStarted.countDown();
            insertCycle(connection, ownerId, type, startDate, endDate, "ACTIVE");
            connection.commit();
            return true;
        } catch (SQLException exception) {
            return false;
        }
    }

    private boolean insertActiveCycleInTransaction(
            UUID ownerId,
            String type,
            LocalDate startDate,
            LocalDate endDate,
            CountDownLatch firstInsertCompleted,
            CountDownLatch releaseFirstCommit
    ) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            insertCycle(connection, ownerId, type, startDate, endDate, "ACTIVE");
            firstInsertCompleted.countDown();
            assertThat(releaseFirstCommit.await(5, TimeUnit.SECONDS)).isTrue();
            connection.commit();
            return true;
        }
    }

    private void insertCycle(
            Connection connection,
            UUID ownerId,
            String type,
            LocalDate startDate,
            LocalDate endDate,
            String status
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO resourcecapital.capital_cycles (
                    owner_id, cycle_type, start_date, end_date, status
                )
                VALUES (?, ?, ?, ?, ?)
                """)) {
            statement.setObject(1, ownerId);
            statement.setString(2, type);
            statement.setObject(3, startDate);
            statement.setObject(4, endDate);
            statement.setString(5, status);
            statement.executeUpdate();
        }
    }
}
