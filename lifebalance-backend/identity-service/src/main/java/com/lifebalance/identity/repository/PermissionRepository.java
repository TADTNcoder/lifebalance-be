package com.lifebalance.identity.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lifebalance.identity.model.Permission;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    @Query("""
            SELECT permission
            FROM Permission permission
            WHERE lower(permission.code) = lower(trim(:code))
            """)
    Optional<Permission> findByCode(@Param("code") String code);

    @Query("""
            SELECT count(permission) > 0
            FROM Permission permission
            WHERE lower(permission.code) = lower(trim(:code))
            """)
    boolean existsByCode(@Param("code") String code);

    @Query("""
            SELECT permission
            FROM Permission permission
            WHERE lower(permission.module) = lower(trim(:module))
            ORDER BY permission.code
            """)
    List<Permission> findByModule(@Param("module") String module);

    @Query("""
            SELECT DISTINCT permission
            FROM Permission permission
            JOIN permission.rolePermissions rolePermission
            WHERE rolePermission.role.id = :roleId
              AND permission.deletedAt IS NULL
            ORDER BY permission.code
            """)
    List<Permission> findByRoleId(@Param("roleId") UUID roleId);

    @Query("""
            SELECT DISTINCT permission
            FROM Permission permission
            JOIN permission.rolePermissions rolePermission
            WHERE rolePermission.role.id IN :roleIds
              AND permission.deletedAt IS NULL
            ORDER BY permission.code
            """)
    List<Permission> findAllByRoleIds(@Param("roleIds") Collection<UUID> roleIds);
}
