package com.lifebalance.task.controller;

import com.lifebalance.task.dto.response.TagResponse;
import com.lifebalance.task.service.TaskTagService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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
                AuthenticatedUserId.from(httpRequest),
                taskId,
                tagId);
    }

    @DeleteMapping("/{tagId}")
    public void removeTag(
            @PathVariable UUID taskId,
            @PathVariable UUID tagId,
            HttpServletRequest httpRequest) {

        taskTagService.removeTag(
                AuthenticatedUserId.from(httpRequest),
                taskId,
                tagId);
    }

    @GetMapping
    public List<TagResponse> getTags(
            @PathVariable UUID taskId,
            HttpServletRequest httpRequest) {

        return taskTagService.getTags(
                AuthenticatedUserId.from(httpRequest),
                taskId);
    }

}
