package com.lifebalance.identity.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.lifebalance.identity.model.Permission;
import com.lifebalance.identity.model.Role;
import com.lifebalance.identity.model.RolePermission;
import com.lifebalance.identity.model.RolePermissionId;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.model.UserRole;
import com.lifebalance.identity.model.UserRoleId;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Transactional
@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "eureka.client.enabled=false"
})
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesAndFindsRoleById() {
        Role role = roleRepository.saveAndFlush(Role.builder()
                .code(" Billing-Admin ")
                .name("Billing Admin")
                .description("Can manage billing configuration")
                .build());

        entityManager.clear();

        assertThat(roleRepository.findById(role.getId()))
                .isPresent()
                .get()
                .satisfies(found -> {
                    assertThat(found.getCode()).isEqualTo("billing-admin");
                    assertThat(found.getName()).isEqualTo("Billing Admin");
                    assertThat(found.getDescription()).isEqualTo("Can manage billing configuration");
                    assertThat(found.getSystem()).isFalse();
                    assertThat(found.getCreatedAt()).isNotNull();
                });
    }

    @Test
    void findsRoleByCodeAndNameIgnoringCaseAndOuterSpaces() {
        Role role = roleRepository.saveAndFlush(Role.builder()
                .code("Finance-Manager")
                .name("Finance Manager")
                .build());

        entityManager.clear();

        assertThat(roleRepository.findByCode(" FINANCE-MANAGER "))
                .isPresent()
                .get()
                .extracting(Role::getId)
                .isEqualTo(role.getId());
        assertThat(roleRepository.findByName(" finance manager "))
                .isPresent()
                .get()
                .extracting(Role::getId)
                .isEqualTo(role.getId());
        assertThat(roleRepository.existsByCode(" FINANCE-MANAGER ")).isTrue();
        assertThat(roleRepository.existsByCode("missing-role")).isFalse();
        assertThat(roleRepository.existsByName(" FINANCE MANAGER ")).isTrue();
        assertThat(roleRepository.existsByName("Missing Role")).isFalse();
    }

    @Test
    void detectsDuplicateRoleCodeExcludingCurrentRole() {
        Role first = roleRepository.save(Role.builder()
                .code("Duplicate-Role-A")
                .name("Duplicate Role A")
                .build());
        Role second = roleRepository.saveAndFlush(Role.builder()
                .code("Duplicate-Role-B")
                .name("Duplicate Role B")
                .build());

        assertThat(roleRepository.existsByCodeAndIdNot(" duplicate-role-a ", first.getId())).isFalse();
        assertThat(roleRepository.existsByCodeAndIdNot(" DUPLICATE-ROLE-A ", second.getId())).isTrue();
        assertThat(roleRepository.existsByNameAndIdNot(" Duplicate Role A ", first.getId())).isFalse();
        assertThat(roleRepository.existsByNameAndIdNot(" DUPLICATE ROLE A ", second.getId())).isTrue();
    }

    @Test
    void findsSystemAndCustomRoles() {
        Role systemRole = roleRepository.save(Role.builder()
                .code("Lb615-System-Role")
                .name("LB615 System Role")
                .system(true)
                .build());
        Role customRole = roleRepository.saveAndFlush(Role.builder()
                .code("Lb615-Custom-Role")
                .name("LB615 Custom Role")
                .system(false)
                .build());

        entityManager.clear();

        assertThat(roleRepository.findBySystemTrueOrderByCodeAsc())
                .extracting(Role::getId)
                .contains(systemRole.getId());
        assertThat(roleRepository.findBySystemFalseOrderByCodeAsc())
                .extracting(Role::getId)
                .contains(customRole.getId())
                .doesNotContain(systemRole.getId());
    }

    @Test
    void excludesSoftDeletedRolesFromDefaultQueriesButNativeQueriesCanDetectThem() {
        Role role = roleRepository.saveAndFlush(Role.builder()
                .code("Soft-Deleted-Role")
                .name("Soft Deleted Role")
                .build());

        roleRepository.delete(role);
        roleRepository.flush();
        entityManager.clear();

        assertThat(roleRepository.findById(role.getId())).isEmpty();
        assertThat(roleRepository.findByCode("soft-deleted-role")).isEmpty();
        assertThat(roleRepository.existsByCode("soft-deleted-role")).isFalse();
        assertThat(roleRepository.existsByIdIncludingDeleted(role.getId())).isTrue();
        assertThat(roleRepository.existsDeletedById(role.getId())).isTrue();
    }

    @Test
    void findsRolesAssignedToUser() {
        User user = persistUser("role-user@example.com", "role-user");
        Role firstRole = roleRepository.save(Role.builder()
                .code("Assigned-Role-A")
                .name("Assigned Role A")
                .build());
        Role secondRole = roleRepository.saveAndFlush(Role.builder()
                .code("Assigned-Role-B")
                .name("Assigned Role B")
                .build());

        persistUserRole(user, firstRole);
        persistUserRole(user, secondRole);
        entityManager.flush();
        entityManager.clear();

        assertThat(roleRepository.findByUserId(user.getId()))
                .extracting(Role::getCode)
                .containsExactly("assigned-role-a", "assigned-role-b");
    }

    @Test
    void findsPermissionCodesGrantedToRole() {
        Role role = roleRepository.saveAndFlush(Role.builder()
                .code("Permission-Role")
                .name("Permission Role")
                .build());
        Permission readPermission = persistPermission("Task:Read", "Read Tasks", "Task");
        Permission updatePermission = persistPermission("Task:Update", "Update Tasks", "Task");

        persistRolePermission(role, readPermission);
        persistRolePermission(role, updatePermission);
        entityManager.flush();
        entityManager.clear();

        List<String> expectedPermissionCodes = List.of("task:read", "task:update");

        assertThat(roleRepository.findPermissionCodesByRoleId(role.getId()))
                .containsExactlyElementsOf(expectedPermissionCodes);
        assertThat(roleRepository.findPermissionCodesByRoleCode(" PERMISSION-ROLE "))
                .containsExactlyElementsOf(expectedPermissionCodes);
    }

    private User persistUser(String email, String username) {
        User user = User.builder()
                .email(email)
                .username(username)
                .build();
        entityManager.persist(user);
        entityManager.flush();
        return user;
    }

    private Permission persistPermission(String code, String name, String module) {
        Permission permission = Permission.builder()
                .code(code)
                .name(name)
                .module(module)
                .build();
        entityManager.persist(permission);
        entityManager.flush();
        return permission;
    }

    private void persistUserRole(User user, Role role) {
        entityManager.persist(UserRole.builder()
                .id(new UserRoleId(user.getId(), role.getId()))
                .user(user)
                .role(role)
                .assignedAt(OffsetDateTime.now())
                .build());
    }

    private void persistRolePermission(Role role, Permission permission) {
        entityManager.persist(RolePermission.builder()
                .id(new RolePermissionId(role.getId(), permission.getId()))
                .role(role)
                .permission(permission)
                .grantedAt(OffsetDateTime.now())
                .build());
    }
}
