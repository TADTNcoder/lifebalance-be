package com.lifebalance.task.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "eureka.client.enabled=false",
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.flyway.locations=classpath:db/migration/postgresql",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/.well-known/jwks.json"
})
class TaskFlywayMigrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16.4-alpine")
            .withDatabaseName("task_test")
            .withUsername("task")
            .withPassword("task");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Test
    void flywayCreatesTagsTableAndPartialUniqueIndex() {
        assertTableExists("tags");
        assertColumnExists("tags", "id", "uuid");
        assertColumnExists("tags", "user_id", "uuid");
        assertColumnExists("tags", "name", "character varying");
        assertColumnExists("tags", "created_at", "timestamp with time zone");
        assertColumnExists("tags", "updated_at", "timestamp with time zone");
        assertColumnExists("tags", "deleted_at", "timestamp with time zone");
        assertIndexExists("uq_tags_user_name_active");
        assertIndexPredicate("uq_tags_user_name_active", "(deleted_at IS NULL)");
    }

    @Test
    void partialUniqueIndexRejectsDuplicateActiveTagsButAllowsNameReuseAfterSoftDelete() {
        UUID userId = UUID.randomUUID();
        insertTag(userId, "Work");

        assertThatThrownBy(() -> insertTag(userId, "Work"))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("uq_tags_user_name_active");

        jdbcTemplate.update("""
                UPDATE task.tags
                SET deleted_at = now()
                WHERE user_id = ?
                  AND name = ?
                """, userId, "Work");

        insertTag(userId, "Work");

        Long tagCount = jdbcTemplate.queryForObject("""
                SELECT count(*)
                FROM task.tags
                WHERE user_id = ?
                  AND name = ?
                """, Long.class, userId, "Work");

        assertThat(tagCount).isEqualTo(2);
    }

    @Test
    void tagsTableDoesNotCreatePhysicalUserForeignKey() {
        Integer foreignKeyCount = jdbcTemplate.queryForObject("""
                SELECT count(*)
                FROM information_schema.table_constraints
                WHERE lower(table_schema) = 'task'
                  AND lower(table_name) = 'tags'
                  AND lower(constraint_type) = 'foreign key'
                """, Integer.class);

        assertThat(foreignKeyCount).isZero();
    }

    private void insertTag(UUID userId, String name) {
        jdbcTemplate.update("""
                INSERT INTO task.tags (user_id, name)
                VALUES (?, ?)
                """, userId, name);
    }

    private void assertTableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT count(*)
                FROM information_schema.tables
                WHERE lower(table_schema) = 'task'
                  AND lower(table_name) = lower(?)
                """, Integer.class, tableName);

        assertThat(count).isEqualTo(1);
    }

    private void assertColumnExists(String tableName, String columnName, String dataType) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT count(*)
                FROM information_schema.columns
                WHERE lower(table_schema) = 'task'
                  AND lower(table_name) = lower(?)
                  AND lower(column_name) = lower(?)
                  AND lower(data_type) = lower(?)
                """, Integer.class, tableName, columnName, dataType);

        assertThat(count).isEqualTo(1);
    }

    private void assertIndexExists(String indexName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT count(*)
                FROM pg_indexes
                WHERE lower(schemaname) = 'task'
                  AND lower(indexname) = lower(?)
                """, Integer.class, indexName);

        assertThat(count).isEqualTo(1);
    }

    private void assertIndexPredicate(String indexName, String predicate) {
        String indexDefinition = jdbcTemplate.queryForObject("""
                SELECT indexdef
                FROM pg_indexes
                WHERE lower(schemaname) = 'task'
                  AND lower(indexname) = lower(?)
                """, String.class, indexName);

        assertThat(indexDefinition).contains(predicate);
    }
}
