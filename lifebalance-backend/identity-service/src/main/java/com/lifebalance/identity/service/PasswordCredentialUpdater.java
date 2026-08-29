package com.lifebalance.identity.service;

public interface PasswordCredentialUpdater {

    boolean updatePasswordAndRevokeSessions(String keycloakUserId, String newPassword);
}
