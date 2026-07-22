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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lifebalance.identity.exception.UserInactiveException;
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
        return new CurrentUser(
                "kc-user-1",
                "alice",
                "alice@example.com",
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
