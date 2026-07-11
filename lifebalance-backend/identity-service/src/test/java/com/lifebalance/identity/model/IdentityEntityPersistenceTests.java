package com.lifebalance.identity.model;

import com.lifebalance.identity.model.enums.AccountStatus;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "eureka.client.enabled=false"
})
class IdentityEntityPersistenceTests {

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsUserWithNormalizedIdentifiersAndDefaults() {
        User user = User.builder()
                .email("  Person@Example.COM  ")
                .username("  PersonOne  ")
                .displayName("Person One")
                .build();

        entityManager.persist(user);
        entityManager.flush();

        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail()).isEqualTo("person@example.com");
        assertThat(user.getUsername()).isEqualTo("personone");
        assertThat(user.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(user.getRegisteredAt()).isNotNull();
        assertThat(user.getCreatedAt()).isNotNull();
    }

    @Test
    void softDeletesUserInsteadOfRemovingRow() {
        User user = User.builder()
                .email("delete-me@example.com")
                .build();

        entityManager.persist(user);
        entityManager.flush();
        UUID id = user.getId();

        entityManager.remove(user);
        entityManager.flush();
        entityManager.clear();

        assertThat(entityManager.find(User.class, id)).isNull();

        Object deletedAt = entityManager
                .createNativeQuery("SELECT deleted_at FROM identity.users WHERE id = ?")
                .setParameter(1, id)
                .getSingleResult();

        assertThat(deletedAt).isNotNull();
    }
}
