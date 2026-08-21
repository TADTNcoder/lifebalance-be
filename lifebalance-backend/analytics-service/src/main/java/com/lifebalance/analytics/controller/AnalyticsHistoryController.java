package com.lifebalance.analytics.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import com.lifebalance.analytics.domain.AnalyticsHistoryActionType;
import com.lifebalance.analytics.dto.AnalyticsHistoryResponse;
import com.lifebalance.analytics.dto.PageResponse;
import com.lifebalance.analytics.service.AnalyticsHistoryService;
import com.lifebalance.common.api.ApiResponse;
import com.lifebalance.common.web.PageableLimits;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics/history")
public class AnalyticsHistoryController {

    private final AnalyticsHistoryService historyService;

    public AnalyticsHistoryController(AnalyticsHistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AnalyticsHistoryResponse>>> search(
            @RequestParam(required = false) AnalyticsHistoryActionType actionType,
            @RequestParam(required = false) UUID actualRecordId,
            @RequestParam(required = false) UUID evaluationResultId,
            @RequestParam(required = false) UUID reportId,
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to,
            Pageable pageable,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(historyService.search(
                CurrentAnalyticsUser.ownerId(currentUser),
                actionType,
                actualRecordId,
                evaluationResultId,
                reportId,
                from,
                to,
                PageableLimits.normalize(pageable)
        ))));
    }
}
