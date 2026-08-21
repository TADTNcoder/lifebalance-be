package com.lifebalance.analytics.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import com.lifebalance.analytics.domain.ActualRecordStatus;
import com.lifebalance.analytics.domain.ActualRecordType;
import com.lifebalance.analytics.dto.ActualRecordResponse;
import com.lifebalance.analytics.dto.PageResponse;
import com.lifebalance.analytics.dto.ReasonRequest;
import com.lifebalance.analytics.dto.RecordActualRequest;
import com.lifebalance.analytics.dto.UpdateActualRecordRequest;
import com.lifebalance.analytics.service.ActualRecordService;
import com.lifebalance.common.api.ApiResponse;
import com.lifebalance.common.web.PageableLimits;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics/actual-records")
public class ActualRecordController {

    private final ActualRecordService actualRecordService;

    public ActualRecordController(ActualRecordService actualRecordService) {
        this.actualRecordService = actualRecordService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ActualRecordResponse>> record(
            @Valid @RequestBody RecordActualRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.status(201).body(ApiResponse.success(actualRecordService.record(
                CurrentAnalyticsUser.ownerId(currentUser),
                request
        )));
    }

    @GetMapping("/{actualRecordId}")
    public ResponseEntity<ApiResponse<ActualRecordResponse>> getById(
            @PathVariable UUID actualRecordId,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(actualRecordService.getById(
                CurrentAnalyticsUser.ownerId(currentUser),
                actualRecordId
        )));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ActualRecordResponse>>> search(
            @RequestParam(required = false) UUID taskId,
            @RequestParam(required = false) UUID capitalCycleId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) ActualRecordType recordType,
            @RequestParam(required = false) ActualRecordStatus status,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            Pageable pageable,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(actualRecordService.search(
                CurrentAnalyticsUser.ownerId(currentUser),
                taskId,
                capitalCycleId,
                categoryId,
                recordType,
                status,
                from,
                to,
                PageableLimits.normalize(pageable)
        ))));
    }

    @PatchMapping("/{actualRecordId}")
    public ResponseEntity<ApiResponse<ActualRecordResponse>> update(
            @PathVariable UUID actualRecordId,
            @Valid @RequestBody UpdateActualRecordRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(actualRecordService.update(
                CurrentAnalyticsUser.ownerId(currentUser),
                actualRecordId,
                request
        )));
    }

    @PatchMapping("/{actualRecordId}/archive")
    public ResponseEntity<ApiResponse<ActualRecordResponse>> archive(
            @PathVariable UUID actualRecordId,
            @Valid @RequestBody(required = false) ReasonRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(actualRecordService.archive(
                CurrentAnalyticsUser.ownerId(currentUser),
                actualRecordId,
                request
        )));
    }
}
