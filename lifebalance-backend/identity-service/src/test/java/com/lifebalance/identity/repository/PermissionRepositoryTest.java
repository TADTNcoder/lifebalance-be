package com.lifebalance.identity.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.lifebalance.identity.model.Permission;
import com.lifebalance.identity.model.Role;
import com.lifebalance.identity.model.RolePermission;
import com.lifebalance.identity.model.RolePermissionId;
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
class PermissionRepositoryTest {

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesAndFindsPermissionById() {
        Permission permission = permissionRepository.saveAndFlush(Permission.builder()
                .code(" Billing:Read ")
                .name("Read Billing")
                .module(" Billing ")
                .description("Can read billing data")
                .build());

        entityManager.clear();

        assertThat(permissionRepository.findById(permission.getId()))
                .isPresent()
                .get()
                .satisfies(found -> {
                    assertThat(found.getCode()).isEqualTo("billing:read");
                    assertThat(found.getName()).isEqualTo("Read Billing");
                    assertThat(found.getModule()).isEqualTo("billing");
                    assertThat(found.getDescription()).isEqualTo("Can read billing data");
                    assertThat(found.getSystem()).isFalse();
                    assertThat(found.getCreatedAt()).isNotNull();
                });
    }

    @Test
    void findsPermissionByCodeIgnoringCaseAndOuterSpaces() {
        Permission permission = permissionRepository.saveAndFlush(Permission.builder()
                .code("Finance:Approve")
                .name("Approve Finance")
                .module("Finance")
                .build());

        entityManager.clear();

        assertThat(permissionRepository.findByCode(" FINANCE:APPROVE "))
                .isPresent()
                .get()
                .extracting(Permission::getId)
                .isEqualTo(permission.getId());
        assertThat(permissionRepository.existsByCode(" FINANCE:APPROVE ")).isTrue();
        assertThat(permissionRepository.existsByCode("missing:permission")).isFalse();
    }

    @Test
    void findsPermissionsByModuleIgnoringCaseAndOuterSpaces() {
        Permission createPermission = permissionRepository.save(Permission.builder()
                .code("Task:Create")
                .name("Create Tasks")
                .module("Task")
                .build());
        Permission readPermission = permissionRepository.save(Permission.builder()
                .code("Task:Read")
                .name("Read Tasks")
                .module("Task")
                .build());
        permissionRepository.saveAndFlush(Permission.builder()
                .code("Finance:Read")
                .name("Read Finance")
                .module("Finance")
                .build());

        entityManager.clear();

        assertThat(permissionRepository.findByModule(" TASK "))
                .extracting(Permission::getId)
                .containsExactly(createPermission.getId(), readPermission.getId());
    }

    @Test
    void excludesSoftDeletedPermissionsFromDefaultQueries() {
        Permission permission = permissionRepository.saveAndFlush(Permission.builder()
                .code("Soft-Deleted:Permission")
                .name("Soft Deleted Permission")
                .module("Soft Delete")
                .build());

        permissionRepository.delete(permission);
        permissionRepository.flush();
        entityManager.clear();

        assertThat(permissionRepository.findById(permission.getId())).isEmpty();
        assertThat(permissionRepository.findByCode("soft-deleted:permission")).isEmpty();
        assertThat(permissionRepository.existsByCode("soft-deleted:permission")).isFalse();
        assertThat(permissionRepository.findByModule("soft delete")).isEmpty();
    }

    @Test
    void findsPermissionsGrantedToRole() {
        Role role = persistRole("Permission-Repository-Role", "Permission Repository Role");
        Permission readPermission = permissionRepository.save(Permission.builder()
                .code("Project:Read")
                .name("Read Projects")
                .module("Project")
                .build());
        Permission updatePermission = permissionRepository.saveAndFlush(Permission.builder()
                .code("Project:Update")
                .name("Update Projects")
                .module("Project")
                .build());

        persistRolePermission(role, readPermission);
        persistRolePermission(role, updatePermission);
        entityManager.flush();
        entityManager.clear();

        assertThat(permissionRepository.findByRoleId(role.getId()))
                .extracting(Permission::getCode)
                .containsExactly("project:read", "project:update");
    }

    @Test
    void findsDistinctPermissionsGrantedToRoles() {
        Role editorRole = persistRole("Permission-Repository-Editor", "Permission Repository Editor");
        Role auditorRole = persistRole("Permission-Repository-Auditor", "Permission Repository Auditor");
        Permission exportPermission = permissionRepository.save(Permission.builder()
                .code("Report:Export")
                .name("Export Reports")
                .module("Report")
                .build());
        Permission readPermission = permissionRepository.save(Permission.builder()
                .code("Report:Read")
                .name("Read Reports")
                .module("Report")
                .build());
        Permission updatePermission = permissionRepository.saveAndFlush(Permission.builder()
                .code("Report:Update")
                .name("Update Reports")
                .module("Report")
                .build());

        persistRolePermission(editorRole, readPermission);
        persistRolePermission(editorRole, updatePermission);
        persistRolePermission(auditorRole, exportPermission);
        persistRolePermission(auditorRole, readPermission);
        entityManager.flush();
        entityManager.clear();

        assertThat(permissionRepository.findAllByRoleIds(List.of(editorRole.getId(), auditorRole.getId())))
                .extracting(Permission::getCode)
                .containsExactly("report:export", "report:read", "report:update");
    }

    @Test
    void excludesSoftDeletedPermissionsFromRoleQueries() {
        Role role = persistRole("Permission-Repository-Soft-Delete", "Permission Repository Soft Delete");
        Permission activePermission = permissionRepository.save(Permission.builder()
                .code("Account:Read")
                .name("Read Accounts")
                .module("Account")
                .build());
        Permission deletedPermission = permissionRepository.saveAndFlush(Permission.builder()
                .code("Account:Delete")
                .name("Delete Accounts")
                .module("Account")
                .build());

        persistRolePermission(role, activePermission);
        persistRolePermission(role, deletedPermission);
        entityManager.flush();
        entityManager.createNativeQuery("""
                        UPDATE identity.permissions
                        SET deleted_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """)
                .setParameter(1, deletedPermission.getId())
                .executeUpdate();
        entityManager.clear();

        assertThat(permissionRepository.findByRoleId(role.getId()))
                .extracting(Permission::getCode)
                .containsExactly("account:read");
    }

    private Role persistRole(String code, String name) {
        Role role = Role.builder()
                .code(code)
                .name(name)
                .build();
        entityManager.persist(role);
        entityManager.flush();
        return role;
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
