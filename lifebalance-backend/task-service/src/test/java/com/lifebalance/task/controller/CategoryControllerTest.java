package com.lifebalance.task.controller;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import com.lifebalance.task.dto.request.CreateCategoryRequest;
import com.lifebalance.task.service.CategoryService;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

class CategoryControllerTest {

    private final CategoryService categoryService = mock(CategoryService.class);
    private final CategoryController controller = new CategoryController(categoryService);

    @Test
    void regularUserRoleCanCreateCategory() {
        UUID userId = UUID.randomUUID();
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Cá nhân");
        KeycloakUserPrincipal currentUser = principal(userId, Set.of("user"));

        controller.create(request, currentUser);

        verify(categoryService).create(userId, request);
    }

    @Test
    void authenticatedUserIdIsRequired() {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Cá nhân");

        assertThatThrownBy(() -> controller.create(request, null))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    private static KeycloakUserPrincipal principal(UUID userId, Set<String> roles) {
        return new KeycloakUserPrincipal(
                "subject",
                userId,
                "user",
                "user@example.com",
                "User",
                "User",
                null,
                "lifebalance-web",
                Set.of("lifebalance-web"),
                roles,
                Set.of(),
                roles);
    }
}
