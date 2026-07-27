package com.lifebalance.security.method;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@AutoConfiguration
@EnableMethodSecurity(prePostEnabled = true)
public class LifebalanceMethodSecurityAutoConfiguration {
}
