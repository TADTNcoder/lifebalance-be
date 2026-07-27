package com.lifebalance.identity.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.lifebalance.identity.model.User;
import com.lifebalance.identity.service.PermissionEvaluationService;

@ExtendWith(MockitoExtension.class)
class CustomPermissionEvaluatorTest {

    @Mock
    private PermissionEvaluationService permissionEvaluationService;

    private CustomPermissionEvaluator evaluator;

    private Authentication authentication;

    @BeforeEach
    void setUp() {
        evaluator = new CustomPermissionEvaluator(permissionEvaluationService);
        authentication = new TestingAuthenticationToken("kc-user-1", "credentials");
    }

    @Test
    void shouldEvaluateDirectPermissionKeyWhenTargetDomainObjectIsNull() {
        when(permissionEvaluationService.hasPermission(authentication, "user:write"))
                .thenReturn(true);

        assertThat(evaluator.hasPermission(authentication, null, "USER:WRITE")).isTrue();

        verify(permissionEvaluationService).hasPermission(authentication, "user:write");
    }

    @Test
    void shouldEvaluatePermissionFromTargetIdTypeAndAction() {
        UUID targetId = UUID.randomUUID();
        when(permissionEvaluationService.hasPermission(authentication, "user", "update"))
                .thenReturn(true);

        assertThat(evaluator.hasPermission(authentication, targetId, "User", "UPDATE"))
                .isTrue();

        verify(permissionEvaluationService).hasPermission(authentication, "user", "update");
    }

    @Test
    void shouldAllowReadableUserResourceOwnerWhenPermissionIsMissing() {
        UUID targetId = UUID.randomUUID();
        when(permissionEvaluationService.hasPermission(authentication, "user", "read"))
                .thenReturn(false);
        when(permissionEvaluationService.isCurrentUser(authentication, targetId))
                .thenReturn(true);

        assertThat(evaluator.hasPermission(authentication, targetId, "User", "READ"))
                .isTrue();
    }

    @Test
    void shouldNotAllowWritableUserResourceOwnerWhenPermissionIsMissing() {
        UUID targetId = UUID.randomUUID();
        when(permissionEvaluationService.hasPermission(authentication, "user", "update"))
                .thenReturn(false);

        assertThat(evaluator.hasPermission(authentication, targetId, "User", "UPDATE"))
                .isFalse();
    }

    @Test
    void shouldAllowOwnedDomainObjectWhenPermissionIsMissing() {
        UUID ownerUserId = UUID.randomUUID();
        User targetUser = new User();
        targetUser.setId(ownerUserId);

        when(permissionEvaluationService.hasPermission(authentication, "user", "read"))
                .thenReturn(false);
        when(permissionEvaluationService.isCurrentUser(authentication, ownerUserId))
                .thenReturn(true);

        assertThat(evaluator.hasPermission(authentication, targetUser, "READ")).isTrue();
    }

    @Test
    void shouldAllowOwnedPermissionEvaluationContextWhenPermissionIsMissing() {
        UUID ownerUserId = UUID.randomUUID();
        PermissionEvaluationContext context =
                PermissionEvaluationContext.ownedBy("Task", "READ", ownerUserId);

        when(permissionEvaluationService.hasPermission(authentication, "task", "read"))
                .thenReturn(false);
        when(permissionEvaluationService.isCurrentUser(authentication, ownerUserId))
                .thenReturn(true);

        assertThat(evaluator.hasPermission(authentication, context, "READ")).isTrue();
    }

    @Test
    void shouldReturnFalseForInvalidInputs() {
        assertThat(evaluator.hasPermission(authentication, null, null)).isFalse();
        assertThat(evaluator.hasPermission(authentication, UUID.randomUUID(), "User", null))
                .isFalse();
        assertThat(evaluator.hasPermission(authentication, null, "READ")).isFalse();
        assertThat(evaluator.hasPermission(authentication, "User", new Object())).isFalse();

        verifyNoInteractions(permissionEvaluationService);
    }
}
