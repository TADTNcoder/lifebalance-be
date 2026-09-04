package com.lifebalance.task.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lifebalance.common.web.PageableLimits;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import com.lifebalance.task.dto.response.TaskResponse;
import com.lifebalance.task.model.enums.PriorityLevel;
import com.lifebalance.task.model.enums.TaskStatus;
import com.lifebalance.task.service.TaskService;

import lombok.RequiredArgsConstructor;

/**
 * Read-only task administration API used by the admin Business Management page.
 * The normal {@code /api/tasks} endpoint remains owner-scoped.
 */
@RestController
@RequestMapping("/api/tasks/admin")
@RequiredArgsConstructor
public class AdminTaskController {

    private final TaskService taskService;

    @GetMapping
    public Page<TaskResponse> searchAll(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) PriorityLevel priority,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) LocalDate deadlineFrom,
            @RequestParam(required = false) LocalDate deadlineTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false)
            KeycloakUserPrincipal currentUser) {

        requireAdmin(currentUser);

        Pageable pageable = PageableLimits.of(
                page,
                size,
                TaskSortCriteria.toSort(sortBy, sortDirection));

        return taskService.searchAllForAdmin(
                keyword,
                status,
                priority,
                categoryId,
                deadlineFrom,
                deadlineTo,
                pageable);
    }

    private static void requireAdmin(KeycloakUserPrincipal currentUser) {
        if (currentUser == null || currentUser.userId() == null) {
            throw new AuthenticationCredentialsNotFoundException(
                    "Authenticated internal user id is required.");
        }

        boolean admin = hasAdminRole(currentUser.roles())
                || hasAdminRole(currentUser.realmRoles())
                || hasAdminRole(currentUser.clientRoles());

        if (!admin) {
            throw new AccessDeniedException("ADMIN role is required to inspect all tasks.");
        }
    }

    private static boolean hasAdminRole(Iterable<String> roles) {
        if (roles == null) {
            return false;
        }

        for (String role : roles) {
            if (role == null) {
                continue;
            }

            String normalized = role.trim();
            if ("ADMIN".equalsIgnoreCase(normalized)
                    || "ROLE_ADMIN".equalsIgnoreCase(normalized)) {
                return true;
            }
        }

        return false;
    }
}
