package com.lifebalance.task.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "eureka.client.enabled=false",
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.flyway.locations=classpath:db/migration/postgresql",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/.well-known/jwks.json"
})
class TaskFlywayMigrationTest {

    private static final String TASK_MIGRATION_LOCATION = "classpath:db/migration/postgresql";

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16.4-alpine")
            .withDatabaseName("task_test")
            .withUsername("task")
            .withPassword("task");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ResourceLoader resourceLoader;

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
        assertColumnExists("tags", "slug", "character varying");
        assertColumnExists("tags", "color", "character varying");
        assertColumnExists("tags", "is_system", "boolean");
        assertColumnExists("tags", "created_at", "timestamp with time zone");
        assertColumnExists("tags", "updated_at", "timestamp with time zone");
        assertColumnExists("tags", "deleted_at", "timestamp with time zone");
        assertColumnIsNotNullable("tags", "slug");
        assertColumnIsNotNullable("tags", "is_system");
        assertIndexExists("uq_tags_user_slug_active");
        assertIndexExists("idx_tags_user_name_active");
        assertIndexDoesNotExist("uq_tags_user_name_active");
        assertIndexPredicate("uq_tags_user_slug_active", "(deleted_at IS NULL)");
        assertIndexPredicate("idx_tags_user_name_active", "(deleted_at IS NULL)");
    }

    @Test
    void flywayCreatesTaskTagsTableWithCompositePrimaryKeyCascadeForeignKeysAndTagTaskIndex() {
        assertTableExists("task_tags");
        assertColumnExists("task_tags", "task_id", "uuid");
        assertColumnExists("task_tags", "tag_id", "uuid");
        assertColumnExists("task_tags", "created_at", "timestamp with time zone");
        assertColumnIsNotNullable("task_tags", "task_id");
        assertColumnIsNotNullable("task_tags", "tag_id");
        assertColumnIsNotNullable("task_tags", "created_at");
        assertPrimaryKeyColumns("task_tags", List.of("task_id", "tag_id"));
        assertForeignKeyDeleteRule("task_tags", "fk_task_tags_task", "tasks", "CASCADE");
        assertForeignKeyDeleteRule("task_tags", "fk_task_tags_tag", "tags", "CASCADE");
        assertIndexDoesNotExist("idx_task_tags_tag_id");
        assertIndexExists("idx_task_tags_tag_task");
        assertIndexColumns("idx_task_tags_tag_task", List.of("tag_id", "task_id"));
    }

    @Test
    void flywayCreatesTaskModuleFilterIndexesWithExpectedColumns() {
        assertIndexExists("idx_tasks_user_status");
        assertIndexColumns("idx_tasks_user_status", List.of("user_id", "status"));

        assertIndexExists("idx_tasks_user_deadline");
        assertIndexColumns("idx_tasks_user_deadline", List.of("user_id", "deadline"));

        assertIndexExists("idx_tasks_user_priority");
        assertIndexColumns("idx_tasks_user_priority", List.of("user_id", "priority"));

        assertIndexExists("idx_tasks_user_category");
        assertIndexColumns("idx_tasks_user_category", List.of("user_id", "category_id"));

        assertIndexExists("idx_task_tags_tag_task");
        assertIndexColumns("idx_task_tags_tag_task", List.of("tag_id", "task_id"));
    }

    @Test
    void flywayExtendsCategoriesTableForDefaultCategoryMetadata() {
        assertTableExists("categories");
        assertColumnExists("categories", "slug", "character varying");
        assertColumnExists("categories", "color", "character varying");
        assertColumnExists("categories", "icon", "character varying");
        assertColumnExists("categories", "is_system", "boolean");
        assertColumnIsNotNullable("categories", "slug");
        assertColumnIsNotNullable("categories", "is_system");
        assertIndexExists("uq_categories_slug_active");
        assertIndexPredicate("uq_categories_slug_active", "(deleted_at IS NULL)");
    }

    @Test
    void flywaySeedsDefaultSystemCategories() {
        List<String> categories = jdbcTemplate.queryForList("""
                SELECT name || '|' || slug || '|' || color || '|' || icon || '|' || is_system::text
                FROM task.categories
                WHERE slug IN ('work', 'personal-development', 'health', 'finance', 'learning')
                ORDER BY slug
                """, String.class);

        assertThat(categories).containsExactly(
                "Finance|finance|#FB8C00|dollar-sign|true",
                "Health & Fitness|health|#E53935|heart|true",
                "Learning|learning|#8E24AA|book|true",
                "Personal Development|personal-development|#43A047|user|true",
                "Work|work|#1E88E5|briefcase|true"
        );
    }

    @Test
    void defaultCategorySeedScriptCanRunAgainWithoutCreatingDuplicates() {
        long defaultCategoryCountBefore = countDefaultCategories();

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                resourceLoader.getResource("classpath:db/migration/postgresql/V9__seed_default_categories.sql")
        );
        populator.execute(dataSource);

        assertThat(countDefaultCategories()).isEqualTo(defaultCategoryCountBefore);
    }

    @Test
    void taskTagsRejectDuplicateTaskTagPairs() {
        UUID userId = UUID.randomUUID();
        UUID taskId = insertTask(userId, "Task with one tag");
        UUID tagId = insertTagReturningId(userId, "Duplicate Guard", "duplicate-guard");
        insertTaskTag(taskId, tagId);

        assertThatThrownBy(() -> insertTaskTag(taskId, tagId))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("pk_task_tags");
    }

    @Test
    void taskTagsAreDeletedWhenTaskIsHardDeleted() {
        UUID userId = UUID.randomUUID();
        UUID taskId = insertTask(userId, "Task cascade source");
        UUID tagId = insertTagReturningId(userId, "Task Cascade", "task-cascade");
        insertTaskTag(taskId, tagId);

        jdbcTemplate.update("""
                DELETE FROM task.tasks
                WHERE id = ?
                """, taskId);

        assertTaskTagCount(taskId, tagId, 0L);
    }

    @Test
    void taskTagsAreDeletedWhenTagIsHardDeleted() {
        UUID userId = UUID.randomUUID();
        UUID taskId = insertTask(userId, "Tag cascade source");
        UUID tagId = insertTagReturningId(userId, "Tag Cascade", "tag-cascade");
        insertTaskTag(taskId, tagId);

        jdbcTemplate.update("""
                DELETE FROM task.tags
                WHERE id = ?
                """, tagId);

        assertTaskTagCount(taskId, tagId, 0L);
    }

    @Test
    void partialUniqueIndexRejectsDuplicateActiveSlugsButAllowsSlugReuseAfterSoftDelete() {
        UUID userId = UUID.randomUUID();
        insertTag(userId, "Work", "work");

        assertThatThrownBy(() -> insertTag(userId, "Office", "work"))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("uq_tags_user_slug_active");

        jdbcTemplate.update("""
                UPDATE task.tags
                SET deleted_at = now()
                WHERE user_id = ?
                  AND slug = ?
                """, userId, "work");

        insertTag(userId, "Office", "work");

        Long tagCount = jdbcTemplate.queryForObject("""
                SELECT count(*)
                FROM task.tags
                WHERE user_id = ?
                  AND slug = ?
                """, Long.class, userId, "work");

        assertThat(tagCount).isEqualTo(2);
    }

    @Test
    void v5BackfillsSlugForRowsCreatedByV4BeforeSettingNotNull() {
        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.4-alpine")
                .withDatabaseName("task_v5_backfill")
                .withUsername("task")
                .withPassword("task")) {
            postgres.start();

            DriverManagerDataSource dataSource = new DriverManagerDataSource(
                    postgres.getJdbcUrl(),
                    postgres.getUsername(),
                    postgres.getPassword()
            );
            dataSource.setDriverClassName("org.postgresql.Driver");
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);

            flywayConfiguration(dataSource)
                    .target("4")
                    .load()
                    .migrate();

            UUID userId = UUID.randomUUID();
            UUID vietnameseTagId = UUID.fromString("00000000-0000-0000-0000-000000000001");
            UUID cPlusPlusTagId = UUID.fromString("00000000-0000-0000-0000-000000000002");
            UUID cSharpTagId = UUID.fromString("00000000-0000-0000-0000-000000000003");
            insertLegacyTag(jdbc, vietnameseTagId, userId, "Công việc");
            insertLegacyTag(jdbc, cPlusPlusTagId, userId, "C++");
            insertLegacyTag(jdbc, cSharpTagId, userId, "C#");

            flywayConfiguration(dataSource)
                    .load()
                    .migrate();

            List<String> slugs = jdbc.queryForList("""
                    SELECT slug
                    FROM task.tags
                    ORDER BY id
                    """, String.class);

            assertThat(slugs)
                    .hasSize(3)
                    .contains("cong-viec", "c");
            assertThat(slugs.get(2))
                    .startsWith("c-")
                    .hasSize(34);
            assertThat(jdbc.queryForObject("""
                    SELECT count(*)
                    FROM task.tags
                    WHERE slug IS NULL
                    """, Long.class)).isZero();
        }
    }

    @Test
    void v8BackfillsCategorySlugForRowsCreatedBeforeCategoryMetadata() {
        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.4-alpine")
                .withDatabaseName("task_v8_category_backfill")
                .withUsername("task")
                .withPassword("task")) {
            postgres.start();

            DriverManagerDataSource dataSource = new DriverManagerDataSource(
                    postgres.getJdbcUrl(),
                    postgres.getUsername(),
                    postgres.getPassword()
            );
            dataSource.setDriverClassName("org.postgresql.Driver");
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);

            flywayConfiguration(dataSource)
                    .target("7")
                    .load()
                    .migrate();

            UUID vietnameseCategoryId = UUID.fromString("00000000-0000-0000-0000-000000000101");
            UUID cPlusPlusCategoryId = UUID.fromString("00000000-0000-0000-0000-000000000102");
            UUID cSharpCategoryId = UUID.fromString("00000000-0000-0000-0000-000000000103");
            insertLegacyCategory(jdbc, vietnameseCategoryId, "Công việc");
            insertLegacyCategory(jdbc, cPlusPlusCategoryId, "C++");
            insertLegacyCategory(jdbc, cSharpCategoryId, "C#");

            flywayConfiguration(dataSource)
                    .load()
                    .migrate();

            List<String> slugs = jdbc.queryForList("""
                    SELECT slug
                    FROM task.categories
                    WHERE id IN (?, ?, ?)
                    ORDER BY id
                    """, String.class, vietnameseCategoryId, cPlusPlusCategoryId, cSharpCategoryId);

            assertThat(slugs)
                    .hasSize(3)
                    .contains("cong-viec", "c");
            assertThat(slugs.get(2))
                    .startsWith("c-")
                    .hasSize(34);
            assertThat(jdbc.queryForObject("""
                    SELECT count(*)
                    FROM task.categories
                    WHERE slug IS NULL
                    """, Long.class)).isZero();
        }
    }

    @Test
    void v9RestoresSoftDeletedSystemCategoryFromCanonicalSeed() {
        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.4-alpine")
                .withDatabaseName("task_v9_category_restore")
                .withUsername("task")
                .withPassword("task")) {
            postgres.start();

            DriverManagerDataSource dataSource = new DriverManagerDataSource(
                    postgres.getJdbcUrl(),
                    postgres.getUsername(),
                    postgres.getPassword()
            );
            dataSource.setDriverClassName("org.postgresql.Driver");
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);

            flywayConfiguration(dataSource)
                    .target("8")
                    .load()
                    .migrate();

            UUID deletedWorkId = UUID.fromString("00000000-0000-0000-0000-000000000201");
            jdbc.update("""
                    INSERT INTO task.categories (id, name, slug, color, icon, is_system, deleted_at)
                    VALUES (?, 'Archived Work', 'work', '#000000', 'archive', false, now())
                    """, deletedWorkId);

            flywayConfiguration(dataSource)
                    .load()
                    .migrate();

            String restoredCategory = jdbc.queryForObject("""
                    SELECT name || '|' || slug || '|' || color || '|' || icon || '|'
                           || is_system::text || '|' || (deleted_at IS NULL)::text
                    FROM task.categories
                    WHERE id = ?
                    """, String.class, deletedWorkId);

            assertThat(restoredCategory)
                    .isEqualTo("Work|work|#1E88E5|briefcase|true|true");
            assertThat(countSeedRowsBySlug(jdbc, "work")).isEqualTo(1L);
        }
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

    private void insertTag(UUID userId, String name, String slug) {
        jdbcTemplate.update("""
                INSERT INTO task.tags (user_id, name, slug)
                VALUES (?, ?, ?)
                """, userId, name, slug);
    }

    private FluentConfiguration flywayConfiguration(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations(TASK_MIGRATION_LOCATION)
                .defaultSchema("public")
                .schemas("public");
    }

    private UUID insertTagReturningId(UUID userId, String name, String slug) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO task.tags (user_id, name, slug)
                VALUES (?, ?, ?)
                RETURNING id
                """, UUID.class, userId, name, slug);
    }

    private UUID insertTask(UUID userId, String name) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO task.tasks (user_id, owner_id, name, status, priority)
                VALUES (?, ?, ?, 'DRAFT', 'LOW')
                RETURNING id
                """, UUID.class, userId, userId, name);
    }

    private void insertTaskTag(UUID taskId, UUID tagId) {
        jdbcTemplate.update("""
                INSERT INTO task.task_tags (task_id, tag_id)
                VALUES (?, ?)
                """, taskId, tagId);
    }

    private void insertLegacyTag(JdbcTemplate jdbc, UUID id, UUID userId, String name) {
        jdbc.update("""
                INSERT INTO task.tags (id, user_id, name)
                VALUES (?, ?, ?)
                """, id, userId, name);
    }

    private void insertLegacyCategory(JdbcTemplate jdbc, UUID id, String name) {
        jdbc.update("""
                INSERT INTO task.categories (id, name)
                VALUES (?, ?)
                """, id, name);
    }

    private long countDefaultCategories() {
        Long count = jdbcTemplate.queryForObject("""
                SELECT count(*)
                FROM task.categories
                WHERE slug IN ('work', 'personal-development', 'health', 'finance', 'learning')
                """, Long.class);
        return count == null ? 0L : count;
    }

    private long countSeedRowsBySlug(JdbcTemplate jdbc, String slug) {
        Long count = jdbc.queryForObject("""
                SELECT count(*)
                FROM task.categories
                WHERE slug = ?
                """, Long.class, slug);
        return count == null ? 0L : count;
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

    private void assertColumnIsNotNullable(String tableName, String columnName) {
        String nullable = jdbcTemplate.queryForObject("""
                SELECT is_nullable
                FROM information_schema.columns
                WHERE lower(table_schema) = 'task'
                  AND lower(table_name) = lower(?)
                  AND lower(column_name) = lower(?)
                """, String.class, tableName, columnName);

        assertThat(nullable).isEqualTo("NO");
    }

    private void assertPrimaryKeyColumns(String tableName, List<String> expectedColumnNames) {
        List<String> primaryKeyColumns = jdbcTemplate.queryForList("""
                SELECT kcu.column_name
                FROM information_schema.table_constraints tc
                JOIN information_schema.key_column_usage kcu
                  ON tc.constraint_schema = kcu.constraint_schema
                 AND tc.constraint_name = kcu.constraint_name
                 AND tc.table_schema = kcu.table_schema
                 AND tc.table_name = kcu.table_name
                WHERE lower(tc.table_schema) = 'task'
                  AND lower(tc.table_name) = lower(?)
                  AND lower(tc.constraint_type) = 'primary key'
                ORDER BY kcu.ordinal_position
                """, String.class, tableName);

        assertThat(primaryKeyColumns).containsExactlyElementsOf(expectedColumnNames);
    }

    private void assertForeignKeyDeleteRule(
            String tableName,
            String constraintName,
            String referencedTableName,
            String deleteRule
    ) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT count(*)
                FROM information_schema.table_constraints tc
                JOIN information_schema.referential_constraints rc
                  ON tc.constraint_schema = rc.constraint_schema
                 AND tc.constraint_name = rc.constraint_name
                JOIN information_schema.constraint_column_usage ccu
                  ON rc.unique_constraint_schema = ccu.constraint_schema
                 AND rc.unique_constraint_name = ccu.constraint_name
                WHERE lower(tc.table_schema) = 'task'
                  AND lower(tc.table_name) = lower(?)
                  AND lower(tc.constraint_name) = lower(?)
                  AND lower(tc.constraint_type) = 'foreign key'
                  AND lower(ccu.table_schema) = 'task'
                  AND lower(ccu.table_name) = lower(?)
                  AND upper(rc.delete_rule) = upper(?)
                """, Integer.class, tableName, constraintName, referencedTableName, deleteRule);

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

    private void assertIndexDoesNotExist(String indexName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT count(*)
                FROM pg_indexes
                WHERE lower(schemaname) = 'task'
                  AND lower(indexname) = lower(?)
                """, Integer.class, indexName);

        assertThat(count).isZero();
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

    private void assertIndexColumns(String indexName, List<String> expectedColumnNames) {
        List<String> indexColumns = jdbcTemplate.queryForList("""
                SELECT attribute.attname
                FROM pg_class index_class
                JOIN pg_namespace namespace
                  ON namespace.oid = index_class.relnamespace
                JOIN pg_index index_metadata
                  ON index_metadata.indexrelid = index_class.oid
                JOIN LATERAL unnest(index_metadata.indkey) WITH ORDINALITY AS indexed_column(attnum, position)
                  ON TRUE
                JOIN pg_attribute attribute
                  ON attribute.attrelid = index_metadata.indrelid
                 AND attribute.attnum = indexed_column.attnum
                WHERE lower(namespace.nspname) = 'task'
                  AND lower(index_class.relname) = lower(?)
                ORDER BY indexed_column.position
                """, String.class, indexName);

        assertThat(indexColumns).containsExactlyElementsOf(expectedColumnNames);
    }

    private void assertTaskTagCount(UUID taskId, UUID tagId, long expectedCount) {
        Long taskTagCount = jdbcTemplate.queryForObject("""
                SELECT count(*)
                FROM task.task_tags
                WHERE task_id = ?
                  AND tag_id = ?
                """, Long.class, taskId, tagId);

        assertThat(taskTagCount).isEqualTo(expectedCount);
    }
}
