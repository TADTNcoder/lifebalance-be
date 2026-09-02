package com.lifebalance.finance;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/.well-known/jwks.json"
})
@ActiveProfiles("test")
class FinanceServiceApplicationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    @Transactional
    void databaseRejectsASecondActiveLifetimeMainPoolForTheSameOwner() {
        UUID ownerId = UUID.randomUUID();
        insertMainPool(UUID.randomUUID(), ownerId, "Ví tổng");

        assertThatThrownBy(() -> insertMainPool(UUID.randomUUID(), ownerId, "Ví tổng khác"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private void insertMainPool(UUID accountId, UUID ownerId, String name) {
        jdbcTemplate.update("""
                insert into finance.finance_accounts (
                    id, owner_id, name, account_type, currency_code, status
                ) values (?, ?, ?, 'MAIN_POOL', 'VND', 'ACTIVE')
                """, accountId, ownerId, name);
    }

}
