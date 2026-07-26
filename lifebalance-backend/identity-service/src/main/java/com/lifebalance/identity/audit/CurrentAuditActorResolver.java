package com.lifebalance.identity.audit;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import com.lifebalance.identity.model.User;
import com.lifebalance.identity.repository.UserRepository;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CurrentAuditActorResolver {

    private static final String CLAIM_USERNAME = "preferred_username";

    private final UserRepository userRepository;

    public AuditActor resolve() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isUsableAuthentication(authentication)) {
            return new AuditActor(null, null, null);
        }

        String keycloakId = resolveKeycloakId(authentication);
        String username = resolveUsername(authentication);
        Optional<User> actor = keycloakId == null
                ? Optional.empty()
                : userRepository.findByKeycloakId(keycloakId);

        return actor
                .map(user -> new AuditActor(
                        user.getId(),
                        firstNonBlank(user.getKeycloakId(), keycloakId),
                        firstNonBlank(user.getUsername(), username)
                ))
                .orElseGet(() -> new AuditActor(null, keycloakId, username));
    }

    private static boolean isUsableAuthentication(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    private static String resolveKeycloakId(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            return trimToNull(jwtAuthenticationToken.getToken().getSubject());
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return trimToNull(jwt.getSubject());
        }
        if (principal instanceof KeycloakUserPrincipal keycloakUserPrincipal) {
            return trimToNull(keycloakUserPrincipal.subject());
        }

        return trimToNull(authentication.getName());
    }

    private static String resolveUsername(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            return trimToNull(jwtAuthenticationToken.getToken().getClaimAsString(CLAIM_USERNAME));
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return trimToNull(jwt.getClaimAsString(CLAIM_USERNAME));
        }
        if (principal instanceof KeycloakUserPrincipal keycloakUserPrincipal) {
            return trimToNull(keycloakUserPrincipal.username());
        }

        return trimToNull(authentication.getName());
    }

    private static String firstNonBlank(String first, String second) {
        String normalizedFirst = trimToNull(first);
        return normalizedFirst == null ? trimToNull(second) : normalizedFirst;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
