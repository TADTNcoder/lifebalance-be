package com.lifebalance.task.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;

import com.lifebalance.task.dto.request.CreateTaskRequest;
import com.lifebalance.task.dto.response.TaskResponse;
import com.lifebalance.task.model.enums.PriorityLevel;
import com.lifebalance.task.service.TaskService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
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

    @Autowired
    private TaskService taskService;

    @PersistenceContext
    private EntityManager entityManager;

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
        assertColumnExists("tags", "description", "text");
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
        assertColumnExists("task_tags", "assigned_at", "timestamp with time zone");
        assertColumnIsNotNullable("task_tags", "task_id");
        assertColumnIsNotNullable("task_tags", "tag_id");
        assertColumnIsNotNullable("task_tags", "created_at");
        assertColumnIsNotNullable("task_tags", "assigned_at");
        assertPrimaryKeyColumns("task_tags", List.of("task_id", "tag_id"));
        assertForeignKeyDeleteRule("task_tags", "fk_task_tags_task", "tasks", "CASCADE");
        assertForeignKeyDeleteRule("task_tags", "fk_task_tags_tag", "tags", "CASCADE");
        assertIndexDoesNotExist("idx_task_tags_tag_id");
        assertIndexExists("idx_task_tags_tag_task");
        assertIndexColumns("idx_task_tags_tag_task", List.of("tag_id", "task_id"));
        assertIndexExists("idx_task_tags_task_assigned");
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

    // =========================================================================
    // GIỮ LẠI: Các Test Case về Timeline và History của nhánh feature
    // =========================================================================
    @Test
    void flywayAddsTaskPlanningTraceFieldsAndTimelineIndexes() {
        assertColumnExists("tasks", "planned_start_at", "timestamp with time zone");
        assertColumnExists("tasks", "planned_end_at", "timestamp with time zone");
        assertColumnExists("tasks", "scheduled_start_at", "timestamp with time zone");
        assertColumnExists("tasks", "scheduled_end_at", "timestamp with time zone");
        assertColumnExists("tasks", "completed_at", "timestamp with time zone");
        assertColumnExists("tasks", "cancelled_at", "timestamp with time zone");
        assertColumnExists("tasks", "archived_at", "timestamp with time zone");
        assertColumnExists("tasks", "created_by", "uuid");
        assertColumnExists("tasks", "updated_by", "uuid");
        assertConstraintExists("tasks", "chk_tasks_planned_window");
        assertConstraintExists("tasks", "chk_tasks_scheduled_window");
        assertIndexExists("idx_tasks_owner_status_deadline");
        assertIndexExists("idx_tasks_owner_scheduled_start");
        assertIndexExists("idx_tasks_owner_updated");
    }

    @Test
    void flywayCreatesTimelinePlacementAndHistoryStorage() {
        assertTableExists("timeline_placements");
        assertColumnExists("timeline_placements", "owner_id", "uuid");
        assertColumnExists("timeline_placements", "task_id", "uuid");
        assertColumnExists("timeline_placements", "start_at", "timestamp with time zone");
        assertColumnExists("timeline_placements", "end_at", "timestamp with time zone");
        assertColumnExists("timeline_placements", "status", "character varying");
        assertColumnExists("timeline_placements", "created_by", "uuid");
        assertColumnExists("timeline_placements", "updated_by", "uuid");
        assertColumnIsNotNullable("timeline_placements", "owner_id");
        assertColumnIsNotNullable("timeline_placements", "task_id");
        assertForeignKeyDeleteRule("timeline_placements", "fk_timeline_placements_task", "tasks", "RESTRICT");
        assertConstraintExists("timeline_placements", "chk_timeline_placements_window");
        assertIndexExists("idx_timeline_placements_owner_time");
        assertIndexExists("idx_timeline_placements_owner_status_time");

        assertTableExists("task_change_histories");
        assertColumnExists("task_change_histories", "owner_id", "uuid");
        assertColumnExists("task_change_histories", "actor_id", "uuid");
        assertColumnExists("task_change_histories", "task_id", "uuid");
        assertColumnExists("task_change_histories", "timeline_placement_id", "uuid");
        assertColumnExists("task_change_histories", "action_type", "character varying");
        assertColumnExists("task_change_histories", "occurred_at", "timestamp with time zone");
        assertColumnExists("task_change_histories", "created_at", "timestamp with time zone");
        assertForeignKeyDeleteRule("task_change_histories", "fk_task_change_histories_task", "tasks", "RESTRICT");
        assertForeignKeyDeleteRule(
                "task_change_histories",
                "fk_task_change_histories_timeline_placement",
                "timeline_placements",
                "SET NULL"
        );
        assertConstraintExists("task_change_histories", "chk_task_change_histories_action");
        assertIndexExists("idx_task_change_histories_owner_time");
        assertIndexExists("idx_task_change_histories_task_time");
        assertIndexExists("idx_task_change_histories_action_time");
        assertIndexExists("idx_task_change_histories_owner_action_time");
    }

    @Test
    void flywayAllowsBackendServiceHistoryActions() {
        UUID userId = UUID.randomUUID();
        UUID taskId = insertTask(userId, "History action extension");

        jdbcTemplate.update("""
                INSERT INTO task.task_change_histories (owner_id, actor_id, task_id, action_type, field_name)
                VALUES (?, ?, ?, 'TASK_REMINDER_CREATED', 'reminder')
                """, userId, userId, taskId);

        Long count = jdbcTemplate.queryForObject("""
                SELECT count(*)
                FROM task.task_change_histories
                WHERE owner_id = ?
                  AND task_id = ?
                  AND action_type = 'TASK_REMINDER_CREATED'
                """, Long.class, userId, taskId);

        assertThat(count).isEqualTo(1L);
    }

    // =========================================================================
    // GIỮ LẠI: Các Test Case chuẩn chỉnh đã được update từ nhánh Main
    // =========================================================================
    @Test
    void flywayCreatesRecurringRuleAndReminderStorageWithoutEnablingOptionalFeature() {
        assertTableExists("task_feature_policy_approvals");
        assertColumnExists("task_feature_policy_approvals", "owner_id", "uuid");
        assertColumnExists("task_feature_policy_approvals", "task_id", "uuid");
        assertColumnExists("task_feature_policy_approvals", "feature_code", "character varying");
        assertColumnExists("task_feature_policy_approvals", "approval_status", "character varying");
        assertColumnExists("task_feature_policy_approvals", "requested_at", "timestamp with time zone");
        assertColumnExists("task_feature_policy_approvals", "decided_at", "timestamp with time zone");
        assertColumnExists("task_feature_policy_approvals", "decision_reason", "character varying");
        assertColumnIsNotNullable("task_feature_policy_approvals", "owner_id");
        assertColumnIsNotNullable("task_feature_policy_approvals", "feature_code");
        assertColumnIsNotNullable("task_feature_policy_approvals", "approval_status");
        assertCheckConstraintExists("chk_task_feature_policy_feature_code");
        assertCheckConstraintExists("chk_task_feature_policy_status");
        assertCheckConstraintExists("chk_task_feature_policy_decision_state");

        assertTableExists("task_recurring_rules");
        assertColumnExists("task_recurring_rules", "owner_id", "uuid");
        assertColumnExists("task_recurring_rules", "task_id", "uuid");
        assertColumnExists("task_recurring_rules", "policy_approval_id", "uuid");
        assertColumnExists("task_recurring_rules", "policy_feature_code", "character varying");
        assertColumnExists("task_recurring_rules", "policy_approval_status", "character varying");
        assertColumnExists("task_recurring_rules", "rule_status", "character varying");
        assertColumnExists("task_recurring_rules", "frequency", "character varying");
        assertColumnExists("task_recurring_rules", "interval_count", "integer");
        assertColumnExists("task_recurring_rules", "next_run_at", "timestamp with time zone");
        assertCheckConstraintExists("chk_task_recurring_rules_policy_feature");
        assertCheckConstraintExists("chk_task_recurring_rules_policy_status");
        assertCheckConstraintExists("chk_task_recurring_rules_active_schedule");

        assertTableExists("task_reminders");
        assertColumnExists("task_reminders", "owner_id", "uuid");
        assertColumnExists("task_reminders", "task_id", "uuid");
        assertColumnExists("task_reminders", "recurring_rule_id", "uuid");
        assertColumnExists("task_reminders", "policy_approval_id", "uuid");
        assertColumnExists("task_reminders", "policy_feature_code", "character varying");
        assertColumnExists("task_reminders", "policy_approval_status", "character varying");
        assertColumnExists("task_reminders", "reminder_status", "character varying");
        assertColumnExists("task_reminders", "reminder_kind", "character varying");
        assertColumnExists("task_reminders", "remind_at", "timestamp with time zone");
        assertCheckConstraintExists("chk_task_reminders_policy_feature");
        assertCheckConstraintExists("chk_task_reminders_policy_status");
        assertCheckConstraintExists("chk_task_reminders_sent_state");

        assertIndexExists("idx_task_feature_policy_owner_feature_status");
        assertIndexExists("idx_task_recurring_rules_owner_status_next_run");
        assertIndexExists("idx_task_reminders_owner_status_remind_at");
    }

    @Test
    void recurringRulesAndRemindersRequireApprovedPolicyDecision() {
        UUID userId = UUID.randomUUID();
        UUID taskId = insertTask(userId, "Policy gated task");
        UUID pendingRecurringPolicyId = insertFeaturePolicy(userId, taskId, "RECURRING_RULE", "PENDING");

        assertThatThrownBy(() -> insertRecurringRule(userId, taskId, pendingRecurringPolicyId))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("fk_task_recurring_rules_policy_approved");

        UUID approvedRecurringPolicyId = insertFeaturePolicy(userId, taskId, "RECURRING_RULE", "APPROVED");
        UUID recurringRuleId = insertRecurringRule(userId, taskId, approvedRecurringPolicyId);

        UUID pendingReminderPolicyId = insertFeaturePolicy(userId, taskId, "REMINDER", "PENDING");
        assertThatThrownBy(() -> insertReminder(userId, taskId, recurringRuleId, pendingReminderPolicyId))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("fk_task_reminders_policy_approved");

        UUID approvedReminderPolicyId = insertFeaturePolicy(userId, taskId, "REMINDER", "APPROVED");
        UUID reminderId = insertReminder(userId, taskId, recurringRuleId, approvedReminderPolicyId);

        assertThat(recurringRuleId).isNotNull();
        assertThat(reminderId).isNotNull();
    }

    @Test
    void v11DoesNotSeedOptionalFeatureRowsOnFreshDatabase() {
        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.4-alpine")
                .withDatabaseName("task_v11_optional_features")
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
                    .load()
                    .migrate();

            assertThat(countRows(jdbc, "task_feature_policy_approvals")).isZero();
            assertThat(countRows(jdbc, "task_recurring_rules")).isZero();
            assertThat(countRows(jdbc, "task_reminders")).isZero();
        }
    }

    // =========================================================================
    // CÁC TEST CŨ CỦA DỰ ÁN GIỮ NGUYÊN (Không bị conflict)
    // =========================================================================
    @Test
    void flywayExtendsCategoriesTableForDefaultCategoryMetadata() {
        assertTableExists("categories");
        assertColumnExists("categories", "owner_id", "uuid");
        assertColumnExists("categories", "slug", "character varying");
        assertColumnExists("categories", "color", "character varying");
        assertColumnExists("categories", "icon", "character varying");
        assertColumnExists("categories", "is_system", "boolean");
        assertColumnIsNotNullable("categories", "slug");
        assertColumnIsNotNullable("categories", "is_system");
        assertIndexDoesNotExist("uq_categories_slug_active");
        assertIndexExists("uq_categories_owner_name_active");
        assertIndexExists("uq_categories_owner_slug_active");
        assertIndexExists("idx_categories_visible_owner_name");
        assertIndexPredicate("uq_categories_owner_name_active", "(owner_id IS NOT NULL)");
        assertIndexPredicate("uq_categories_owner_name_active", "(deleted_at IS NULL)");
        assertIndexPredicate("uq_categories_owner_slug_active", "(owner_id IS NOT NULL)");
        assertIndexPredicate("uq_categories_owner_slug_active", "(deleted_at IS NULL)");
    }

    @Test
    void categoryNamesAndSlugsAreUniquePerOwnerInsteadOfGlobally() {
        UUID firstOwnerId = UUID.randomUUID();
        UUID secondOwnerId = UUID.randomUUID();

        jdbcTemplate.update("""
                INSERT INTO task.categories (owner_id, name, slug, is_system)
                VALUES (?, 'Dự án', 'du-an', false)
                """, firstOwnerId);
        jdbcTemplate.update("""
                INSERT INTO task.categories (owner_id, name, slug, is_system)
                VALUES (?, 'Dự án', 'du-an', false)
                """, secondOwnerId);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO task.categories (owner_id, name, slug, is_system)
                VALUES (?, '  DỰ ÁN  ', 'DU-AN', false)
                """, firstOwnerId))
                .isInstanceOf(DataIntegrityViolationException.class);
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

    @Test
    void flywayAddsTaskNoteForEditRoundTrip() {
        assertColumnExists("tasks", "note", "character varying");
        assertColumnExists("tasks", "currency", "character varying");
    }

    @Test
    void taskSchemaDoesNotCreateCrossServiceIdentityForeignKeys() {
        Integer foreignKeyCount = jdbcTemplate.queryForObject("""
                SELECT count(*)
                FROM pg_constraint source_constraint
                JOIN pg_class source_table
                    ON source_table.oid = source_constraint.conrelid
                JOIN pg_namespace source_namespace
                    ON source_namespace.oid = source_table.relnamespace
                WHERE source_constraint.contype = 'f'
                  AND source_namespace.nspname = 'task'
                  AND source_constraint.confrelid = to_regclass('identity.users')
                """, Integer.class);

        assertThat(foreignKeyCount).isZero();
    }

    @Test
    @Transactional
    void tasksAcceptAuthenticatedOwnerWithoutLocalIdentityReplica() {
        UUID ownerId = UUID.randomUUID();
        CreateTaskRequest request = new CreateTaskRequest();
        request.setName("Owner without local identity " + ownerId);
        request.setPriority(PriorityLevel.LOW);

        TaskResponse response = taskService.create(ownerId, request);
        entityManager.flush();
        Long historyCount = jdbcTemplate.queryForObject("""
                SELECT count(*)
                FROM task.task_change_histories
                WHERE task_id = ?
                  AND owner_id = ?
                  AND actor_id = ?
                """, Long.class, response.getId(), ownerId, ownerId);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getOwnerId()).isEqualTo(ownerId);
        assertThat(historyCount).isEqualTo(1L);
    }

    // =========================================================================
    // HELPER METHODS (Các hàm hỗ trợ được giữ nguyên)
    // =========================================================================
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

    private UUID insertFeaturePolicy(UUID userId, UUID taskId, String featureCode, String approvalStatus) {
        if ("PENDING".equals(approvalStatus)) {
            return jdbcTemplate.queryForObject("""
                    INSERT INTO task.task_feature_policy_approvals (
                        owner_id,
                        task_id,
                        feature_code,
                        approval_status,
                        requested_by
                    )
                    VALUES (?, ?, ?, ?, ?)
                    RETURNING id
                    """, UUID.class, userId, taskId, featureCode, approvalStatus, userId);
        }

        return jdbcTemplate.queryForObject("""
                INSERT INTO task.task_feature_policy_approvals (
                    owner_id,
                    task_id,
                    feature_code,
                    approval_status,
                    requested_by,
                    decided_at,
                    decided_by,
                    decision_reason
                )
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?)
                RETURNING id
                """, UUID.class, userId, taskId, featureCode, approvalStatus, userId, userId, "Approved for test");
    }

    private UUID insertRecurringRule(UUID userId, UUID taskId, UUID policyApprovalId) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO task.task_recurring_rules (
                    owner_id,
                    task_id,
                    policy_approval_id,
                    rule_status,
                    frequency,
                    starts_at,
                    next_run_at,
                    created_by,
                    updated_by
                )
                VALUES (?, ?, ?, 'ACTIVE', 'WEEKLY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '1 day', ?, ?)
                RETURNING id
                """, UUID.class, userId, taskId, policyApprovalId, userId, userId);
    }

    private UUID insertReminder(UUID userId, UUID taskId, UUID recurringRuleId, UUID policyApprovalId) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO task.task_reminders (
                    owner_id,
                    task_id,
                    recurring_rule_id,
                    policy_approval_id,
                    reminder_kind,
                    remind_at,
                    created_by,
                    updated_by
                )
                VALUES (?, ?, ?, ?, 'RECURRING_INSTANCE', CURRENT_TIMESTAMP + INTERVAL '1 hour', ?, ?)
                RETURNING id
                """, UUID.class, userId, taskId, recurringRuleId, policyApprovalId, userId, userId);
    }

    private UUID insertTask(UUID userId, String name) {
        jdbcTemplate.update("INSERT INTO identity.users (id) VALUES (?) ON CONFLICT (id) DO NOTHING", userId);

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

    private long countRows(JdbcTemplate jdbc, String tableName) {
        Long count = jdbc.queryForObject("SELECT count(*) FROM task." + tableName, Long.class);
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

    private void assertCheckConstraintExists(String constraintName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT count(*)
                FROM information_schema.table_constraints
                WHERE lower(constraint_schema) = 'task'
                  AND lower(constraint_name) = lower(?)
                  AND lower(constraint_type) = 'check'
                """, Integer.class, constraintName);

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

    private void assertConstraintExists(String tableName, String constraintName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT count(*)
                FROM pg_constraint constraint_metadata
                JOIN pg_class table_metadata
                  ON table_metadata.oid = constraint_metadata.conrelid
                JOIN pg_namespace namespace
                  ON namespace.oid = table_metadata.relnamespace
                WHERE lower(namespace.nspname) = 'task'
                  AND lower(table_metadata.relname) = lower(?)
                  AND lower(constraint_metadata.conname) = lower(?)
                """, Integer.class, tableName, constraintName);

        assertThat(count).isEqualTo(1);
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
