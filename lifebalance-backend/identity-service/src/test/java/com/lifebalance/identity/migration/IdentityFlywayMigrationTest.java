package com.lifebalance.identity.migration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "eureka.client.enabled=false"
})
class IdentityFlywayMigrationTest {

    private static final List<String> DEFAULT_PERMISSION_CODES = List.of(
            "activity-log:read",
            "administration-dashboard:read",
            "announcement:create",
            "announcement:read",
            "audit:export",
            "audit:read",
            "configuration:read",
            "configuration:update",
            "maintenance:read",
            "maintenance:update",
            "permission:create",
            "permission:delete",
            "permission:read",
            "permission:update",
            "profile:read",
            "profile:update",
            "role:assign",
            "role:create",
            "role:delete",
            "role:read",
            "role:update",
            "support-ticket:assign",
            "support-ticket:create",
            "support-ticket:read",
            "support-ticket:resolve",
            "support-ticket:update",
            "user:create",
            "user:delete",
            "user:lock",
            "user:read",
            "user:unlock",
            "user:update"
    );

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void flywayCreatesUserProfileColumns() {
        assertColumnExists("users", "phone");
        assertColumnExists("users", "gender");
        assertColumnExists("users", "birth_date");
    }

    @Test
    void flywayCreatesRoleSchemaAndIndexes() {
        assertTableExists("roles");
        assertTableExists("role_permissions");
        assertIndexExists("uq_identity_roles_name_active");
        assertIndexExists("idx_identity_roles_is_system_code_active");
    }

    @Test
    void flywayCreatesAuditLogSchemaIndexesAndConstraints() {
        assertTableExists("audit_logs");

        assertColumnExists("audit_logs", "entity_name");
        assertColumnExists("audit_logs", "entity_id");
        assertColumnExists("audit_logs", "actor_id");
        assertColumnExists("audit_logs", "actor_keycloak_id");
        assertColumnExists("audit_logs", "actor_username");
        assertColumnExists("audit_logs", "old_value");
        assertColumnExists("audit_logs", "new_value");

        assertIndexExists("idx_identity_audit_logs_entity");
        assertIndexExists("idx_identity_audit_logs_actor");
        assertIndexExists("idx_identity_audit_logs_action_created_at");
        assertIndexExists("idx_identity_audit_logs_status_created_at");

        assertCheckConstraintExists("chk_identity_audit_logs_entity_name");
        assertCheckConstraintExists("chk_identity_audit_logs_action");
        assertCheckConstraintExists("chk_identity_audit_logs_status");

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO identity.audit_logs (entity_name, action, status)
                VALUES ('INVALID_ENTITY', 'LOGIN', 'SUCCESS')
                """))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void flywaySeedsDefaultPermissionsAndRoleMappings() {
        List<String> permissionCodes = jdbcTemplate.queryForList("""
                SELECT code
                FROM identity.permissions
                WHERE is_system = true
                  AND deleted_at IS NULL
                ORDER BY code
                """, String.class);

        assertThat(permissionCodes).containsExactlyElementsOf(DEFAULT_PERMISSION_CODES);

        List<String> adminPermissionCodes = findPermissionCodesByRoleCode("admin");
        assertThat(adminPermissionCodes).containsExactlyElementsOf(DEFAULT_PERMISSION_CODES);

        List<String> userPermissionCodes = findPermissionCodesByRoleCode("user");
        assertThat(userPermissionCodes).containsExactly(
                "announcement:read",
                "maintenance:read",
                "profile:read",
                "profile:update",
                "support-ticket:create",
                "support-ticket:read"
        );
        assertThat(userPermissionCodes).doesNotContain("user:update", "user:delete");

        List<String> managerPermissionCodes = findPermissionCodesByRoleCode("manager");
        assertThat(managerPermissionCodes).contains(
                "support-ticket:create",
                "support-ticket:read",
                "support-ticket:update",
                "support-ticket:assign",
                "support-ticket:resolve",
                "activity-log:read",
                "administration-dashboard:read",
                "announcement:read",
                "maintenance:read"
        );
        assertThat(managerPermissionCodes).doesNotContain("configuration:update", "permission:update");
    }

    @Test
    void flywayCreatesAdministrationSupportSchema() {
        assertTableExists("support_tickets");
        assertTableExists("support_ticket_history");
        assertTableExists("activity_logs");
        assertTableExists("system_configurations");
        assertTableExists("system_announcements");

        assertColumnExists("support_tickets", "requester_id");
        assertColumnExists("support_tickets", "assignee_id");
        assertColumnExists("support_ticket_history", "action");
        assertColumnExists("activity_logs", "category");
        assertColumnExists("system_configurations", "requires_confirmation");
        assertColumnExists("system_announcements", "audience");

        assertIndexExists("idx_identity_support_tickets_status");
        assertIndexExists("idx_identity_ticket_history_ticket");
        assertIndexExists("idx_identity_activity_logs_category");
        assertIndexExists("idx_identity_system_configurations_key");
        assertIndexExists("idx_identity_announcements_status");

        Integer configurationCount = jdbcTemplate.queryForObject("""
                SELECT count(*)
                FROM identity.system_configurations
                WHERE config_key IN (
                    'announcement.policy.enabled',
                    'maintenance.policy.enabled',
                    'maintenance.mode.enabled',
                    'maintenance.message'
                )
                """, Integer.class);
        assertThat(configurationCount).isEqualTo(4);
    }

    private void assertTableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT count(*)
                FROM information_schema.tables
                WHERE lower(table_schema) = 'identity'
                  AND lower(table_name) = lower(?)
                """, Integer.class, tableName);

        assertThat(count).isEqualTo(1);
    }

    private void assertColumnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT count(*)
                FROM information_schema.columns
                WHERE lower(table_schema) = 'identity'
                  AND lower(table_name) = lower(?)
                  AND lower(column_name) = lower(?)
                """, Integer.class, tableName, columnName);

        assertThat(count).isEqualTo(1);
    }

    private void assertIndexExists(String indexName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT count(*)
                FROM information_schema.indexes
                WHERE lower(table_schema) = 'identity'
                  AND lower(index_name) = lower(?)
                """, Integer.class, indexName);

        assertThat(count).isEqualTo(1);
    }

    private void assertCheckConstraintExists(String constraintName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT count(*)
                FROM information_schema.table_constraints
                WHERE lower(constraint_schema) = 'identity'
                  AND lower(constraint_name) = lower(?)
                  AND lower(constraint_type) = 'check'
                """, Integer.class, constraintName);

        assertThat(count).isEqualTo(1);
    }

    private List<String> findPermissionCodesByRoleCode(String roleCode) {
        return jdbcTemplate.queryForList("""
                SELECT permission.code
                FROM identity.roles role
                JOIN identity.role_permissions role_permission ON role_permission.role_id = role.id
                JOIN identity.permissions permission ON permission.id = role_permission.permission_id
                WHERE lower(role.code) = lower(?)
                  AND role.deleted_at IS NULL
                  AND permission.deleted_at IS NULL
                ORDER BY permission.code
                """, String.class, roleCode);
    }
}
