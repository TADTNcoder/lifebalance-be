package com.lifebalance.identity.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lifebalance.identity.model.User;

public interface UserRepository extends JpaRepository<User, UUID> {
    @Query("""
            SELECT user
            FROM User user
            WHERE lower(user.email) = lower(:email)
            """)
    Optional<User> findByEmail(@Param("email") String email);

    @Query("""
            SELECT user
            FROM User user
            WHERE lower(user.username) = lower(:username)
            """)
    Optional<User> findByUsername(@Param("username") String username);

    Optional<User> findByKeycloakId(String keycloakId);

    @Query(value = """
            SELECT count(*) > 0
            FROM identity.users
            WHERE id = :id
            """, nativeQuery = true)
    boolean existsByIdIncludingDeleted(@Param("id") UUID id);

    @Query(value = """
            SELECT count(*) > 0
            FROM identity.users
            WHERE id = :id
              AND deleted_at IS NOT NULL
            """, nativeQuery = true)
    boolean existsDeletedById(@Param("id") UUID id);

    @Query(value = """
            SELECT count(*) > 0
            FROM identity.users
            WHERE keycloak_id = :keycloakId
              AND deleted_at IS NOT NULL
            """, nativeQuery = true)
    boolean existsDeletedByKeycloakId(@Param("keycloakId") String keycloakId);

    @Query("""
            SELECT count(user) > 0
            FROM User user
            WHERE lower(user.email) = lower(:email)
            """)
    boolean existsByEmail(@Param("email") String email);

    @Query("""
            SELECT count(user) > 0
            FROM User user
            WHERE lower(user.username) = lower(:username)
            """)
    boolean existsByUsername(@Param("username") String username);

    @Query("""
            SELECT count(user) > 0
            FROM User user
            WHERE lower(user.email) = lower(:email)
              AND user.id <> :id
            """)
    boolean existsByEmailAndIdNot(
            @Param("email") String email,
            @Param("id") UUID id
    );

    @Query("""
            SELECT count(user) > 0
            FROM User user
            WHERE lower(user.username) = lower(:username)
              AND user.id <> :id
            """)
    boolean existsByUsernameAndIdNot(
            @Param("username") String username,
            @Param("id") UUID id
    );

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
