package com.lifebalance.identity.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lifebalance.identity.model.Role;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    @Query("""
            SELECT role
            FROM Role role
            WHERE lower(role.code) = lower(trim(:code))
            """)
    Optional<Role> findByCode(@Param("code") String code);

    @Query("""
            SELECT role
            FROM Role role
            WHERE lower(role.name) = lower(trim(:name))
            """)
    Optional<Role> findByName(@Param("name") String name);

    @Query("""
            SELECT count(role) > 0
            FROM Role role
            WHERE lower(role.code) = lower(trim(:code))
            """)
    boolean existsByCode(@Param("code") String code);

    @Query("""
            SELECT count(role) > 0
            FROM Role role
            WHERE lower(role.name) = lower(trim(:name))
            """)
    boolean existsByName(@Param("name") String name);

    @Query("""
            SELECT count(role) > 0
            FROM Role role
            WHERE lower(role.code) = lower(trim(:code))
              AND role.id <> :id
            """)
    boolean existsByCodeAndIdNot(
            @Param("code") String code,
            @Param("id") UUID id
    );

    @Query("""
            SELECT count(role) > 0
            FROM Role role
            WHERE lower(role.name) = lower(trim(:name))
              AND role.id <> :id
            """)
    boolean existsByNameAndIdNot(
            @Param("name") String name,
            @Param("id") UUID id
    );

    List<Role> findBySystemTrueOrderByCodeAsc();

    List<Role> findBySystemFalseOrderByCodeAsc();

    @Query("""
            SELECT DISTINCT role
            FROM UserRole userRole
            JOIN userRole.role role
            WHERE userRole.user.id = :userId
            ORDER BY role.code
            """)
    List<Role> findByUserId(@Param("userId") UUID userId);

    @Query(value = """
            SELECT count(*) > 0
            FROM identity.roles
            WHERE id = :id
            """, nativeQuery = true)
    boolean existsByIdIncludingDeleted(@Param("id") UUID id);

    @Query(value = """
            SELECT count(*) > 0
            FROM identity.roles
            WHERE id = :id
              AND deleted_at IS NOT NULL
            """, nativeQuery = true)
    boolean existsDeletedById(@Param("id") UUID id);

    @Query(value = """
            SELECT DISTINCT permission.code
            FROM identity.role_permissions role_permission
            JOIN identity.permissions permission ON permission.id = role_permission.permission_id
            WHERE role_permission.role_id = :roleId
              AND permission.deleted_at IS NULL
            ORDER BY permission.code
            """, nativeQuery = true)
    List<String> findPermissionCodesByRoleId(@Param("roleId") UUID roleId);

    @Query(value = """
            SELECT DISTINCT permission.code
            FROM identity.roles role
            JOIN identity.role_permissions role_permission ON role_permission.role_id = role.id
            JOIN identity.permissions permission ON permission.id = role_permission.permission_id
            WHERE lower(role.code) = lower(trim(:roleCode))
              AND role.deleted_at IS NULL
              AND permission.deleted_at IS NULL
            ORDER BY permission.code
            """, nativeQuery = true)
    List<String> findPermissionCodesByRoleCode(@Param("roleCode") String roleCode);

}
