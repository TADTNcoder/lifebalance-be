package com.lifebalance.identity.audit;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifebalance.identity.model.Permission;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PermissionAuditSnapshotMapper {

    private final ObjectMapper objectMapper;

    public String toJson(Permission permission) {
        try {
            return objectMapper.writeValueAsString(toSnapshot(permission));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize permission audit snapshot", exception);
        }
    }

    private Map<String, Object> toSnapshot(Permission permission) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", permission.getId());
        snapshot.put("code", permission.getCode());
        snapshot.put("name", permission.getName());
        snapshot.put("module", permission.getModule());
        snapshot.put("description", permission.getDescription());
        snapshot.put("system", permission.getSystem());
        snapshot.put("createdAt", permission.getCreatedAt());
        snapshot.put("updatedAt", permission.getUpdatedAt());
        return snapshot;
    }
}
