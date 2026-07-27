package com.lifebalance.security.keycloak;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifebalance.common.error.AuthErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest(classes = AuthorizationExceptionHandlingIntegrationTest.TestApplication.class)
@AutoConfigureMockMvc
class AuthorizationExceptionHandlingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnStandardUnauthorizedJsonWhenTokenIsMissing() throws Exception {
        mockMvc.perform(get("/api/secure"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.error.message").value("Authentication is required"))
                .andExpect(jsonPath("$.error.details").isMap())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void shouldReturnStandardForbiddenJsonWhenAuthenticatedUserLacksAuthority() throws Exception {
        mockMvc.perform(get("/api/admin")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_user"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.FORBIDDEN))
                .andExpect(jsonPath("$.error.message").value("Access is denied"))
                .andExpect(jsonPath("$.error.details").isMap())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = LifebalanceSecurityAutoConfiguration.class)
    @Import({
            TestSecurityConfig.class,
            TestController.class
    })
    static class TestApplication {
    }

    @Configuration
    static class TestSecurityConfig {

        @Bean
        AuthenticationFailureLogger authenticationFailureLogger() {
            return new AuthenticationFailureLogger();
        }

        @Bean
        AuthorizationFailureLogger authorizationFailureLogger() {
            return new AuthorizationFailureLogger();
        }

        @Bean
        LifebalanceAuthenticationEntryPoint lifebalanceAuthenticationEntryPoint(
                ObjectMapper objectMapper,
                AuthenticationFailureLogger authenticationFailureLogger
        ) {
            return new LifebalanceAuthenticationEntryPoint(objectMapper, authenticationFailureLogger);
        }

        @Bean
        LifebalanceAccessDeniedHandler lifebalanceAccessDeniedHandler(
                ObjectMapper objectMapper,
                AuthorizationFailureLogger authorizationFailureLogger
        ) {
            return new LifebalanceAccessDeniedHandler(objectMapper, authorizationFailureLogger);
        }

        @Bean
        SecurityFilterChain testSecurityFilterChain(
                HttpSecurity http,
                LifebalanceAuthenticationEntryPoint authenticationEntryPoint,
                LifebalanceAccessDeniedHandler accessDeniedHandler
        ) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .httpBasic(AbstractHttpConfigurer::disable)
                    .formLogin(AbstractHttpConfigurer::disable)
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/admin").hasAuthority("SCOPE_admin")
                            .anyRequest().authenticated()
                    )
                    .exceptionHandling(exception -> exception
                            .authenticationEntryPoint(authenticationEntryPoint)
                            .accessDeniedHandler(accessDeniedHandler)
                    )
                    .oauth2ResourceServer(oauth2 -> oauth2
                            .authenticationEntryPoint(authenticationEntryPoint)
                            .jwt(Customizer.withDefaults())
                    )
                    .build();
        }

        @Bean
        JwtDecoder jwtDecoder() {
            return token -> {
                throw new JwtException("JWT decoding is not used by this test");
            };
        }
    }

    @RestController
    static class TestController {

        @GetMapping("/api/secure")
        String secure() {
            return "secure";
        }

        @GetMapping("/api/admin")
        String admin() {
            return "admin";
        }
    }
}
