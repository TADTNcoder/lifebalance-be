package com.lifebalance.ai.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.lifebalance.ai.domain.AiHistoryActionType;
import com.lifebalance.ai.domain.AiInsight;
import com.lifebalance.ai.domain.AiInsightSeverity;
import com.lifebalance.ai.domain.AiInsightStatus;
import com.lifebalance.ai.domain.AiInsightType;
import com.lifebalance.ai.dto.AiInsightResponse;
import com.lifebalance.ai.dto.GenerateInsightRequest;
import com.lifebalance.ai.error.AiErrorCode;
import com.lifebalance.ai.repository.AiInsightRepository;
import com.lifebalance.common.error.AppException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AiInsightServiceImplTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID REFERENCE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID INSIGHT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Mock
    private AiInsightRepository insightRepository;

    @Mock
    private AiHistoryRecorder historyRecorder;

    @Test
    void generateCreatesInsightFromTimelineSignal() {
        when(insightRepository.save(any(AiInsight.class))).thenAnswer(invocation -> {
            AiInsight insight = invocation.getArgument(0);
            ReflectionTestUtils.setField(insight, "id", INSIGHT_ID);
            return insight;
        });

        AiInsightResponse response = service().generate(
                OWNER_ID,
                new GenerateInsightRequest(
                        null,
                        LocalDate.parse("2026-08-01"),
                        LocalDate.parse("2026-08-31"),
                        "TIMELINE",
                        REFERENCE_ID,
                        "timeline conflict risk across focus blocks"
                )
        );

        assertThat(response.id()).isEqualTo(INSIGHT_ID);
        assertThat(response.insightType()).isEqualTo(AiInsightType.TIMELINE_CONFLICT);
        assertThat(response.severity()).isEqualTo(AiInsightSeverity.WARNING);
        assertThat(response.status()).isEqualTo(AiInsightStatus.ACTIVE);
        assertThat(response.confidenceScore()).isEqualByComparingTo(new BigDecimal("0.7800"));

        verify(historyRecorder).recordInsight(
                eq(OWNER_ID),
                eq(OWNER_ID),
                eq(AiHistoryActionType.INSIGHT_GENERATED),
                any(AiInsight.class),
                isNull(),
                contains("type=TIMELINE_CONFLICT"),
                eq("Generated from AI insight request")
        );
    }

    @Test
    void generateRejectsInvalidPeriodWithoutChangingRecords() {
        assertThatThrownBy(() -> service().generate(
                OWNER_ID,
                new GenerateInsightRequest(
                        AiInsightType.GENERAL,
                        LocalDate.parse("2026-08-31"),
                        LocalDate.parse("2026-08-01"),
                        null,
                        null,
                        "summary"
                )
        ))
                .isInstanceOf(AppException.class)
                .extracting("code")
                .isEqualTo(AiErrorCode.INVALID_PERIOD);

        verify(insightRepository, never()).save(any());
        verifyNoInteractions(historyRecorder);
    }

    private AiInsightServiceImpl service() {
        return new AiInsightServiceImpl(
                insightRepository,
                new AiSuggestionEngine(),
                historyRecorder,
                new AiMapper()
        );
    }
}
