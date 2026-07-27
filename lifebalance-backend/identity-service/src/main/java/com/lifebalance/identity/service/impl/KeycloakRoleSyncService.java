package com.lifebalance.identity.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.RoleRepresentation;

import com.lifebalance.identity.config.KeycloakRoleSyncProperties;
import com.lifebalance.identity.exception.KeycloakRoleSyncException;
import com.lifebalance.identity.model.Role;
import com.lifebalance.identity.service.RoleSyncService;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class KeycloakRoleSyncService implements RoleSyncService {

    private static final int HTTP_CONFLICT = 409;

    private static final String ATTRIBUTE_ROLE_ID = "lifebalance.role.id";
    private static final String ATTRIBUTE_ROLE_NAME = "lifebalance.role.name";
    private static final String ATTRIBUTE_SYSTEM_ROLE = "lifebalance.role.system";

    private final Keycloak keycloak;
    private final KeycloakRoleSyncProperties properties;

    @Override
    public void syncCreatedRole(Role role) {
        RoleRepresentation representation = newRoleRepresentation(role);
        try {
            roles().create(representation);
        } catch (WebApplicationException exception) {
            if (status(exception) == HTTP_CONFLICT) {
                updateExistingRole(role);
                return;
            }

            throw failure("create", roleName(role), exception);
        } catch (ProcessingException exception) {
            throw failure("create", roleName(role), exception);
        }
    }

    @Override
    public void syncUpdatedRole(Role role) {
        try {
            updateExistingRole(role);
        } catch (NotFoundException exception) {
            syncCreatedRole(role);
        } catch (WebApplicationException exception) {
            throw failure("update", roleName(role), exception);
        } catch (ProcessingException exception) {
            throw failure("update", roleName(role), exception);
        }
    }

    @Override
    public void syncDeletedRole(Role role) {
        String name = roleName(role);
        try {
            roles().get(name).remove();
        } catch (NotFoundException ignored) {
            // Deleting an already-missing role is idempotent for DB-to-Keycloak sync.
        } catch (WebApplicationException exception) {
            throw failure("delete", name, exception);
        } catch (ProcessingException exception) {
            throw failure("delete", name, exception);
        }
    }

    @Override
    public int syncAllRoles(Collection<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return 0;
        }

        int synced = 0;
        for (Role role : roles) {
            syncCreatedRole(role);
            synced++;
        }

        return synced;
    }

    private void updateExistingRole(Role role) {
        RoleResource roleResource = roles().get(roleName(role));
        RoleRepresentation representation = roleResource.toRepresentation();
        representation.setDescription(description(role));
        representation.setAttributes(mergedAttributes(representation.getAttributes(), role));
        roleResource.update(representation);
    }

    private RolesResource roles() {
        return keycloak.realm(properties.getRealm()).roles();
    }

    private RoleRepresentation newRoleRepresentation(Role role) {
        RoleRepresentation representation = new RoleRepresentation();
        representation.setName(roleName(role));
        representation.setDescription(description(role));
        representation.setClientRole(false);
        representation.setComposite(false);
        representation.setAttributes(mergedAttributes(Map.of(), role));

        return representation;
    }

    private Map<String, List<String>> mergedAttributes(
            Map<String, List<String>> currentAttributes,
            Role role
    ) {
        Map<String, List<String>> attributes = new LinkedHashMap<>();
        if (currentAttributes != null) {
            currentAttributes.forEach((key, value) -> attributes.put(key, copy(value)));
        }

        if (role.getId() != null) {
            attributes.put(ATTRIBUTE_ROLE_ID, List.of(role.getId().toString()));
        }
        attributes.put(ATTRIBUTE_ROLE_NAME, List.of(nullToEmpty(role.getName())));
        attributes.put(ATTRIBUTE_SYSTEM_ROLE, List.of(Boolean.TRUE.equals(role.getSystem()) ? "true" : "false"));

        return attributes;
    }

    private static List<String> copy(List<String> values) {
        return values == null ? List.of() : new ArrayList<>(values);
    }

    private static String description(Role role) {
        String description = trimToNull(role.getDescription());
        if (description != null) {
            return description;
        }

        return role.getName();
    }

    private static String roleName(Role role) {
        if (role == null || trimToNull(role.getCode()) == null) {
            throw new KeycloakRoleSyncException("Cannot sync role without code");
        }

        return role.getCode().trim();
    }

    private static int status(WebApplicationException exception) {
        Response response = exception.getResponse();
        return response == null ? -1 : response.getStatus();
    }

    private static KeycloakRoleSyncException failure(
            String action,
            String roleName,
            RuntimeException exception
    ) {
        int status = exception instanceof WebApplicationException webApplicationException
                ? status(webApplicationException)
                : -1;
        String statusText = status > 0 ? "HTTP " + status : exception.getClass().getSimpleName();

        return new KeycloakRoleSyncException(
                "Failed to " + action + " Keycloak role " + roleName + ": " + statusText
        );
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
