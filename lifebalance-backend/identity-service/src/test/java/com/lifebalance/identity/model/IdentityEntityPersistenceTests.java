package com.lifebalance.identity.model;

import com.lifebalance.identity.model.enums.AccountStatus;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.OffsetDateTime;
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

    @Test
    void persistsPermissionWithNormalizedIdentifiersAndDefaults() {
        Permission permission = Permission.builder()
                .code("  User:Read  ")
                .name("Read Users")
                .module("  User_Management  ")
                .description("Allows reading user profiles")
                .build();

        entityManager.persist(permission);
        entityManager.flush();

        assertThat(permission.getId()).isNotNull();
        assertThat(permission.getCode()).isEqualTo("user:read");
        assertThat(permission.getName()).isEqualTo("Read Users");
        assertThat(permission.getModule()).isEqualTo("user_management");
        assertThat(permission.getDescription()).isEqualTo("Allows reading user profiles");
        assertThat(permission.getSystem()).isFalse();
        assertThat(permission.getRolePermissions()).isEmpty();
        assertThat(permission.getCreatedAt()).isNotNull();
    }

    @Test
    void persistsSystemPermissionFlag() {
        Permission permission = Permission.builder()
                .code("role:assign")
                .name("Assign Roles")
                .module("identity")
                .system(true)
                .build();

        entityManager.persist(permission);
        entityManager.flush();
        entityManager.clear();

        Permission found = entityManager.find(Permission.class, permission.getId());

        assertThat(found).isNotNull();
        assertThat(found.getSystem()).isTrue();
    }

    @Test
    void softDeletesPermissionInsteadOfRemovingRow() {
        Permission permission = Permission.builder()
                .code("permission:delete")
                .name("Delete Permissions")
                .module("identity")
                .build();

        entityManager.persist(permission);
        entityManager.flush();
        UUID id = permission.getId();

        entityManager.remove(permission);
        entityManager.flush();
        entityManager.clear();

        assertThat(entityManager.find(Permission.class, id)).isNull();

        Object deletedAt = entityManager
                .createNativeQuery("SELECT deleted_at FROM identity.permissions WHERE id = ?")
                .setParameter(1, id)
                .getSingleResult();

        assertThat(deletedAt).isNotNull();
    }

    @Test
    void mapsRolePermissionsBidirectionally() {
        Role role = Role.builder()
                .code("permission-owner")
                .name("Permission Owner")
                .build();
        Permission permission = Permission.builder()
                .code("permission:read")
                .name("Read Permissions")
                .module("identity")
                .build();

        entityManager.persist(role);
        entityManager.persist(permission);
        entityManager.flush();

        RolePermission rolePermission = RolePermission.builder()
                .id(new RolePermissionId(role.getId(), permission.getId()))
                .role(role)
                .permission(permission)
                .grantedAt(OffsetDateTime.now())
                .build();
        entityManager.persist(rolePermission);
        entityManager.flush();
        entityManager.clear();

        Role foundRole = entityManager.find(Role.class, role.getId());
        Permission foundPermission = entityManager.find(Permission.class, permission.getId());

        assertThat(foundRole.getRolePermissions())
                .hasSize(1)
                .first()
                .satisfies(found -> assertThat(found.getPermission().getCode()).isEqualTo("permission:read"));
        assertThat(foundPermission.getRolePermissions())
                .hasSize(1)
                .first()
                .satisfies(found -> assertThat(found.getRole().getCode()).isEqualTo("permission-owner"));
    }

    @Test
    void rolePermissionIdUsesRoleAndPermissionIdsForEquality() {
        UUID roleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();

        RolePermissionId first = new RolePermissionId(roleId, permissionId);
        RolePermissionId second = new RolePermissionId(roleId, permissionId);
        RolePermissionId differentPermission = new RolePermissionId(roleId, UUID.randomUUID());

        assertThat(first)
                .isEqualTo(second)
                .hasSameHashCodeAs(second)
                .isNotEqualTo(differentPermission);
    }
}
