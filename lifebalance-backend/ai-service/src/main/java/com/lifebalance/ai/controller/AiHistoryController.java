package com.lifebalance.ai.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import com.lifebalance.ai.domain.AiHistoryActionType;
import com.lifebalance.ai.dto.AiHistoryResponse;
import com.lifebalance.ai.dto.PageResponse;
import com.lifebalance.ai.service.AiHistoryService;
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
@RequestMapping("/api/ai/history")
public class AiHistoryController {

    private final AiHistoryService historyService;

    public AiHistoryController(AiHistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AiHistoryResponse>>> search(
            @RequestParam(required = false) AiHistoryActionType actionType,
            @RequestParam(required = false) UUID conversationId,
            @RequestParam(required = false) UUID recommendationId,
            @RequestParam(required = false) UUID insightId,
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to,
            Pageable pageable,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(historyService.search(
                CurrentAiUser.ownerId(currentUser),
                actionType,
                conversationId,
                recommendationId,
                insightId,
                from,
                to,
                PageableLimits.normalize(pageable)
        ))));
    }
}
