package com.lifebalance.identity.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lifebalance.identity.model.RolePermission;
import com.lifebalance.identity.model.RolePermissionId;

public interface RolePermissionRepository
        extends JpaRepository<RolePermission, RolePermissionId> {

    boolean existsByIdRoleIdAndIdPermissionId(
            UUID roleId,
            UUID permissionId);

    void deleteByIdRoleIdAndIdPermissionId(
            UUID roleId,
            UUID permissionId);

    List<RolePermission> findByIdRoleId(UUID roleId);
}