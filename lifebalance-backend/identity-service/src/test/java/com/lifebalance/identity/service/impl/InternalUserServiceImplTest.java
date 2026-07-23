package com.lifebalance.identity.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lifebalance.identity.exception.UserEmailAlreadyExistsException;
import com.lifebalance.identity.exception.UserInactiveException;
import com.lifebalance.identity.exception.UserUsernameAlreadyExistsException;
import com.lifebalance.identity.exception.UserValidationException;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.model.enums.AccountStatus;
import com.lifebalance.identity.repository.UserRepository;
import com.lifebalance.identity.security.CurrentUser;

@ExtendWith(MockitoExtension.class)
class InternalUserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void shouldReturnExistingActiveUser() {
        CurrentUser currentUser = createCurrentUser();
        User user = createUser(AccountStatus.ACTIVE);

        when(userRepository.findByKeycloakId("kc-user-1")).thenReturn(Optional.of(user));

        InternalUserServiceImpl service = new InternalUserServiceImpl(userRepository);

        assertThat(service.findOrCreate(currentUser)).isSameAs(user);
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldCreateUserFromKeycloakClaimsWhenMissing() {
        CurrentUser currentUser = createCurrentUser("kc-user-1", " Alice ", " Alice@Example.COM ");

        when(userRepository.findByKeycloakId("kc-user-1")).thenReturn(Optional.empty());
        when(userRepository.existsDeletedByKeycloakId("kc-user-1")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InternalUserServiceImpl service = new InternalUserServiceImpl(userRepository);

        User user = service.findOrCreate(currentUser);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(user).isSameAs(userCaptor.getValue());
        assertThat(user.getKeycloakId()).isEqualTo("kc-user-1");
        assertThat(user.getEmail()).isEqualTo("alice@example.com");
        assertThat(user.getUsername()).isEqualTo("alice");
    }

    @Test
    void shouldSyncChangedKeycloakClaimsForExistingActiveUser() {
        CurrentUser currentUser = createCurrentUser("kc-user-1", " Alice.Updated ", " Alice.Updated@Example.COM ");
        User user = createUser(AccountStatus.ACTIVE);

        when(userRepository.findByKeycloakId("kc-user-1")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot("alice.updated@example.com", user.getId()))
                .thenReturn(false);
        when(userRepository.existsByUsernameAndIdNot("alice.updated", user.getId()))
                .thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        InternalUserServiceImpl service = new InternalUserServiceImpl(userRepository);

        User result = service.findOrCreate(currentUser);

        assertThat(result).isSameAs(user);
        assertThat(user.getEmail()).isEqualTo("alice.updated@example.com");
        assertThat(user.getUsername()).isEqualTo("alice.updated");
        verify(userRepository).save(user);
    }

    @Test
    void shouldRejectDuplicateEmailWhenCreatingFromKeycloakClaims() {
        CurrentUser currentUser = createCurrentUser("kc-user-1", "alice", "alice@example.com");

        when(userRepository.findByKeycloakId("kc-user-1")).thenReturn(Optional.empty());
        when(userRepository.existsDeletedByKeycloakId("kc-user-1")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        InternalUserServiceImpl service = new InternalUserServiceImpl(userRepository);

        assertThatThrownBy(() -> service.findOrCreate(currentUser))
                .isInstanceOf(UserEmailAlreadyExistsException.class)
                .hasMessage("Email already exists: alice@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRejectDuplicateUsernameWhenSyncingKeycloakClaims() {
        CurrentUser currentUser = createCurrentUser("kc-user-1", "taken", "alice@example.com");
        User user = createUser(AccountStatus.ACTIVE);

        when(userRepository.findByKeycloakId("kc-user-1")).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameAndIdNot("taken", user.getId()))
                .thenReturn(true);

        InternalUserServiceImpl service = new InternalUserServiceImpl(userRepository);

        assertThatThrownBy(() -> service.findOrCreate(currentUser))
                .isInstanceOf(UserUsernameAlreadyExistsException.class)
                .hasMessage("Username already exists: taken");
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRejectMissingEmailClaim() {
        CurrentUser currentUser = createCurrentUser("kc-user-1", "alice", null);

        InternalUserServiceImpl service = new InternalUserServiceImpl(userRepository);

        assertThatThrownBy(() -> service.findOrCreate(currentUser))
                .isInstanceOf(UserValidationException.class)
                .hasMessage("Email is required");
        verify(userRepository, never()).findByKeycloakId(any());
    }

    @Test
    void shouldRejectExistingDisabledUser() {
        CurrentUser currentUser = createCurrentUser();
        User user = createUser(AccountStatus.DISABLED);

        when(userRepository.findByKeycloakId("kc-user-1")).thenReturn(Optional.of(user));

        InternalUserServiceImpl service = new InternalUserServiceImpl(userRepository);

        assertThatThrownBy(() -> service.findOrCreate(currentUser))
                .isInstanceOf(UserInactiveException.class)
                .hasMessage("User account is not active: DISABLED");
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRejectSoftDeletedUserAndNotRecreateFromToken() {
        CurrentUser currentUser = createCurrentUser();

        when(userRepository.findByKeycloakId("kc-user-1")).thenReturn(Optional.empty());
        when(userRepository.existsDeletedByKeycloakId("kc-user-1")).thenReturn(true);

        InternalUserServiceImpl service = new InternalUserServiceImpl(userRepository);

        assertThatThrownBy(() -> service.findOrCreate(currentUser))
                .isInstanceOf(UserInactiveException.class)
                .hasMessage("User account is not active: DELETED");
        verify(userRepository, never()).save(any());
    }

    private static CurrentUser createCurrentUser() {
        return createCurrentUser("kc-user-1", "alice", "alice@example.com");
    }

    private static CurrentUser createCurrentUser(String userId, String username, String email) {
        return new CurrentUser(
                userId,
                username,
                email,
                List.of("user")
        );
    }

    private static User createUser(AccountStatus status) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setKeycloakId("kc-user-1");
        user.setEmail("alice@example.com");
        user.setUsername("alice");
        user.setStatus(status);

        return user;
    }
}
