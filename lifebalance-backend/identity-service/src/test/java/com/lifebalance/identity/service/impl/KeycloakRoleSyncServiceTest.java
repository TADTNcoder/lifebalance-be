package com.lifebalance.identity.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lifebalance.identity.config.KeycloakRoleSyncProperties;
import com.lifebalance.identity.exception.KeycloakRoleSyncException;
import com.lifebalance.identity.model.Role;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
class KeycloakRoleSyncServiceTest {

    @Mock
    private Keycloak keycloak;

    @Mock
    private RealmResource realmResource;

    @Mock
    private RolesResource rolesResource;

    @Mock
    private RoleResource roleResource;

    private KeycloakRoleSyncService service;

    @BeforeEach
    void setUp() {
        KeycloakRoleSyncProperties properties = new KeycloakRoleSyncProperties();
        properties.setRealm("lifebalance");

        when(keycloak.realm("lifebalance")).thenReturn(realmResource);
        when(realmResource.roles()).thenReturn(rolesResource);

        service = new KeycloakRoleSyncService(keycloak, properties);
    }

    @Test
    void shouldCreateRealmRole() {
        Role role = role("manager");

        service.syncCreatedRole(role);

        ArgumentCaptor<RoleRepresentation> captor =
                ArgumentCaptor.forClass(RoleRepresentation.class);
        verify(rolesResource).create(captor.capture());
        RoleRepresentation representation = captor.getValue();
        assertThat(representation.getName()).isEqualTo("manager");
        assertThat(representation.getDescription()).isEqualTo("Operational managers");
        assertThat(representation.getClientRole()).isFalse();
        assertThat(representation.getAttributes())
                .containsEntry("lifebalance.role.id", List.of(role.getId().toString()))
                .containsEntry("lifebalance.role.name", List.of("Manager"))
                .containsEntry("lifebalance.role.system", List.of("false"));
    }

    @Test
    void shouldUpdateExistingRoleWhenCreateReturnsConflict() {
        Role role = role("manager");
        RoleRepresentation existing = existingRepresentation();
        doThrow(new WebApplicationException(Response.status(Response.Status.CONFLICT).build()))
                .when(rolesResource)
                .create(any(RoleRepresentation.class));
        when(rolesResource.get("manager")).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(existing);

        service.syncCreatedRole(role);

        ArgumentCaptor<RoleRepresentation> captor =
                ArgumentCaptor.forClass(RoleRepresentation.class);
        verify(roleResource).update(captor.capture());
        assertThat(captor.getValue().getDescription()).isEqualTo("Operational managers");
        assertThat(captor.getValue().getAttributes())
                .containsEntry("external", List.of("preserved"))
                .containsEntry("lifebalance.role.name", List.of("Manager"));
    }

    @Test
    void shouldCreateRoleWhenUpdateFindsMissingRole() {
        Role role = role("manager");
        when(rolesResource.get("manager")).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenThrow(new NotFoundException());

        service.syncUpdatedRole(role);

        verify(rolesResource).create(any(RoleRepresentation.class));
        verify(roleResource, never()).update(any());
    }

    @Test
    void shouldIgnoreMissingRoleWhenDeleting() {
        Role role = role("manager");
        when(rolesResource.get("manager")).thenReturn(roleResource);
        doThrow(new NotFoundException()).when(roleResource).remove();

        service.syncDeletedRole(role);

        verify(roleResource).remove();
    }

    @Test
    void shouldThrowWhenCreateReturnsUnexpectedStatus() {
        Role role = role("manager");
        doThrow(new WebApplicationException(Response.serverError().build()))
                .when(rolesResource)
                .create(any(RoleRepresentation.class));

        assertThatThrownBy(() -> service.syncCreatedRole(role))
                .isInstanceOf(KeycloakRoleSyncException.class)
                .hasMessage("Failed to create Keycloak role manager: HTTP 500");
    }

    private static Role role(String code) {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setCode(code);
        role.setName("Manager");
        role.setDescription("Operational managers");
        role.setSystem(false);
        return role;
    }

    private static RoleRepresentation existingRepresentation() {
        RoleRepresentation representation = new RoleRepresentation();
        representation.setName("manager");
        representation.setDescription("Old description");
        representation.setAttributes(Map.of("external", List.of("preserved")));
        return representation;
    }
}
