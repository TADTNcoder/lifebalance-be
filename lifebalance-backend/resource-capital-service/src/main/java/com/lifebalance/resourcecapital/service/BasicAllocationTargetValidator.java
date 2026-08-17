package com.lifebalance.resourcecapital.service;

import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationTargetType;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.AllocationTargetUnavailableException;
import com.lifebalance.resourcecapital.domain.capitalallocation.exception.InvalidAllocationTargetException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Service
public class BasicAllocationTargetValidator implements AllocationTargetValidator {

    private final RestClient taskServiceClient;

    public BasicAllocationTargetValidator(
            RestClient.Builder restClientBuilder,
            @Value("${lifebalance.integration.task-service.base-url:http://task-service:8080}")
            String taskServiceBaseUrl
    ) {
        this.taskServiceClient = restClientBuilder
                .baseUrl(normalizeBaseUrl(taskServiceBaseUrl))
                .build();
    }

    @Override
    public void validateTarget(UUID ownerId, AllocationTargetType targetType, UUID targetId) {
        if (ownerId == null) {
            throw new InvalidAllocationTargetException("Allocation owner id is required.");
        }
        if (targetType == null) {
            throw new InvalidAllocationTargetException("Allocation target type is required.");
        }
        if (targetId == null) {
            throw new InvalidAllocationTargetException("Allocation target id is required.");
        }
        if (targetType != AllocationTargetType.TASK) {
            throw new InvalidAllocationTargetException("Only TASK allocation targets are supported.");
        }

        validateTaskOwnership(ownerId, targetId);
    }

    private void validateTaskOwnership(UUID ownerId, UUID targetId) {
        String authorizationHeader = currentAuthorizationHeader();
        if (authorizationHeader == null) {
            throw new InvalidAllocationTargetException(
                    "Authenticated bearer token is required to validate allocation target ownership."
            );
        }

        try {
            TaskOwnershipResponse response = taskServiceClient
                    .get()
                    .uri("/api/tasks/{id}", targetId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .body(TaskOwnershipResponse.class);

            if (response == null || !ownerId.equals(response.userId())) {
                throw new InvalidAllocationTargetException("Allocation target task does not belong to the current user.");
            }
        } catch (HttpClientErrorException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND
                    || exception.getStatusCode() == HttpStatus.FORBIDDEN
                    || exception.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new InvalidAllocationTargetException("Allocation target task is not accessible to the current user.");
            }

            throw unavailable(exception);
        } catch (RestClientException exception) {
            throw unavailable(exception);
        }
    }

    private AllocationTargetUnavailableException unavailable(RestClientException exception) {
        return new AllocationTargetUnavailableException(
                "Allocation target ownership could not be validated because task data is unavailable.",
                exception
        );
    }

    private String currentAuthorizationHeader() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return null;
        }

        HttpServletRequest request = servletRequestAttributes.getRequest();
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null
                || !authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }

        return authorizationHeader;
    }

    private String normalizeBaseUrl(String value) {
        if (value == null || value.isBlank()) {
            return "http://task-service:8080";
        }

        String normalized = value.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }

    private record TaskOwnershipResponse(UUID userId) {
    }
}
