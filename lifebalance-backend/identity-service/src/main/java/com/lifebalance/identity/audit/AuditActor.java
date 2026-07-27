package com.lifebalance.identity.audit;

import java.util.UUID;

public record AuditActor(
        UUID id,
        String keycloakId,
        String username
) {
}
