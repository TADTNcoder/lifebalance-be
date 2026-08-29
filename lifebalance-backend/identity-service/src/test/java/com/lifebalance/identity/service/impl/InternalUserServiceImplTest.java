package com.lifebalance.identity.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lifebalance.identity.dto.UpdateUserRequest;
import com.lifebalance.identity.exception.UserEmailAlreadyExistsException;
import com.lifebalance.identity.exception.UserInactiveException;
import com.lifebalance.identity.exception.UserNotFoundException;
import com.lifebalance.identity.exception.UserUsernameAlreadyExistsException;
import com.lifebalance.identity.exception.UserValidationException;
import com.lifebalance.identity.model.Role;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.model.UserRole;
import com.lifebalance.identity.model.enums.AccountStatus;
import com.lifebalance.identity.repository.RoleRepository;
import com.lifebalance.identity.repository.UserRepository;
import com.lifebalance.identity.repository.UserRoleRepository;
import com.lifebalance.identity.security.CurrentUser;
import com.lifebalance.identity.service.UserAuthorizationCacheService;

@ExtendWith(MockitoExtension.class)
class InternalUserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private UserAuthorizationCacheService userAuthorizationCacheService;

    @BeforeEach
    void setUp() {
        lenient().when(roleRepository.findByCodesIgnoreCase(any())).thenReturn(List.of());
        lenient().when(userRoleRepository.findByUserId(any())).thenReturn(List.of());
    }

    @Test
    void shouldReturnExistingActiveUser() {
        CurrentUser currentUser = createCurrentUser();
        User user = createUser(AccountStatus.ACTIVE);

        when(userRepository.findByKeycloakId("kc-user-1")).thenReturn(Optional.of(user));

        InternalUserServiceImpl service = createService();

        assertThat(service.findOrCreate(currentUser)).isSameAs(user);
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldCreateUserWithGeneratedInternalIdWhenUuidKeycloakSubjectIsMissing() {
        String keycloakSubject = "5d79e9ef-043f-46b3-af48-60f63cc12e90";
        CurrentUser currentUser = createCurrentUser(keycloakSubject, " Alice ", " Alice@Example.COM ");

        when(userRepository.findByKeycloakId(keycloakSubject)).thenReturn(Optional.empty());
        when(userRepository.existsDeletedByKeycloakId(keycloakSubject)).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User newUser = invocation.getArgument(0);
            assertThat(newUser.getId()).isNull();
            newUser.setId(UUID.randomUUID());
            return newUser;
        });

        InternalUserServiceImpl service = createService();

        User user = service.findOrCreate(currentUser);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(user).isSameAs(userCaptor.getValue());
        assertThat(user.getKeycloakId()).isEqualTo(keycloakSubject);
        assertThat(user.getId()).isNotEqualTo(UUID.fromString(keycloakSubject));
        assertThat(user.getEmail()).isEqualTo("alice@example.com");
        assertThat(user.getUsername()).isEqualTo("alice");
    }

    @Test
    void shouldPreserveUpdatedProfileWhenTokenStillContainsOldClaims() {
        CurrentUser currentUser = createCurrentUser("kc-user-1", " Alice.Updated ", " Alice.Updated@Example.COM ");
        User user = createUser(AccountStatus.ACTIVE);

        when(userRepository.findByKeycloakId("kc-user-1")).thenReturn(Optional.of(user));

        InternalUserServiceImpl service = createService();

        User result = service.findOrCreate(currentUser);

        assertThat(result).isSameAs(user);
        assertThat(user.getEmail()).isEqualTo("alice@example.com");
        assertThat(user.getUsername()).isEqualTo("alice");
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRejectDuplicateEmailWhenCreatingFromKeycloakClaims() {
        CurrentUser currentUser = createCurrentUser("kc-user-1", "alice", "alice@example.com");

        when(userRepository.findByKeycloakId("kc-user-1")).thenReturn(Optional.empty());
        when(userRepository.existsDeletedByKeycloakId("kc-user-1")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        InternalUserServiceImpl service = createService();

        assertThatThrownBy(() -> service.findOrCreate(currentUser))
                .isInstanceOf(UserEmailAlreadyExistsException.class)
                .hasMessage("Email already exists: alice@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldNotValidateStaleTokenClaimsForExistingUser() {
        CurrentUser currentUser = createCurrentUser("kc-user-1", "taken", "alice@example.com");
        User user = createUser(AccountStatus.ACTIVE);

        when(userRepository.findByKeycloakId("kc-user-1")).thenReturn(Optional.of(user));

        InternalUserServiceImpl service = createService();

        assertThat(service.findOrCreate(currentUser)).isSameAs(user);
        assertThat(user.getUsername()).isEqualTo("alice");
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRejectMissingEmailClaim() {
        CurrentUser currentUser = createCurrentUser("kc-user-1", "alice", null);

        InternalUserServiceImpl service = createService();

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

        InternalUserServiceImpl service = createService();

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

        InternalUserServiceImpl service = createService();

        assertThatThrownBy(() -> service.findOrCreate(currentUser))
                .isInstanceOf(UserInactiveException.class)
                .hasMessage("User account is not active: DELETED");
        verify(userRepository, never()).save(any());
    }

    @Test
    void getCurrentUserShouldThrowDomainExceptionWhenKeycloakUserIsMissing() {
        CurrentUser currentUser = createCurrentUser();

        when(userRepository.findByKeycloakId("kc-user-1")).thenReturn(Optional.empty());

        InternalUserServiceImpl service = createService();

        assertThatThrownBy(() -> service.getCurrentUser(currentUser))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found for Keycloak subject: kc-user-1");
    }

    @Test
    void updateCurrentUserShouldThrowDomainExceptionWhenKeycloakUserIsMissing() {
        CurrentUser currentUser = createCurrentUser();
        UpdateUserRequest request = new UpdateUserRequest();
        request.setDisplayName("Alice Nguyen");
        request.setEmail("alice.nguyen@example.com");

        when(userRepository.findByKeycloakId("kc-user-1")).thenReturn(Optional.empty());

        InternalUserServiceImpl service = createService();

        assertThatThrownBy(() -> service.updateCurrentUser(currentUser, request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found for Keycloak subject: kc-user-1");
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateCurrentUserShouldPersistProfileDetails() {
        CurrentUser currentUser = createCurrentUser();
        User user = createUser(AccountStatus.ACTIVE);
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("alice-updated");
        request.setDisplayName("Alice Updated");
        request.setEmail("alice.updated@example.com");
        request.setPhone("+84 912 345 678");
        request.setGender("Nữ");
        request.setBirthDate(LocalDate.of(1998, 5, 20));

        when(userRepository.findByKeycloakId("kc-user-1")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = createService().updateCurrentUser(currentUser, request);

        assertThat(updated.getUsername()).isEqualTo("alice-updated");
        assertThat(updated.getDisplayName()).isEqualTo("Alice Updated");
        assertThat(updated.getEmail()).isEqualTo("alice.updated@example.com");
        assertThat(updated.getPhone()).isEqualTo("+84 912 345 678");
        assertThat(updated.getGender()).isEqualTo("Nữ");
        assertThat(updated.getBirthDate()).isEqualTo(LocalDate.of(1998, 5, 20));
        verify(userRepository).save(user);
    }

    @Test
    void shouldAssignKnownKeycloakRolesWhenCreatingUserFromToken() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        CurrentUser currentUser = createCurrentUser("kc-user-1", "alice", "alice@example.com", List.of("USER", "offline_access"));
        Role userRole = createRole(roleId, "USER");

        when(userRepository.findByKeycloakId("kc-user-1")).thenReturn(Optional.empty());
        when(userRepository.existsDeletedByKeycloakId("kc-user-1")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(userId);
            return user;
        });
        when(roleRepository.findByCodesIgnoreCase(any())).thenReturn(List.of(userRole));

        User user = createService(true).findOrCreate(currentUser);

        ArgumentCaptor<List<UserRole>> userRolesCaptor = ArgumentCaptor.forClass(List.class);
        verify(userRoleRepository).saveAll(userRolesCaptor.capture());
        UserRole assignedRole = userRolesCaptor.getValue().getFirst();
        assertThat(user.getId()).isEqualTo(userId);
        assertThat(assignedRole.getId().getUserId()).isEqualTo(userId);
        assertThat(assignedRole.getId().getRoleId()).isEqualTo(roleId);
        assertThat(assignedRole.getRole()).isSameAs(userRole);
        verify(userAuthorizationCacheService).evictUser(userId);
    }

    @Test
    void shouldAssignPrefixedKeycloakRolesWhenCreatingUserFromToken() {
        UUID userId = UUID.randomUUID();
        UUID adminRoleId = UUID.randomUUID();
        UUID userRoleId = UUID.randomUUID();
        CurrentUser currentUser = createCurrentUser(
                "kc-user-1",
                "alice",
                "alice@example.com",
                List.of("ROLE_ADMIN", "ROLE-user", "offline_access")
        );
        Role adminRole = createRole(adminRoleId, "admin");
        Role standardUserRole = createRole(userRoleId, "user");

        when(userRepository.findByKeycloakId("kc-user-1")).thenReturn(Optional.empty());
        when(userRepository.existsDeletedByKeycloakId("kc-user-1")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(userId);
            return user;
        });
        when(roleRepository.findByCodesIgnoreCase(any())).thenReturn(List.of(adminRole, standardUserRole));

        createService(true).findOrCreate(currentUser);

        ArgumentCaptor<Collection<String>> roleCodesCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(roleRepository).findByCodesIgnoreCase(roleCodesCaptor.capture());
        assertThat(roleCodesCaptor.getValue()).containsExactly("admin", "user", "offline_access");

        ArgumentCaptor<List<UserRole>> userRolesCaptor = ArgumentCaptor.forClass(List.class);
        verify(userRoleRepository).saveAll(userRolesCaptor.capture());
        assertThat(userRolesCaptor.getValue())
                .extracting(userRole -> userRole.getRole().getId())
                .containsExactlyInAnyOrder(adminRoleId, userRoleId);
        verify(userAuthorizationCacheService).evictUser(userId);
    }

    @Test
    void shouldRemoveRolesMissingFromKeycloakTokenForExistingUser() {
        UUID userId = UUID.randomUUID();
        UUID staleRoleId = UUID.randomUUID();
        CurrentUser currentUser = createCurrentUser("kc-user-1", "alice", "alice@example.com", List.of("offline_access"));
        User user = createUser(AccountStatus.ACTIVE);
        user.setId(userId);
        Role staleRole = createRole(staleRoleId, "MANAGER");

        when(userRepository.findByKeycloakId("kc-user-1")).thenReturn(Optional.of(user));
        when(roleRepository.findByCodesIgnoreCase(any())).thenReturn(List.of());
        when(userRoleRepository.findByUserId(userId)).thenReturn(List.of(UserRole.builder()
                .role(staleRole)
                .user(user)
                .id(new com.lifebalance.identity.model.UserRoleId(userId, staleRoleId))
                .build()));

        createService(true).findOrCreate(currentUser);

        verify(userRoleRepository).deleteByUserIdAndRoleIds(userId, List.of(staleRoleId));
        verify(userAuthorizationCacheService).evictUser(userId);
    }

    @Test
    void shouldAssignDefaultUserRoleWhenTokenRoleSyncIsDisabled() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        CurrentUser currentUser = createCurrentUser("kc-user-1", "alice", "alice@example.com", List.of("ADMIN"));
        Role userRole = createRole(roleId, "USER");

        when(userRepository.findByKeycloakId("kc-user-1")).thenReturn(Optional.empty());
        when(userRepository.existsDeletedByKeycloakId("kc-user-1")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(userId);
            return user;
        });
        when(roleRepository.findByCode("USER")).thenReturn(Optional.of(userRole));
        when(userRoleRepository.existsByUserIdAndRoleId(userId, roleId)).thenReturn(false);

        createService().findOrCreate(currentUser);

        ArgumentCaptor<UserRole> userRoleCaptor = ArgumentCaptor.forClass(UserRole.class);
        verify(userRoleRepository).save(userRoleCaptor.capture());
        assertThat(userRoleCaptor.getValue().getId().getUserId()).isEqualTo(userId);
        assertThat(userRoleCaptor.getValue().getId().getRoleId()).isEqualTo(roleId);
        verify(userAuthorizationCacheService).evictUser(userId);
        verify(roleRepository, never()).findByCodesIgnoreCase(any());
    }

    private static CurrentUser createCurrentUser() {
        return createCurrentUser("kc-user-1", "alice", "alice@example.com");
    }

    private static CurrentUser createCurrentUser(String userId, String username, String email) {
        return createCurrentUser(userId, username, email, List.of("user"));
    }

    private static CurrentUser createCurrentUser(
            String userId,
            String username,
            String email,
            List<String> roles
    ) {
        return new CurrentUser(
                userId,
                username,
                email,
                roles
        );
    }

    private InternalUserServiceImpl createService() {
        return createService(false);
    }

    private InternalUserServiceImpl createService(boolean tokenRoleSyncEnabled) {
        return new InternalUserServiceImpl(
                userRepository,
                roleRepository,
                userRoleRepository,
                userAuthorizationCacheService,
                tokenRoleSyncEnabled
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

    private static Role createRole(UUID roleId, String code) {
        Role role = new Role();
        role.setId(roleId);
        role.setCode(code);
        role.setName(code);
        role.setSystem(false);
        return role;
    }
}
