package com.lifebalance.identity.repository;

import com.lifebalance.identity.model.RolePermission;
import com.lifebalance.identity.model.RolePermissionId;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {

    @Query("""
            SELECT rolePermission
            FROM RolePermission rolePermission
            JOIN FETCH rolePermission.permission permission
            WHERE rolePermission.role.id IN :roleIds
              AND permission.deletedAt IS NULL
            """)
    List<RolePermission> findByRoleIds(@Param("roleIds") Collection<UUID> roleIds);

    @Modifying
    @Query("""
            DELETE FROM RolePermission rolePermission
            WHERE rolePermission.role.id = :roleId
            """)
    void deleteByRoleId(@Param("roleId") UUID roleId);
}
