package com.lifebalance.identity.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lifebalance.identity.model.UserRole;
import com.lifebalance.identity.model.UserRoleId;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    boolean existsByUserIdAndRoleId(UUID userId, UUID roleId);

    @Query("""
            SELECT userRole
            FROM UserRole userRole
            JOIN FETCH userRole.role
            WHERE userRole.user.id = :userId
            """)
    List<UserRole> findByUserId(@Param("userId") UUID userId);

    void deleteByUserIdAndRoleId(UUID userId, UUID roleId);

    @Modifying
    @Query("""
            DELETE FROM UserRole userRole
            WHERE userRole.user.id = :userId
              AND userRole.role.id IN :roleIds
            """)
    void deleteByUserIdAndRoleIds(
            @Param("userId") UUID userId,
            @Param("roleIds") Collection<UUID> roleIds
    );
}
