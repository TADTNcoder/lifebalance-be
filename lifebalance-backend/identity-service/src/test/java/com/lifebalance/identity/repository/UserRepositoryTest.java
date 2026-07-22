package com.lifebalance.identity.repository;

import com.lifebalance.identity.model.Permission;
import com.lifebalance.identity.model.Role;
import com.lifebalance.identity.model.RolePermission;
import com.lifebalance.identity.model.RolePermissionId;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.model.UserRole;
import com.lifebalance.identity.model.UserRoleId;
import com.lifebalance.identity.model.enums.AccountStatus;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "eureka.client.enabled=false"
})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesAndFindsUserById() {
        User user = userRepository.saveAndFlush(User.builder()
                .email("save-read@example.com")
                .username("save-reader")
                .displayName("Save Reader")
                .keycloakId("kc-save-read")
                .build());

        entityManager.clear();

        assertThat(userRepository.findById(user.getId()))
                .isPresent()
                .get()
                .satisfies(found -> {
                    assertThat(found.getEmail()).isEqualTo("save-read@example.com");
                    assertThat(found.getUsername()).isEqualTo("save-reader");
                    assertThat(found.getDisplayName()).isEqualTo("Save Reader");
                    assertThat(found.getKeycloakId()).isEqualTo("kc-save-read");
                });
    }

    @Test
    void findsUserByEmailUsernameAndKeycloakId() {
        User user = userRepository.saveAndFlush(User.builder()
                .email("Lookup.Person@Example.COM")
                .username("LookupPerson")
                .keycloakId("kc-lookup-person")
                .build());

        entityManager.clear();

        assertThat(userRepository.findByEmail("LOOKUP.PERSON@example.com"))
                .isPresent()
                .get()
                .extracting(User::getId)
                .isEqualTo(user.getId());
        assertThat(userRepository.findByUsername("lookupPERSON"))
                .isPresent()
                .get()
                .extracting(User::getId)
                .isEqualTo(user.getId());
        assertThat(userRepository.findByKeycloakId("kc-lookup-person"))
                .isPresent()
                .get()
                .extracting(User::getId)
                .isEqualTo(user.getId());
    }

    @Test
    void detectsDuplicateIdentifiersExcludingCurrentUser() {
        User first = userRepository.save(User.builder()
                .email("first-duplicate@example.com")
                .username("first-duplicate")
                .build());
        User second = userRepository.saveAndFlush(User.builder()
                .email("second-duplicate@example.com")
                .username("second-duplicate")
                .build());

        assertThat(userRepository.existsByEmail("FIRST-DUPLICATE@example.com")).isTrue();
        assertThat(userRepository.existsByUsername("FIRST-DUPLICATE")).isTrue();
        assertThat(userRepository.existsByEmailAndIdNot("first-duplicate@example.com", first.getId())).isFalse();
        assertThat(userRepository.existsByUsernameAndIdNot("first-duplicate", first.getId())).isFalse();
        assertThat(userRepository.existsByEmailAndIdNot("first-duplicate@example.com", second.getId())).isTrue();
        assertThat(userRepository.existsByUsernameAndIdNot("first-duplicate", second.getId())).isTrue();
    }

    @Test
    void updatesUserWithSave() {
        User user = userRepository.saveAndFlush(User.builder()
                .email("before-update@example.com")
                .username("before-update")
                .displayName("Before Update")
                .build());

        user.setEmail("After-Update@Example.COM");
        user.setUsername("AfterUpdate");
        user.setDisplayName("After Update");
        userRepository.saveAndFlush(user);
        entityManager.clear();

        assertThat(userRepository.findById(user.getId()))
                .isPresent()
                .get()
                .satisfies(found -> {
                    assertThat(found.getEmail()).isEqualTo("after-update@example.com");
                    assertThat(found.getUsername()).isEqualTo("afterupdate");
                    assertThat(found.getDisplayName()).isEqualTo("After Update");
                });
    }

    @Test
    void excludesSoftDeletedUsersFromRepositoryQueries() {
        User user = userRepository.saveAndFlush(User.builder()
                .email("soft-deleted-repository@example.com")
                .username("soft-deleted-repository")
                .keycloakId("kc-soft-deleted-repository")
                .build());

        userRepository.delete(user);
        userRepository.flush();
        entityManager.clear();

        assertThat(userRepository.findById(user.getId())).isEmpty();
        assertThat(userRepository.findByEmail("soft-deleted-repository@example.com")).isEmpty();
        assertThat(userRepository.findByUsername("soft-deleted-repository")).isEmpty();
        assertThat(userRepository.findByKeycloakId("kc-soft-deleted-repository")).isEmpty();
    }

    @Test
    void softDeleteMarksUserAsDeletedInDatabase() {
        User user = userRepository.saveAndFlush(User.builder()
                .email("soft-delete-status@example.com")
                .username("soft-delete-status")
                .keycloakId("kc-soft-delete-status")
                .build());

        userRepository.delete(user);
        userRepository.flush();
        entityManager.clear();

        Object[] row = (Object[]) entityManager.createNativeQuery("""
                        SELECT status, deleted_at
                        FROM identity.users
                        WHERE id = ?
                        """)
                .setParameter(1, user.getId())
                .getSingleResult();

        assertThat(row[0]).isEqualTo("DELETED");
        assertThat(row[1]).isNotNull();
        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

    @Test
    void detectsUsersIncludingSoftDeletedRecords() {
        User user = userRepository.saveAndFlush(User.builder()
                .email("deleted-detection@example.com")
                .username("deleted-detection")
                .keycloakId("kc-deleted-detection")
                .build());

        userRepository.delete(user);
        userRepository.flush();
        entityManager.clear();

        assertThat(userRepository.existsByIdIncludingDeleted(user.getId())).isTrue();
        assertThat(userRepository.existsDeletedById(user.getId())).isTrue();
        assertThat(userRepository.existsDeletedByKeycloakId("kc-deleted-detection")).isTrue();
        assertThat(userRepository.existsDeletedByKeycloakId("missing-keycloak-id")).isFalse();
    }

    @Test
    void savesDisabledUserStatus() {
        User user = userRepository.saveAndFlush(User.builder()
                .email("disabled-status@example.com")
                .username("disabled-status")
                .status(AccountStatus.DISABLED)
                .build());

        entityManager.clear();

        assertThat(userRepository.findById(user.getId()))
                .isPresent()
                .get()
                .extracting(User::getStatus)
                .isEqualTo(AccountStatus.DISABLED);
    }

    @Test
    void findsRoleAndPermissionCodesByUserId() {
        User user = userRepository.saveAndFlush(User.builder()
                .email("authorized-user@example.com")
                .username("authorized-user")
                .build());
        Role role = Role.builder()
                .code("task-owner")
                .name("Task Owner")
                .build();
        Permission permission = Permission.builder()
                .code("task:read")
                .name("Read Tasks")
                .module("task")
                .build();

        entityManager.persist(role);
        entityManager.persist(permission);
        entityManager.flush();

        entityManager.persist(UserRole.builder()
                .id(new UserRoleId(user.getId(), role.getId()))
                .user(user)
                .role(role)
                .assignedAt(OffsetDateTime.now())
                .build());
        entityManager.persist(RolePermission.builder()
                .id(new RolePermissionId(role.getId(), permission.getId()))
                .role(role)
                .permission(permission)
                .grantedAt(OffsetDateTime.now())
                .build());
        entityManager.flush();
        entityManager.clear();

        assertThat(userRepository.findRoleCodesByUserId(user.getId()))
                .containsExactly("task-owner");
        assertThat(userRepository.findPermissionCodesByUserId(user.getId()))
                .containsExactly("task:read");
    }
}
