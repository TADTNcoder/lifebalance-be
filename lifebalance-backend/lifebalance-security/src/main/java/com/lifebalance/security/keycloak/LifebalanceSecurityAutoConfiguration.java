package com.lifebalance.security.keycloak;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@AutoConfiguration(before = SecurityAutoConfiguration.class)
@EnableConfigurationProperties(KeycloakSecurityProperties.class)
public class LifebalanceSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public KeycloakUserMapper keycloakUserMapper(
            KeycloakSecurityProperties properties
    ) {
        return new KeycloakUserMapper(properties);
    }

    @Bean
    @ConditionalOnMissingBean(JwtDecoder.class)
    @ConditionalOnProperty(
            prefix = "spring.security.oauth2.resourceserver.jwt",
            name = "issuer-uri"
    )
    public JwtDecoder lifebalanceJwtDecoder(
            OAuth2ResourceServerProperties resourceServerProperties,
            KeycloakSecurityProperties keycloakSecurityProperties
    ) {
        AtomicReference<JwtDecoder> delegate = new AtomicReference<>();
        return token -> {
            JwtDecoder decoder = delegate.updateAndGet(existing -> existing == null
                    ? buildJwtDecoder(resourceServerProperties, keycloakSecurityProperties)
                    : existing);
            return decoder.decode(token);
        };
    }

    private JwtDecoder buildJwtDecoder(
            OAuth2ResourceServerProperties resourceServerProperties,
            KeycloakSecurityProperties keycloakSecurityProperties
    ) {
        String issuerUri = resourceServerProperties.getJwt().getIssuerUri();
        String jwkSetUri = resourceServerProperties.getJwt().getJwkSetUri();
        NimbusJwtDecoder decoder = jwkSetUri == null
                ? NimbusJwtDecoder.withIssuerLocation(issuerUri).build()
                : NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                issuerUri == null
                        ? new JwtTimestampValidator()
                        : JwtValidators.createDefaultWithIssuer(issuerUri),
                JwtAudienceValidator.requireAudience(keycloakSecurityProperties.getClientId())
        ));

        return decoder;
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthenticationFailureLogger authenticationFailureLogger() {
        return new AuthenticationFailureLogger();
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthorizationFailureLogger authorizationFailureLogger() {
        return new AuthorizationFailureLogger();
    }

    @Bean
    @ConditionalOnMissingBean(BearerTokenResolver.class)
    public BearerTokenResolver lifebalanceBearerTokenResolver() {
        return new PublicReadinessBearerTokenResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public LifebalanceAuthenticationEntryPoint lifebalanceAuthenticationEntryPoint(
            ObjectMapper objectMapper,
            AuthenticationFailureLogger authenticationFailureLogger
    ) {
        return new LifebalanceAuthenticationEntryPoint(objectMapper, authenticationFailureLogger);
    }

    @Bean
    @ConditionalOnMissingBean
    public LifebalanceAccessDeniedHandler lifebalanceAccessDeniedHandler(
            ObjectMapper objectMapper,
            AuthorizationFailureLogger authorizationFailureLogger
    ) {
        return new LifebalanceAccessDeniedHandler(objectMapper, authorizationFailureLogger);
    }

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain lifebalanceSecurityFilterChain(
            HttpSecurity http,
            KeycloakUserMapper keycloakUserMapper,
            LifebalanceAuthenticationEntryPoint authenticationEntryPoint,
            LifebalanceAccessDeniedHandler accessDeniedHandler,
            BearerTokenResolver bearerTokenResolver,
            Map<String, JwtDecoder> jwtDecoders
    ) throws Exception {
        KeycloakUserMappingFilter keycloakUserMappingFilter =
                new KeycloakUserMappingFilter(keycloakUserMapper);

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/actuator/health/**",
                                "/actuator/info",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/*/status",
                                "/api/*/*/status",
                                "/api/*/*/*/status",
                                "/api/*/health",
                                "/api/*/*/health",
                                "/api/*/*/*/health"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2
                                .authenticationEntryPoint(authenticationEntryPoint)
                                .bearerTokenResolver(bearerTokenResolver)
                                .jwt(jwt -> {
                                    JwtDecoder jwtDecoder = preferredJwtDecoder(jwtDecoders);
                                    if (jwtDecoder != null) {
                                        jwt.decoder(jwtDecoder);
                                    }
                                })
                )
                .addFilterAfter(
                        keycloakUserMappingFilter,
                        BearerTokenAuthenticationFilter.class
                )
                .build();
    }

    private JwtDecoder preferredJwtDecoder(Map<String, JwtDecoder> jwtDecoders) {
        JwtDecoder namedJwtDecoder = jwtDecoders.get("jwtDecoder");
        if (namedJwtDecoder != null) {
            return namedJwtDecoder;
        }
        if (jwtDecoders.size() == 1) {
            return jwtDecoders.values().iterator().next();
        }
        return jwtDecoders.get("lifebalanceJwtDecoder");
    }
}
