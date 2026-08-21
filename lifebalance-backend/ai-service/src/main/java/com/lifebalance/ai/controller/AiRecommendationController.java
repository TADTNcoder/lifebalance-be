package com.lifebalance.ai.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import com.lifebalance.ai.domain.AiPriority;
import com.lifebalance.ai.domain.AiRecommendationStatus;
import com.lifebalance.ai.domain.AiRecommendationType;
import com.lifebalance.ai.dto.AiRecommendationResponse;
import com.lifebalance.ai.dto.GenerateRecommendationRequest;
import com.lifebalance.ai.dto.PageResponse;
import com.lifebalance.ai.dto.RecommendationDecisionRequest;
import com.lifebalance.ai.service.AiRecommendationService;
import com.lifebalance.common.api.ApiResponse;
import com.lifebalance.common.web.PageableLimits;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
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
@RequestMapping("/api/ai/recommendations")
public class AiRecommendationController {

    private final AiRecommendationService recommendationService;

    public AiRecommendationController(AiRecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<AiRecommendationResponse>> generate(
            @Valid @RequestBody GenerateRecommendationRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.status(201).body(ApiResponse.success(recommendationService.generate(
                CurrentAiUser.ownerId(currentUser),
                request
        )));
    }

    @GetMapping("/{recommendationId}")
    public ResponseEntity<ApiResponse<AiRecommendationResponse>> getById(
            @PathVariable UUID recommendationId,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(recommendationService.getById(
                CurrentAiUser.ownerId(currentUser),
                recommendationId
        )));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AiRecommendationResponse>>> search(
            @RequestParam(required = false) AiRecommendationType recommendationType,
            @RequestParam(required = false) AiRecommendationStatus status,
            @RequestParam(required = false) AiPriority priority,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) UUID targetId,
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to,
            Pageable pageable,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(recommendationService.search(
                CurrentAiUser.ownerId(currentUser),
                recommendationType,
                status,
                priority,
                targetType,
                targetId,
                from,
                to,
                PageableLimits.normalize(pageable)
        ))));
    }

    @PatchMapping("/{recommendationId}/apply")
    public ResponseEntity<ApiResponse<AiRecommendationResponse>> apply(
            @PathVariable UUID recommendationId,
            @Valid @RequestBody(required = false) RecommendationDecisionRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(recommendationService.apply(
                CurrentAiUser.ownerId(currentUser),
                recommendationId,
                request
        )));
    }

    @PatchMapping("/{recommendationId}/dismiss")
    public ResponseEntity<ApiResponse<AiRecommendationResponse>> dismiss(
            @PathVariable UUID recommendationId,
            @Valid @RequestBody(required = false) RecommendationDecisionRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(recommendationService.dismiss(
                CurrentAiUser.ownerId(currentUser),
                recommendationId,
                request
        )));
    }
}
