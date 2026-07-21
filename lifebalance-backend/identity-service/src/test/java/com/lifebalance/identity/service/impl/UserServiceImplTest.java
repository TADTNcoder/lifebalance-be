package com.lifebalance.identity.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lifebalance.identity.dto.UpdateUserRequest;
import com.lifebalance.identity.dto.UserResponse;
import com.lifebalance.identity.exception.UserEmailAlreadyExistsException;
import com.lifebalance.identity.exception.UserNotFoundException;
import com.lifebalance.identity.exception.UserUsernameAlreadyExistsException;
import com.lifebalance.identity.exception.UserValidationException;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.model.enums.AccountStatus;
import com.lifebalance.identity.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void shouldReturnUserById() {
        UUID userId = UUID.randomUUID();
        User user = createUser(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserServiceImpl service = new UserServiceImpl(userRepository);

        UserResponse response = service.getUserById(userId);

        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getEmail()).isEqualTo("alice@example.com");
        assertThat(response.getUsername()).isEqualTo("alice");
        assertThat(response.getDisplayName()).isEqualTo("Alice");
        assertThat(response.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(response.getRegisteredAt()).isEqualTo(OffsetDateTime.parse("2026-07-20T10:15:30Z"));
        assertThat(response.getLastLoginAt()).isEqualTo(OffsetDateTime.parse("2026-07-21T11:20:30Z"));
    }

    @Test
    void shouldThrowWhenUserIsNotFoundById() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserServiceImpl service = new UserServiceImpl(userRepository);

        assertThatThrownBy(() -> service.getUserById(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found: " + userId);
    }

    @Test
    void shouldUpdateUserEmailUsernameAndDisplayName() {
        UUID userId = UUID.randomUUID();
        User user = createUser(userId);
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail(" New.Alice@Example.COM ");
        request.setUsername("  AliceUpdated  ");
        request.setDisplayName("  Alice Updated  ");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot("new.alice@example.com", userId))
                .thenReturn(false);
        when(userRepository.existsByUsernameAndIdNot("aliceupdated", userId))
                .thenReturn(false);
        when(userRepository.save(user)).thenAnswer(invocation -> invocation.getArgument(0));

        UserServiceImpl service = new UserServiceImpl(userRepository);

        UserResponse response = service.updateUser(userId, request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("new.alice@example.com");
        assertThat(userCaptor.getValue().getUsername()).isEqualTo("aliceupdated");
        assertThat(userCaptor.getValue().getDisplayName()).isEqualTo("Alice Updated");
        assertThat(response.getEmail()).isEqualTo("new.alice@example.com");
        assertThat(response.getUsername()).isEqualTo("aliceupdated");
        assertThat(response.getDisplayName()).isEqualTo("Alice Updated");
    }

    @Test
    void shouldKeepExistingValuesWhenPatchFieldsAreNull() {
        UUID userId = UUID.randomUUID();
        User user = createUser(userId);
        UpdateUserRequest request = new UpdateUserRequest();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenAnswer(invocation -> invocation.getArgument(0));

        UserServiceImpl service = new UserServiceImpl(userRepository);

        UserResponse response = service.updateUser(userId, request);

        verify(userRepository, never()).existsByEmailAndIdNot(
                anyString(),
                any()
        );
        verify(userRepository, never()).existsByUsernameAndIdNot(
                anyString(),
                any()
        );
        assertThat(response.getEmail()).isEqualTo("alice@example.com");
        assertThat(response.getUsername()).isEqualTo("alice");
        assertThat(response.getDisplayName()).isEqualTo("Alice");
    }

    @Test
    void shouldThrowWhenUpdateTargetDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest();
        request.setDisplayName("Alice Updated");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserServiceImpl service = new UserServiceImpl(userRepository);

        assertThatThrownBy(() -> service.updateUser(userId, request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found: " + userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRejectInvalidEmail() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("invalid-email");

        UserServiceImpl service = new UserServiceImpl(userRepository);

        assertThatThrownBy(() -> service.updateUser(UUID.randomUUID(), request))
                .isInstanceOf(UserValidationException.class)
                .hasMessage("Email must be valid");
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRejectBlankDisplayName() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setDisplayName(" ");

        UserServiceImpl service = new UserServiceImpl(userRepository);

        assertThatThrownBy(() -> service.updateUser(UUID.randomUUID(), request))
                .isInstanceOf(UserValidationException.class)
                .hasMessage("Display name must not be blank");
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRejectBlankUsername() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername(" ");

        UserServiceImpl service = new UserServiceImpl(userRepository);

        assertThatThrownBy(() -> service.updateUser(UUID.randomUUID(), request))
                .isInstanceOf(UserValidationException.class)
                .hasMessage("Username must not be blank");
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRejectDuplicateEmail() {
        UUID userId = UUID.randomUUID();
        User user = createUser(userId);
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("taken@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot("taken@example.com", userId))
                .thenReturn(true);

        UserServiceImpl service = new UserServiceImpl(userRepository);

        assertThatThrownBy(() -> service.updateUser(userId, request))
                .isInstanceOf(UserEmailAlreadyExistsException.class)
                .hasMessage("Email already exists: taken@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRejectDuplicateUsername() {
        UUID userId = UUID.randomUUID();
        User user = createUser(userId);
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("taken");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameAndIdNot("taken", userId))
                .thenReturn(true);

        UserServiceImpl service = new UserServiceImpl(userRepository);

        assertThatThrownBy(() -> service.updateUser(userId, request))
                .isInstanceOf(UserUsernameAlreadyExistsException.class)
                .hasMessage("Username already exists: taken");
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRejectMissingUserId() {
        UserServiceImpl service = new UserServiceImpl(userRepository);

        assertThatThrownBy(() -> service.getUserById(null))
                .isInstanceOf(UserValidationException.class)
                .hasMessage("User id is required");
        verify(userRepository, never()).findById(any());
    }

    private static User createUser(UUID userId) {
        User user = new User();
        user.setId(userId);
        user.setEmail("alice@example.com");
        user.setUsername("alice");
        user.setDisplayName("Alice");
        user.setStatus(AccountStatus.ACTIVE);
        user.setRegisteredAt(OffsetDateTime.parse("2026-07-20T10:15:30Z"));
        user.setLastLoginAt(OffsetDateTime.parse("2026-07-21T11:20:30Z"));

        return user;
    }
}
