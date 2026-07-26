package com.lifebalance.identity.migration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

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

    private void assertTableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT count(*)
                FROM information_schema.tables
                WHERE lower(table_schema) = 'identity'
                  AND lower(table_name) = lower(?)
                """, Integer.class, tableName);

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
}
