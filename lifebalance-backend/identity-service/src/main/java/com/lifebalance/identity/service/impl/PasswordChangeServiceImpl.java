package com.lifebalance.identity.service.impl;

import com.lifebalance.identity.config.PasswordChangeProperties;
import com.lifebalance.identity.dto.ChangePasswordRequest;
import com.lifebalance.identity.error.PasswordChangeExceptions;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.service.CurrentPasswordVerifier;
import com.lifebalance.identity.service.PasswordChangeAttemptLimiter;
import com.lifebalance.identity.service.PasswordChangeService;
import com.lifebalance.identity.service.PasswordCredentialUpdater;

public class PasswordChangeServiceImpl implements PasswordChangeService {

    private final CurrentPasswordVerifier currentPasswordVerifier;
    private final PasswordCredentialUpdater credentialUpdater;
    private final PasswordChangeAttemptLimiter attemptLimiter;
    private final PasswordChangeProperties properties;

    public PasswordChangeServiceImpl(
            CurrentPasswordVerifier currentPasswordVerifier,
            PasswordCredentialUpdater credentialUpdater,
            PasswordChangeAttemptLimiter attemptLimiter,
            PasswordChangeProperties properties
    ) {
        this.currentPasswordVerifier = currentPasswordVerifier;
        this.credentialUpdater = credentialUpdater;
        this.attemptLimiter = attemptLimiter;
        this.properties = properties;
    }

    @Override
    public Result changePassword(
            User user,
            String keycloakUsername,
            ChangePasswordRequest request,
            String clientAddress
    ) {
        validateRequest(request);

        String keycloakUserId = user == null ? null : normalize(user.getKeycloakId());
        String username = normalize(keycloakUsername);
        if (keycloakUserId == null || username == null) {
            throw PasswordChangeExceptions.keycloakUserNotFound();
        }

        String subjectKey = keycloakUserId + '|' + normalizeAddress(clientAddress);
        attemptLimiter.checkAllowed(subjectKey);

        if (!currentPasswordVerifier.verify(username, request.getCurrentPassword())) {
            attemptLimiter.recordFailure(subjectKey);
            throw PasswordChangeExceptions.invalidCurrentPassword();
        }

        attemptLimiter.reset(subjectKey);
        boolean sessionsRevoked = credentialUpdater.updatePasswordAndRevokeSessions(
                keycloakUserId,
                request.getNewPassword()
        );
        return new Result(sessionsRevoked);
    }

    private void validateRequest(ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw PasswordChangeExceptions.confirmationMismatch();
        }
        if (request.getNewPassword().equals(request.getCurrentPassword())) {
            throw PasswordChangeExceptions.samePassword();
        }

        String password = request.getNewPassword();
        if (password.length() < properties.getMinimumLength()
                || password.length() > properties.getMaximumLength()
                || password.chars().noneMatch(Character::isLowerCase)
                || password.chars().noneMatch(Character::isUpperCase)
                || password.chars().noneMatch(Character::isDigit)
                || password.chars().allMatch(Character::isLetterOrDigit)) {
            throw PasswordChangeExceptions.policyViolation(
                    "Use 12-128 characters with uppercase, lowercase, number, and special character"
            );
        }
    }

    private static String normalizeAddress(String clientAddress) {
        String normalized = normalize(clientAddress);
        return normalized == null ? "unknown" : normalized;
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
