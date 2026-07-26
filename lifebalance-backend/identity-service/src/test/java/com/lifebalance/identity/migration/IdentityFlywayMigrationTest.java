package com.lifebalance.identity.migration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "eureka.client.enabled=false"
})
class IdentityFlywayMigrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
}
