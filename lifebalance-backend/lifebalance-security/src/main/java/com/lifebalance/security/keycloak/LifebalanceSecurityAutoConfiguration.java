package com.lifebalance.security.keycloak;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@AutoConfiguration(before = SecurityAutoConfiguration.class)
@EnableConfigurationProperties(KeycloakSecurityProperties.class)
public class LifebalanceSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public KeycloakUserMapper keycloakUserMapper(KeycloakSecurityProperties properties) {
        return new KeycloakUserMapper(properties);
    }

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain lifebalanceSecurityFilterChain(
            HttpSecurity http,
            KeycloakUserMapper keycloakUserMapper
    ) throws Exception {
        KeycloakUserMappingFilter keycloakUserMappingFilter = new KeycloakUserMappingFilter(keycloakUserMapper);

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/*/status").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .addFilterAfter(keycloakUserMappingFilter, BearerTokenAuthenticationFilter.class)
                .build();
    }

}
