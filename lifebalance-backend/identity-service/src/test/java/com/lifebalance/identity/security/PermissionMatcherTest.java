package com.lifebalance.identity.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class PermissionMatcherTest {

    private final PermissionMatcher matcher = new PermissionMatcher();

    @Test
    void shouldMatchExactPermissionIgnoringCaseAndWhitespace() {
        assertThat(matcher.matches(" USER:READ ", "user:read")).isTrue();
    }

    @Test
    void shouldMatchDomainWildcardPermission() {
        assertThat(matcher.matches("user:*", "user:delete")).isTrue();
    }

    @Test
    void shouldMatchGlobalWildcardPermissions() {
        assertThat(matcher.matches("*", "user:delete")).isTrue();
        assertThat(matcher.matches("*:*", "role:create")).isTrue();
    }

    @Test
    void shouldNotMatchDifferentDomainOrInvalidPermissions() {
        assertThat(matcher.matches("task:*", "user:delete")).isFalse();
        assertThat(matcher.matches("user:read", "user:delete")).isFalse();
        assertThat(matcher.matches("user", "user:delete")).isFalse();
        assertThat(matcher.matches(null, "user:delete")).isFalse();
        assertThat(matcher.matches("user:*", " ")).isFalse();
    }

    @Test
    void shouldMatchAnyGrantedPermission() {
        assertThat(matcher.anyMatches(
                List.of("task:read", "user:*"),
                "user:update"
        )).isTrue();
    }

    @Test
    void shouldBuildNormalizedPermissionKey() {
        assertThat(matcher.permissionKey(" User ", " DELETE "))
                .isEqualTo("user:delete");
        assertThat(matcher.permissionKey("user:admin", "delete")).isNull();
        assertThat(matcher.permissionKey("user", null)).isNull();
    }
}
