package com.lifebalance.task.controller;

import com.lifebalance.task.error.TaskExceptions;
import java.util.Locale;
import java.util.Map;
import org.springframework.data.domain.Sort;

final class TaskSortCriteria {

    private static final Map<String, String> APPROVED_FIELDS = Map.ofEntries(
            Map.entry("createdat", "createdAt"),
            Map.entry("createdtime", "createdAt"),
            Map.entry("updatedat", "updatedAt"),
            Map.entry("deadline", "deadline"),
            Map.entry("priority", "priority"),
            Map.entry("status", "status"),
            Map.entry("scheduledat", "scheduledStartAt"),
            Map.entry("scheduledtime", "scheduledStartAt"),
            Map.entry("scheduledstartat", "scheduledStartAt"),
            Map.entry("name", "name"),
            Map.entry("progress", "progress")
    );

    private TaskSortCriteria() {
    }

    static Sort toSort(String sortBy, String sortDirection) {
        String property = resolveProperty(sortBy, sortDirection);
        Sort.Direction direction = resolveDirection(sortBy, sortDirection);

        return Sort.by(direction, property);
    }

    private static String resolveProperty(String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.isBlank()) {
            return "createdAt";
        }

        String property = APPROVED_FIELDS.get(sortBy.trim().toLowerCase(Locale.ROOT));
        if (property == null) {
            throw TaskExceptions.invalidSortCriteria(sortBy, sortDirection);
        }
        return property;
    }

    private static Sort.Direction resolveDirection(String sortBy, String sortDirection) {
        if (sortDirection == null || sortDirection.isBlank()) {
            return Sort.Direction.DESC;
        }
        String normalizedDirection = sortDirection.trim().toUpperCase(Locale.ROOT);
        if ("ASC".equals(normalizedDirection)) {
            return Sort.Direction.ASC;
        }
        if ("DESC".equals(normalizedDirection)) {
            return Sort.Direction.DESC;
        }

        throw TaskExceptions.invalidSortCriteria(sortBy, sortDirection);
    }
}
