package com.lifebalance.analytics.service;

import com.lifebalance.analytics.domain.ActualRecordStatus;
import com.lifebalance.analytics.domain.ActualRecordType;
import com.lifebalance.analytics.dto.ActualRecordResponse;
import com.lifebalance.analytics.dto.ReasonRequest;
import com.lifebalance.analytics.dto.RecordActualRequest;
import com.lifebalance.analytics.dto.UpdateActualRecordRequest;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ActualRecordService {

    ActualRecordResponse record(UUID ownerId, RecordActualRequest request);

    ActualRecordResponse update(UUID ownerId, UUID actualRecordId, UpdateActualRecordRequest request);

    ActualRecordResponse archive(UUID ownerId, UUID actualRecordId, ReasonRequest request);

    ActualRecordResponse getById(UUID ownerId, UUID actualRecordId);

    Page<ActualRecordResponse> search(
            UUID ownerId,
            UUID taskId,
            UUID capitalCycleId,
            UUID categoryId,
            ActualRecordType recordType,
            ActualRecordStatus status,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    );
}
