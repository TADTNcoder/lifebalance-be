package com.lifebalance.task.controller;

import com.lifebalance.security.keycloak.KeycloakUserMappingFilter;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import com.lifebalance.task.dto.response.TagResponse;
import com.lifebalance.task.service.TaskTagService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks/{taskId}/tags")
@RequiredArgsConstructor
public class TaskTagController {

    private final TaskTagService taskTagService;

    @PutMapping("/{tagId}")
    public void assignTag(
            @PathVariable UUID taskId,
            @PathVariable UUID tagId,
            HttpServletRequest httpRequest) {

        taskTagService.assignTag(
                getCurrentUserId(httpRequest),
                taskId,
                tagId);
    }

    @DeleteMapping("/{tagId}")
    public void removeTag(
            @PathVariable UUID taskId,
            @PathVariable UUID tagId,
            HttpServletRequest httpRequest) {

        taskTagService.removeTag(
                getCurrentUserId(httpRequest),
                taskId,
                tagId);
    }

    @GetMapping
    public List<TagResponse> getTags(
            @PathVariable UUID taskId,
            HttpServletRequest httpRequest) {

        return taskTagService.getTags(
                getCurrentUserId(httpRequest),
                taskId);
    }

    private UUID getCurrentUserId(HttpServletRequest request) {
        KeycloakUserPrincipal currentUser = (KeycloakUserPrincipal) request.getAttribute(
                KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE);

        if (currentUser == null || currentUser.userId() == null) {
            throw new AuthenticationCredentialsNotFoundException(
                    "Authenticated internal user id is required.");
        }

        return currentUser.userId();
    }
}
