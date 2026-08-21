package com.lifebalance.identity.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lifebalance.identity.model.SystemConfiguration;

public interface SystemConfigurationRepository extends JpaRepository<SystemConfiguration, UUID> {

    Optional<SystemConfiguration> findByConfigKey(String configKey);

    List<SystemConfiguration> findAllByOrderByConfigKeyAsc();
}
