package com.lifebalance.identity.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.lifebalance.identity.model.User;
import com.lifebalance.identity.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CurrentAuditActorResolverTest {

    @Mock
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldResolveActorFromJwtAndInternalUser() {
        UUID actorId = UUID.randomUUID();
        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "none"),
                Map.of("sub", "kc-admin", "preferred_username", "jwt-admin")
        );
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );
        User user = new User();
        user.setId(actorId);
        user.setKeycloakId("kc-admin");
        user.setUsername("admin");
        when(userRepository.findByKeycloakId("kc-admin")).thenReturn(Optional.of(user));
        CurrentAuditActorResolver resolver = new CurrentAuditActorResolver(userRepository);

        AuditActor actor = resolver.resolve();

        assertThat(actor.id()).isEqualTo(actorId);
        assertThat(actor.keycloakId()).isEqualTo("kc-admin");
        assertThat(actor.username()).isEqualTo("admin");
    }
}
