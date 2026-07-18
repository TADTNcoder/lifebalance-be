package com.lifebalance.security.keycloak;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class KeycloakUserMappingFilterTest {

    private KeycloakUserMappingFilter filter;

    @BeforeEach
    void setUp() {
        KeycloakSecurityProperties properties = new KeycloakSecurityProperties();
        properties.setClientId("lifebalance-api");
        filter = new KeycloakUserMappingFilter(new KeycloakUserMapper(properties));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldNotSetCurrentUserWhenAuthenticationIsNull() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/tasks");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(request.getAttribute(KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE)).isNull();
        assertThat(chain.getRequest()).isSameAs(request);
    }

    @Test
    void shouldNotSetCurrentUserWhenAuthenticationIsNotJwtAuthenticationToken() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("user", "password", "ROLE_USER")
        );

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/tasks");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(request.getAttribute(KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE)).isNull();
        assertThat(chain.getRequest()).isSameAs(request);
    }

    @Test
    void shouldSetCurrentUserWhenAuthenticationIsJwtAuthenticationToken() throws Exception {
        Jwt jwt = jwt(Map.of(
                "sub", "user-1",
                "preferred_username", "john",
                "realm_access", Map.of("roles", List.of("admin")),
                "resource_access", Map.of(
                        "lifebalance-api", Map.of("roles", List.of("task:read"))
                )
        ));

        SecurityContextHolder.getContext().setAuthentication(jwtAuthentication(jwt));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/tasks");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        Object currentUser = request.getAttribute(KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE);

        assertThat(currentUser).isInstanceOf(KeycloakUserPrincipal.class);
        assertThat(((KeycloakUserPrincipal) currentUser).subject()).isEqualTo("user-1");
        assertThat(((KeycloakUserPrincipal) currentUser).roles()).containsExactly("admin", "task:read");
        assertThat(chain.getRequest()).isSameAs(request);
    }

    private JwtAuthenticationToken jwtAuthentication(Jwt jwt) {
        return new JwtAuthenticationToken(
                jwt,
                List.of(new SimpleGrantedAuthority("SCOPE_test"))
        );
    }

    private Jwt jwt(Map<String, Object> claims) {
        Map<String, Object> finalClaims = new LinkedHashMap<>(claims);

        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .claims(existingClaims -> existingClaims.putAll(finalClaims))
                .build();
    }

}
