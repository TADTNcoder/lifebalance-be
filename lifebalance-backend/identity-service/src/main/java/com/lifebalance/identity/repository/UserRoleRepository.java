package com.lifebalance.identity.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lifebalance.identity.model.UserRole;
import com.lifebalance.identity.model.UserRoleId;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    boolean existsByUserIdAndRoleId(UUID userId, UUID roleId);

    List<UserRole> findByUserId(UUID userId);

    void deleteByUserIdAndRoleId(UUID userId, UUID roleId);

}
