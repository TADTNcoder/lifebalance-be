package com.lifebalance.task.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import com.lifebalance.task.dto.response.TaskResponse;
import com.lifebalance.task.model.enums.PriorityLevel;
import com.lifebalance.task.model.enums.TaskStatus;
import com.lifebalance.task.service.TaskService;

class AdminTaskControllerTest {

    private final TaskService taskService = mock(TaskService.class);
    private final AdminTaskController controller = new AdminTaskController(taskService);

    @Test
    void adminCanSearchTasksAcrossOwners() {
        LocalDate deadlineFrom = LocalDate.of(2026, 9, 1);
        LocalDate deadlineTo = LocalDate.of(2026, 9, 30);
        Page<TaskResponse> expected = new PageImpl<>(List.of(new TaskResponse()));
        Pageable expectedPageable = PageRequest.of(
                0,
                100,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        when(taskService.searchAllForAdmin(
                "report",
                TaskStatus.PLANNED,
                PriorityLevel.HIGH,
                null,
                deadlineFrom,
                deadlineTo,
                expectedPageable))
                .thenReturn(expected);

        Page<TaskResponse> actual = controller.searchAll(
                "report",
                TaskStatus.PLANNED,
                PriorityLevel.HIGH,
                null,
                deadlineFrom,
                deadlineTo,
                0,
                500,
                "createdAt",
                "DESC",
                principal(Set.of(), Set.of(" admin "), Set.of()));

        assertThat(actual).isSameAs(expected);
        verify(taskService).searchAllForAdmin(
                "report",
                TaskStatus.PLANNED,
                PriorityLevel.HIGH,
                null,
                deadlineFrom,
                deadlineTo,
                expectedPageable);
    }

    @Test
    void nonAdminCannotSearchTasksAcrossOwners() {
        assertThatThrownBy(() -> controller.searchAll(
                "",
                null,
                null,
                null,
                null,
                null,
                0,
                10,
                "createdAt",
                "DESC",
                principal(Set.of("USER"), Set.of(), Set.of())))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("ADMIN role is required to inspect all tasks.");

        verifyNoInteractions(taskService);
    }

    @Test
    void missingMappedPrincipalIsUnauthenticated() {
        assertThatThrownBy(() -> controller.searchAll(
                "",
                null,
                null,
                null,
                null,
                null,
                0,
                10,
                "createdAt",
                "DESC",
                null))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class)
                .hasMessage("Authenticated internal user id is required.");

        verifyNoInteractions(taskService);
    }

    private static KeycloakUserPrincipal principal(
            Set<String> roles,
            Set<String> realmRoles,
            Set<String> clientRoles) {
        return new KeycloakUserPrincipal(
                "keycloak-admin",
                UUID.randomUUID(),
                "admin",
                "admin@example.com",
                "LifeBalance Admin",
                "LifeBalance",
                "Admin",
                "lifebalance-web",
                Set.of("lifebalance-api"),
                realmRoles,
                clientRoles,
                roles);
    }
}
