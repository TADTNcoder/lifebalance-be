package com.lifebalance.identity.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lifebalance.identity.model.User;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByKeycloakId(String keycloakId);

    @Query(value = """
            SELECT DISTINCT role.code
            FROM identity.user_roles user_role
            JOIN identity.roles role ON role.id = user_role.role_id
            WHERE user_role.user_id = :userId
              AND role.deleted_at IS NULL
            ORDER BY role.code
            """, nativeQuery = true)
    List<String> findRoleCodesByUserId(@Param("userId") UUID userId);

    @Query(value = """
            SELECT DISTINCT permission.code
            FROM identity.user_roles user_role
            JOIN identity.roles role ON role.id = user_role.role_id
            JOIN identity.role_permissions role_permission ON role_permission.role_id = role.id
            JOIN identity.permissions permission ON permission.id = role_permission.permission_id
            WHERE user_role.user_id = :userId
              AND role.deleted_at IS NULL
              AND permission.deleted_at IS NULL
            ORDER BY permission.code
            """, nativeQuery = true)
    List<String> findPermissionCodesByUserId(@Param("userId") UUID userId);
}
