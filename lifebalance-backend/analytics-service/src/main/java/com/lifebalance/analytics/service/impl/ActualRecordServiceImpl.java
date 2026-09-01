package com.lifebalance.analytics.service.impl;

import com.lifebalance.analytics.domain.ActualRecord;
import com.lifebalance.analytics.domain.ActualRecordStatus;
import com.lifebalance.analytics.domain.ActualRecordType;
import com.lifebalance.analytics.domain.AnalyticsHistoryActionType;
import com.lifebalance.analytics.dto.ActualRecordResponse;
import com.lifebalance.analytics.dto.ReasonRequest;
import com.lifebalance.analytics.dto.RecordActualRequest;
import com.lifebalance.analytics.dto.UpdateActualRecordRequest;
import com.lifebalance.analytics.error.AnalyticsExceptions;
import com.lifebalance.analytics.repository.ActualRecordRepository;
import com.lifebalance.analytics.service.ActualRecordService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ActualRecordServiceImpl implements ActualRecordService {

    private final ActualRecordRepository actualRecordRepository;
    private final AnalyticsHistoryRecorder historyRecorder;
    private final AnalyticsMapper mapper;
    private final AutomaticEvaluationService automaticEvaluationService;

    ActualRecordServiceImpl(
            ActualRecordRepository actualRecordRepository,
            AnalyticsHistoryRecorder historyRecorder,
            AnalyticsMapper mapper,
            AutomaticEvaluationService automaticEvaluationService
    ) {
        this.actualRecordRepository = actualRecordRepository;
        this.historyRecorder = historyRecorder;
        this.mapper = mapper;
        this.automaticEvaluationService = automaticEvaluationService;
    }

    @Override
    @Transactional
    public ActualRecordResponse record(UUID ownerId, RecordActualRequest request) {
        validateOwner(ownerId);
        ActualRecord actualRecord = ActualRecord.create(
                ownerId,
                ownerId,
                request.recordType(),
                request.taskId(),
                request.capitalCycleId(),
                request.categoryId(),
                mapper.toTagString(request.tagIds()),
                request.actualMinutes(),
                request.actualCost(),
                request.currencyCode(),
                request.actualDate(),
                request.note(),
                request.source()
        );

        ActualRecord saved = actualRecordRepository.save(actualRecord);
        historyRecorder.recordActual(
                ownerId,
                ownerId,
                AnalyticsHistoryActionType.ACTUAL_RECORDED,
                saved,
                null,
                mapper.actualRecordSnapshot(saved),
                request.note()
        );
        automaticEvaluationService.evaluateAfterActualChange(
                ownerId,
                new AutomaticEvaluationTarget(
                        saved.getTaskId(),
                        saved.getCapitalCycleId(),
                        saved.getActualDate(),
                        saved.getCurrencyCode(),
                        hasTime(saved.getRecordType()),
                        hasCost(saved.getRecordType()),
                        true
                ),
                request.evaluationBaseline(),
                request.note()
        );
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ActualRecordResponse update(UUID ownerId, UUID actualRecordId, UpdateActualRecordRequest request) {
        validateOwner(ownerId);
        ActualRecord actualRecord = actualRecordRepository.findByIdAndOwnerIdForUpdate(actualRecordId, ownerId)
                .orElseThrow(() -> AnalyticsExceptions.actualRecordNotFound(actualRecordId));
        String oldSnapshot = mapper.actualRecordSnapshot(actualRecord);
        AutomaticEvaluationTarget previousTarget = new AutomaticEvaluationTarget(
                actualRecord.getTaskId(),
                actualRecord.getCapitalCycleId(),
                actualRecord.getActualDate(),
                actualRecord.getCurrencyCode(),
                hasTime(actualRecord.getRecordType()),
                hasCost(actualRecord.getRecordType()),
                false
        );

        ActualRecordType targetType = request.recordType() == null ? actualRecord.getRecordType() : request.recordType();
        validateTypeSpecificUpdate(targetType, request);
        actualRecord.update(
                ownerId,
                targetType,
                request.taskId() == null ? actualRecord.getTaskId() : request.taskId(),
                request.capitalCycleId() == null ? actualRecord.getCapitalCycleId() : request.capitalCycleId(),
                request.categoryId() == null ? actualRecord.getCategoryId() : request.categoryId(),
                request.tagIds() == null ? actualRecord.getTagIds() : mapper.toTagString(request.tagIds()),
                resolveMinutes(actualRecord, targetType, request),
                resolveCost(actualRecord, targetType, request),
                resolveCurrency(actualRecord, targetType, request),
                request.actualDate() == null ? actualRecord.getActualDate() : request.actualDate(),
                request.note() == null ? actualRecord.getNote() : request.note(),
                request.source() == null ? actualRecord.getSource() : request.source()
        );

        historyRecorder.recordActual(
                ownerId,
                ownerId,
                AnalyticsHistoryActionType.ACTUAL_UPDATED,
                actualRecord,
                oldSnapshot,
                mapper.actualRecordSnapshot(actualRecord),
                request.reason()
        );
        AutomaticEvaluationTarget currentTarget = new AutomaticEvaluationTarget(
                actualRecord.getTaskId(),
                actualRecord.getCapitalCycleId(),
                actualRecord.getActualDate(),
                actualRecord.getCurrencyCode(),
                hasTime(actualRecord.getRecordType()),
                hasCost(actualRecord.getRecordType()),
                true
        );
        if (!sameEvaluationScope(previousTarget, currentTarget)) {
            automaticEvaluationService.evaluateAfterActualChange(
                    ownerId,
                    previousTarget,
                    null,
                    "Ghi nhận thực tế đã được chuyển sang task khác."
            );
        }
        automaticEvaluationService.evaluateAfterActualChange(
                ownerId,
                currentTarget,
                request.evaluationBaseline(),
                request.reason()
        );
        return mapper.toResponse(actualRecord);
    }

    @Override
    @Transactional
    public ActualRecordResponse archive(UUID ownerId, UUID actualRecordId, ReasonRequest request) {
        validateOwner(ownerId);
        ActualRecord actualRecord = actualRecordRepository.findByIdAndOwnerIdForUpdate(actualRecordId, ownerId)
                .orElseThrow(() -> AnalyticsExceptions.actualRecordNotFound(actualRecordId));
        String oldSnapshot = mapper.actualRecordSnapshot(actualRecord);
        actualRecord.archive(ownerId);

        historyRecorder.recordActual(
                ownerId,
                ownerId,
                AnalyticsHistoryActionType.ACTUAL_ARCHIVED,
                actualRecord,
                oldSnapshot,
                mapper.actualRecordSnapshot(actualRecord),
                request == null ? null : request.reason()
        );
        automaticEvaluationService.evaluateAfterActualChange(
                ownerId,
                new AutomaticEvaluationTarget(
                        actualRecord.getTaskId(),
                        actualRecord.getCapitalCycleId(),
                        actualRecord.getActualDate(),
                        actualRecord.getCurrencyCode(),
                        hasTime(actualRecord.getRecordType()),
                        hasCost(actualRecord.getRecordType()),
                        false
                ),
                null,
                request == null ? null : request.reason()
        );
        return mapper.toResponse(actualRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public ActualRecordResponse getById(UUID ownerId, UUID actualRecordId) {
        validateOwner(ownerId);
        return actualRecordRepository.findByIdAndOwnerId(actualRecordId, ownerId)
                .map(mapper::toResponse)
                .orElseThrow(() -> AnalyticsExceptions.actualRecordNotFound(actualRecordId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActualRecordResponse> search(
            UUID ownerId,
            UUID taskId,
            UUID capitalCycleId,
            UUID categoryId,
            ActualRecordType recordType,
            ActualRecordStatus status,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    ) {
        validateOwner(ownerId);
        validatePeriod(from, to);
        return actualRecordRepository.search(
                ownerId,
                taskId,
                capitalCycleId,
                categoryId,
                recordType,
                status,
                from,
                to,
                pageable
        ).map(mapper::toResponse);
    }

    private static void validateTypeSpecificUpdate(ActualRecordType targetType, UpdateActualRecordRequest request) {
        if (targetType == ActualRecordType.TIME
                && (request.actualCost() != null || ActualRecord.normalizeText(request.currencyCode(), 3) != null)) {
            throw AnalyticsExceptions.invalidRequest("TIME actual record must not include actualCost or currencyCode.");
        }
        if (targetType == ActualRecordType.MONEY && request.actualMinutes() != null) {
            throw AnalyticsExceptions.invalidRequest("MONEY actual record must not include actualMinutes.");
        }
    }

    private static Integer resolveMinutes(
            ActualRecord actualRecord,
            ActualRecordType targetType,
            UpdateActualRecordRequest request
    ) {
        if (targetType == ActualRecordType.MONEY) {
            return null;
        }
        if (request.actualMinutes() != null) {
            return request.actualMinutes();
        }
        if (actualRecord.getRecordType() != ActualRecordType.MONEY) {
            return actualRecord.getActualMinutes();
        }
        throw AnalyticsExceptions.invalidRequest("actualMinutes is required for TIME actual records.");
    }

    private static BigDecimal resolveCost(
            ActualRecord actualRecord,
            ActualRecordType targetType,
            UpdateActualRecordRequest request
    ) {
        if (targetType == ActualRecordType.TIME) {
            return null;
        }
        if (request.actualCost() != null) {
            return request.actualCost();
        }
        if (actualRecord.getRecordType() != ActualRecordType.TIME) {
            return actualRecord.getActualCost();
        }
        throw AnalyticsExceptions.invalidRequest("actualCost is required for MONEY actual records.");
    }

    private static String resolveCurrency(
            ActualRecord actualRecord,
            ActualRecordType targetType,
            UpdateActualRecordRequest request
    ) {
        if (targetType == ActualRecordType.TIME) {
            return null;
        }
        if (request.currencyCode() != null) {
            return request.currencyCode();
        }
        if (actualRecord.getRecordType() != ActualRecordType.TIME) {
            return actualRecord.getCurrencyCode();
        }
        throw AnalyticsExceptions.invalidRequest("currencyCode is required for MONEY actual records.");
    }

    static void validatePeriod(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw AnalyticsExceptions.invalidPeriod("from must be before or equal to to.");
        }
    }

    static void validateOwner(UUID ownerId) {
        if (ownerId == null) {
            throw AnalyticsExceptions.invalidRequest("ownerId is required.");
        }
    }

    private static boolean sameEvaluationScope(
            AutomaticEvaluationTarget left,
            AutomaticEvaluationTarget right
    ) {
        return java.util.Objects.equals(left.taskId(), right.taskId())
                && java.util.Objects.equals(left.capitalCycleId(), right.capitalCycleId())
                && java.util.Objects.equals(left.actualDate(), right.actualDate())
                && java.util.Objects.equals(left.currencyCode(), right.currencyCode())
                && left.hasTime() == right.hasTime()
                && left.hasCost() == right.hasCost();
    }

    private static boolean hasTime(ActualRecordType recordType) {
        return recordType == ActualRecordType.TIME || recordType == ActualRecordType.TIME_AND_MONEY;
    }

    private static boolean hasCost(ActualRecordType recordType) {
        return recordType == ActualRecordType.MONEY || recordType == ActualRecordType.TIME_AND_MONEY;
    }
}
