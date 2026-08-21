package com.lifebalance.ai.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lifebalance.ai.domain.AiHistoryActionType;
import com.lifebalance.ai.domain.AiIntent;
import com.lifebalance.ai.domain.AiPriority;
import com.lifebalance.ai.domain.AiRecommendation;
import com.lifebalance.ai.domain.AiRecommendationStatus;
import com.lifebalance.ai.domain.AiRecommendationType;
import com.lifebalance.ai.dto.AiRecommendationResponse;
import com.lifebalance.ai.dto.GenerateRecommendationRequest;
import com.lifebalance.ai.dto.RecommendationDecisionRequest;
import com.lifebalance.ai.repository.AiRecommendationRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AiRecommendationServiceImplTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SOURCE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID TARGET_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID RECOMMENDATION_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    @Mock
    private AiRecommendationRepository recommendationRepository;

    @Mock
    private AiHistoryRecorder historyRecorder;

    @Test
    void generateCreatesPendingRecommendationFromFinanceSignal() {
        when(recommendationRepository.save(any(AiRecommendation.class))).thenAnswer(invocation -> {
            AiRecommendation recommendation = invocation.getArgument(0);
            ReflectionTestUtils.setField(recommendation, "id", RECOMMENDATION_ID);
            return recommendation;
        });

        AiRecommendationResponse response = service().generate(
                OWNER_ID,
                new GenerateRecommendationRequest(
                        AiIntent.FINANCE_REVIEW,
                        "REPORT",
                        SOURCE_ID,
                        "BUDGET",
                        TARGET_ID,
                        "critical budget risk in recurring spend"
                )
        );

        assertThat(response.id()).isEqualTo(RECOMMENDATION_ID);
        assertThat(response.recommendationType()).isEqualTo(AiRecommendationType.BUDGET_OPTIMIZATION);
        assertThat(response.status()).isEqualTo(AiRecommendationStatus.PENDING);
        assertThat(response.priority()).isEqualTo(AiPriority.HIGH);
        assertThat(response.confidenceScore()).isEqualByComparingTo(new BigDecimal("0.8600"));

        verify(historyRecorder).recordRecommendation(
                eq(OWNER_ID),
                eq(OWNER_ID),
                eq(AiHistoryActionType.RECOMMENDATION_GENERATED),
                any(AiRecommendation.class),
                isNull(),
                contains("type=BUDGET_OPTIMIZATION"),
                eq("Generated from AI recommendation request")
        );
    }

    @Test
    void applyMovesPendingRecommendationToAppliedAndRecordsDecision() {
        AiRecommendation recommendation = pendingRecommendation();
        when(recommendationRepository.findByIdAndOwnerIdForUpdate(RECOMMENDATION_ID, OWNER_ID))
                .thenReturn(Optional.of(recommendation));

        AiRecommendationResponse response = service().apply(
                OWNER_ID,
                RECOMMENDATION_ID,
                new RecommendationDecisionRequest("Accepted by user")
        );

        assertThat(response.status()).isEqualTo(AiRecommendationStatus.APPLIED);
        assertThat(response.decisionReason()).isEqualTo("Accepted by user");
        verify(historyRecorder).recordRecommendation(
                eq(OWNER_ID),
                eq(OWNER_ID),
                eq(AiHistoryActionType.RECOMMENDATION_APPLIED),
                eq(recommendation),
                contains("status=PENDING"),
                contains("status=APPLIED"),
                eq("Accepted by user")
        );
    }

    private AiRecommendationServiceImpl service() {
        return new AiRecommendationServiceImpl(
                recommendationRepository,
                new AiSuggestionEngine(),
                historyRecorder,
                new AiMapper()
        );
    }

    private static AiRecommendation pendingRecommendation() {
        AiRecommendation recommendation = AiRecommendation.generate(
                OWNER_ID,
                OWNER_ID,
                AiRecommendationType.BUDGET_OPTIMIZATION,
                AiPriority.HIGH,
                "Review budget pressure",
                "Review the budget signal before applying any operational change.",
                "REPORT",
                SOURCE_ID,
                "BUDGET",
                TARGET_ID,
                new BigDecimal("0.8600"),
                "critical budget risk"
        );
        ReflectionTestUtils.setField(recommendation, "id", RECOMMENDATION_ID);
        return recommendation;
    }
}
