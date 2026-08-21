package com.lifebalance.ai.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import com.lifebalance.ai.domain.AiConversationStatus;
import com.lifebalance.ai.domain.AiIntent;
import com.lifebalance.ai.dto.AiConversationResponse;
import com.lifebalance.ai.dto.AiMessageResponse;
import com.lifebalance.ai.dto.AiReplyResponse;
import com.lifebalance.ai.dto.AskAiRequest;
import com.lifebalance.ai.dto.PageResponse;
import com.lifebalance.ai.dto.ReasonRequest;
import com.lifebalance.ai.dto.StartConversationRequest;
import com.lifebalance.ai.service.AiConversationService;
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
@RequestMapping("/api/ai/conversations")
public class AiConversationController {

    private final AiConversationService conversationService;

    public AiConversationController(AiConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AiReplyResponse>> start(
            @Valid @RequestBody StartConversationRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.status(201).body(ApiResponse.success(conversationService.start(
                CurrentAiUser.ownerId(currentUser),
                request
        )));
    }

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<ApiResponse<AiReplyResponse>> ask(
            @PathVariable UUID conversationId,
            @Valid @RequestBody AskAiRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.status(201).body(ApiResponse.success(conversationService.ask(
                CurrentAiUser.ownerId(currentUser),
                conversationId,
                request
        )));
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<AiConversationResponse>> getById(
            @PathVariable UUID conversationId,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(conversationService.getById(
                CurrentAiUser.ownerId(currentUser),
                conversationId
        )));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AiConversationResponse>>> search(
            @RequestParam(required = false) AiConversationStatus status,
            @RequestParam(required = false) AiIntent intent,
            @RequestParam(required = false) String contextType,
            @RequestParam(required = false) UUID contextId,
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to,
            Pageable pageable,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(conversationService.search(
                CurrentAiUser.ownerId(currentUser),
                status,
                intent,
                contextType,
                contextId,
                from,
                to,
                PageableLimits.normalize(pageable)
        ))));
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<ApiResponse<PageResponse<AiMessageResponse>>> messages(
            @PathVariable UUID conversationId,
            Pageable pageable,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(conversationService.messages(
                CurrentAiUser.ownerId(currentUser),
                conversationId,
                PageableLimits.normalize(pageable)
        ))));
    }

    @PatchMapping("/{conversationId}/archive")
    public ResponseEntity<ApiResponse<AiConversationResponse>> archive(
            @PathVariable UUID conversationId,
            @Valid @RequestBody(required = false) ReasonRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(conversationService.archive(
                CurrentAiUser.ownerId(currentUser),
                conversationId,
                request
        )));
    }
}
