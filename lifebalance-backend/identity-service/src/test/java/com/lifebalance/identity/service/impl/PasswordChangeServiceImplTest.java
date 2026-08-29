package com.lifebalance.identity.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.config.PasswordChangeProperties;
import com.lifebalance.identity.dto.ChangePasswordRequest;
import com.lifebalance.identity.error.IdentityErrorCode;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.service.CurrentPasswordVerifier;
import com.lifebalance.identity.service.PasswordChangeAttemptLimiter;
import com.lifebalance.identity.service.PasswordChangeService;
import com.lifebalance.identity.service.PasswordCredentialUpdater;

@ExtendWith(MockitoExtension.class)
class PasswordChangeServiceImplTest {

    @Mock
    private CurrentPasswordVerifier currentPasswordVerifier;

    @Mock
    private PasswordCredentialUpdater credentialUpdater;

    @Mock
    private PasswordChangeAttemptLimiter attemptLimiter;

    private PasswordChangeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PasswordChangeServiceImpl(
                currentPasswordVerifier,
                credentialUpdater,
                attemptLimiter,
                new PasswordChangeProperties()
        );
    }

    @Test
    void shouldVerifyCurrentPasswordUpdateCredentialAndResetAttemptCounter() {
        User user = user();
        ChangePasswordRequest request = request("OldPassword1!", "NewPassword1!", "NewPassword1!");
        when(currentPasswordVerifier.verify("alice", "OldPassword1!")).thenReturn(true);
        when(credentialUpdater.updatePasswordAndRevokeSessions("kc-user-1", "NewPassword1!"))
                .thenReturn(true);

        PasswordChangeService.Result result = service.changePassword(
                user,
                "alice",
                request,
                "127.0.0.1"
        );

        assertThat(result.sessionsRevoked()).isTrue();
        verify(attemptLimiter).checkAllowed("kc-user-1|127.0.0.1");
        verify(attemptLimiter).reset("kc-user-1|127.0.0.1");
        verify(credentialUpdater).updatePasswordAndRevokeSessions("kc-user-1", "NewPassword1!");
    }

    @Test
    void shouldCountAnIncorrectCurrentPasswordWithoutCallingAdminApi() {
        User user = user();
        ChangePasswordRequest request = request("WrongPassword1!", "NewPassword1!", "NewPassword1!");
        when(currentPasswordVerifier.verify("alice", "WrongPassword1!")).thenReturn(false);

        assertThatThrownBy(() -> service.changePassword(user, "alice", request, "127.0.0.1"))
                .isInstanceOfSatisfying(AppException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(IdentityErrorCode.CURRENT_PASSWORD_INVALID));

        verify(attemptLimiter).recordFailure("kc-user-1|127.0.0.1");
        verify(credentialUpdater, never()).updatePasswordAndRevokeSessions("kc-user-1", "NewPassword1!");
    }

    @Test
    void shouldRejectMismatchedConfirmationBeforeVerifyingCredentials() {
        ChangePasswordRequest request = request("OldPassword1!", "NewPassword1!", "DifferentPassword1!");

        assertThatThrownBy(() -> service.changePassword(user(), "alice", request, "127.0.0.1"))
                .isInstanceOfSatisfying(AppException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(IdentityErrorCode.PASSWORD_CONFIRMATION_MISMATCH));

        verify(currentPasswordVerifier, never()).verify("alice", "OldPassword1!");
    }

    @Test
    void shouldRejectPasswordThatDoesNotMeetRealmPolicy() {
        ChangePasswordRequest request = request("OldPassword1!", "onlylowercase", "onlylowercase");

        assertThatThrownBy(() -> service.changePassword(user(), "alice", request, "127.0.0.1"))
                .isInstanceOfSatisfying(AppException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(IdentityErrorCode.PASSWORD_POLICY_VIOLATION));

        verify(currentPasswordVerifier, never()).verify("alice", "OldPassword1!");
    }

    private static ChangePasswordRequest request(String current, String password, String confirmation) {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword(current);
        request.setNewPassword(password);
        request.setConfirmPassword(confirmation);
        return request;
    }

    private static User user() {
        User user = new User();
        user.setId(UUID.fromString("1f3f8e30-8b2d-4c92-9fd8-3f11e50b2031"));
        user.setKeycloakId("kc-user-1");
        user.setUsername("alice");
        return user;
    }
}
