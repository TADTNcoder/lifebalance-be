package com.lifebalance.identity.repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lifebalance.identity.model.Role;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByCode(String code);

    List<Role> findByIdIn(List<UUID> ids);

    boolean existsByCode(String code);

}
