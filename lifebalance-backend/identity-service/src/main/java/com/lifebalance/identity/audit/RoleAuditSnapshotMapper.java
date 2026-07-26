package com.lifebalance.identity.audit;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifebalance.identity.model.Permission;
import com.lifebalance.identity.model.Role;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RoleAuditSnapshotMapper {

    private final ObjectMapper objectMapper;

    public String toJson(Role role, List<Permission> permissions) {
        try {
            return objectMapper.writeValueAsString(toSnapshot(role, permissions));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize role audit snapshot", exception);
        }
    }

    private Map<String, Object> toSnapshot(Role role, List<Permission> permissions) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", role.getId());
        snapshot.put("code", role.getCode());
        snapshot.put("name", role.getName());
        snapshot.put("description", role.getDescription());
        snapshot.put("system", role.getSystem());
        snapshot.put("permissions", permissionSnapshots(permissions));
        snapshot.put("createdAt", role.getCreatedAt());
        snapshot.put("updatedAt", role.getUpdatedAt());
        return snapshot;
    }

    private List<Map<String, Object>> permissionSnapshots(List<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return List.of();
        }

        return permissions.stream()
                .sorted(Comparator
                        .comparing(Permission::getModule, Comparator.nullsLast(String::compareTo))
                        .thenComparing(Permission::getCode, Comparator.nullsLast(String::compareTo)))
                .map(this::permissionSnapshot)
                .toList();
    }

    private Map<String, Object> permissionSnapshot(Permission permission) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", permission.getId());
        snapshot.put("code", permission.getCode());
        snapshot.put("name", permission.getName());
        snapshot.put("module", permission.getModule());
        snapshot.put("description", permission.getDescription());
        snapshot.put("system", permission.getSystem());
        return snapshot;
    }
}
