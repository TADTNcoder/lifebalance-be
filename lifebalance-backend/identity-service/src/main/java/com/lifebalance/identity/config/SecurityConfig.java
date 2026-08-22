package com.lifebalance.identity.config;

import com.lifebalance.security.keycloak.LifebalanceAccessDeniedHandler;
import com.lifebalance.security.keycloak.LifebalanceAuthenticationEntryPoint;
import com.lifebalance.security.keycloak.PublicReadinessBearerTokenResolver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

        @Bean
        SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        LifebalanceAuthenticationEntryPoint authenticationEntryPoint,
                        LifebalanceAccessDeniedHandler accessDeniedHandler,
                        ObjectProvider<BearerTokenResolver> bearerTokenResolverProvider
        ) throws Exception {
                BearerTokenResolver bearerTokenResolver = bearerTokenResolverProvider
                                .getIfAvailable(PublicReadinessBearerTokenResolver::new);

                return http
                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(Customizer.withDefaults())
                                .httpBasic(AbstractHttpConfigurer::disable)
                                .formLogin(AbstractHttpConfigurer::disable)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/actuator/health/**",
                                                                "/actuator/info",
                                                                "/actuator/prometheus")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET,
                                                                "/api/*/status",
                                                                "/api/*/*/status",
                                                                "/api/*/*/*/status",
                                                                "/api/*/health",
                                                                "/api/*/*/health",
                                                                "/api/*/*/*/health")
                                                .permitAll()
                                                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**",
                                                                "/swagger-ui.html")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(authenticationEntryPoint)
                                                .accessDeniedHandler(accessDeniedHandler))
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .authenticationEntryPoint(authenticationEntryPoint)
                                                .bearerTokenResolver(bearerTokenResolver)
                                                .jwt(Customizer.withDefaults()))
                                .build();
        }

}
