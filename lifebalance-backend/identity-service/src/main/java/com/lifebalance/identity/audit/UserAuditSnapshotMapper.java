package com.lifebalance.identity.audit;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifebalance.identity.model.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserAuditSnapshotMapper {

    private final ObjectMapper objectMapper;

    public String toJson(User user) {
        try {
            return objectMapper.writeValueAsString(toSnapshot(user));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize user audit snapshot", exception);
        }
    }

    private Map<String, Object> toSnapshot(User user) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", user.getId());
        snapshot.put("keycloakId", user.getKeycloakId());
        snapshot.put("email", user.getEmail());
        snapshot.put("username", user.getUsername());
        snapshot.put("displayName", user.getDisplayName());
        snapshot.put("phone", user.getPhone());
        snapshot.put("gender", user.getGender());
        snapshot.put("birthDate", user.getBirthDate());
        snapshot.put("status", user.getStatus());
        snapshot.put("registeredAt", user.getRegisteredAt());
        snapshot.put("lastLoginAt", user.getLastLoginAt());
        snapshot.put("lockReason", user.getLockReason());
        snapshot.put("lockedAt", user.getLockedAt());
        snapshot.put("lockedUntil", user.getLockedUntil());
        snapshot.put("lockedByKeycloakId", user.getLockedByKeycloakId());
        snapshot.put("tokenValidAfter", user.getTokenValidAfter());
        snapshot.put("createdAt", user.getCreatedAt());
        snapshot.put("updatedAt", user.getUpdatedAt());
        return snapshot;
    }
}
