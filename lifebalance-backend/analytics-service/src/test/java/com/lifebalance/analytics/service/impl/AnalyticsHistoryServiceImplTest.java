package com.lifebalance.analytics.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lifebalance.analytics.repository.AnalyticsHistoryRepository;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AnalyticsHistoryServiceImplTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private AnalyticsHistoryRepository historyRepository;

    @Test
    void searchReplacesMissingTimestampBoundsBeforeQueryingPostgres() {
        Pageable pageable = PageRequest.of(0, 20);
        when(historyRepository.search(
                eq(OWNER_ID),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                any(OffsetDateTime.class),
                any(OffsetDateTime.class),
                eq(pageable)
        )).thenReturn(Page.empty(pageable));

        new AnalyticsHistoryServiceImpl(historyRepository, new AnalyticsMapper())
                .search(OWNER_ID, null, null, null, null, null, null, pageable);

        ArgumentCaptor<OffsetDateTime> fromCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);
        ArgumentCaptor<OffsetDateTime> toCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);
        verify(historyRepository).search(
                eq(OWNER_ID),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                fromCaptor.capture(),
                toCaptor.capture(),
                eq(pageable)
        );
        assertThat(fromCaptor.getValue()).isEqualTo(OffsetDateTime.parse("0001-01-01T00:00:00Z"));
        assertThat(toCaptor.getValue()).isEqualTo(OffsetDateTime.parse("9999-12-31T23:59:59.999999999Z"));
    }
}
