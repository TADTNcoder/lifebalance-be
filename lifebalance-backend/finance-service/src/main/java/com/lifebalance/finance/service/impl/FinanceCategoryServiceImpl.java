package com.lifebalance.finance.service.impl;

import com.lifebalance.finance.domain.FinanceCategory;
import com.lifebalance.finance.domain.FinanceCategoryStatus;
import com.lifebalance.finance.domain.FinanceCategoryType;
import com.lifebalance.finance.domain.FinanceHistoryActionType;
import com.lifebalance.finance.domain.FinanceReferenceType;
import com.lifebalance.finance.dto.CreateFinanceCategoryRequest;
import com.lifebalance.finance.dto.FinanceCategoryResponse;
import com.lifebalance.finance.dto.UpdateFinanceCategoryRequest;
import com.lifebalance.finance.error.FinanceExceptions;
import com.lifebalance.finance.repository.FinanceCategoryRepository;
import com.lifebalance.finance.service.FinanceCategoryService;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinanceCategoryServiceImpl implements FinanceCategoryService {

    private final FinanceCategoryRepository financeCategoryRepository;
    private final FinanceHistoryRecorder historyRecorder;

    public FinanceCategoryServiceImpl(
            FinanceCategoryRepository financeCategoryRepository,
            FinanceHistoryRecorder historyRecorder
    ) {
        this.financeCategoryRepository = financeCategoryRepository;
        this.historyRecorder = historyRecorder;
    }

    @Override
    @Transactional
    public FinanceCategoryResponse create(UUID ownerId, CreateFinanceCategoryRequest request) {
        String name = request.name().trim();
        if (financeCategoryRepository.existsByOwnerIdAndCategoryTypeAndNameIgnoreCaseAndStatus(
                ownerId,
                request.categoryType(),
                name,
                FinanceCategoryStatus.ACTIVE
        )) {
            throw FinanceExceptions.categoryAlreadyExists(name);
        }

        FinanceCategory category = FinanceCategory.create(
                ownerId,
                ownerId,
                name,
                request.categoryType(),
                request.color(),
                request.icon()
        );
        category = financeCategoryRepository.save(category);

        historyRecorder.record(
                ownerId,
                ownerId,
                FinanceHistoryActionType.CATEGORY_CREATED,
                FinanceReferenceType.FINANCE_CATEGORY,
                category.getId(),
                null,
                null,
                snapshot(category)
        );

        return FinanceMapper.toCategoryResponse(category);
    }

    @Override
    @Transactional
    public FinanceCategoryResponse update(UUID ownerId, UUID categoryId, UpdateFinanceCategoryRequest request) {
        FinanceCategory category = getOwnedCategory(ownerId, categoryId);
        String name = request.name().trim();

        if (!category.getName().equalsIgnoreCase(name)
                && financeCategoryRepository.existsByOwnerIdAndCategoryTypeAndNameIgnoreCaseAndStatus(
                ownerId,
                category.getCategoryType(),
                name,
                FinanceCategoryStatus.ACTIVE
        )) {
            throw FinanceExceptions.categoryAlreadyExists(name);
        }

        String oldValue = snapshot(category);
        category.updateDetails(ownerId, name, request.color(), request.icon());
        category = financeCategoryRepository.save(category);

        historyRecorder.record(
                ownerId,
                ownerId,
                FinanceHistoryActionType.CATEGORY_UPDATED,
                FinanceReferenceType.FINANCE_CATEGORY,
                category.getId(),
                request.reason(),
                oldValue,
                snapshot(category)
        );

        return FinanceMapper.toCategoryResponse(category);
    }

    @Override
    @Transactional
    public FinanceCategoryResponse archive(UUID ownerId, UUID categoryId, String reason) {
        FinanceCategory category = getOwnedCategory(ownerId, categoryId);
        String oldValue = snapshot(category);
        category.archive(ownerId);
        category = financeCategoryRepository.save(category);

        historyRecorder.record(
                ownerId,
                ownerId,
                FinanceHistoryActionType.CATEGORY_ARCHIVED,
                FinanceReferenceType.FINANCE_CATEGORY,
                category.getId(),
                reason,
                oldValue,
                snapshot(category)
        );

        return FinanceMapper.toCategoryResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public FinanceCategoryResponse getById(UUID ownerId, UUID categoryId) {
        return FinanceMapper.toCategoryResponse(getOwnedCategory(ownerId, categoryId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FinanceCategoryResponse> getCategories(
            UUID ownerId,
            FinanceCategoryType type,
            FinanceCategoryStatus status,
            Pageable pageable
    ) {
        return financeCategoryRepository.search(ownerId, type, status, pageable)
                .map(FinanceMapper::toCategoryResponse);
    }

    private FinanceCategory getOwnedCategory(UUID ownerId, UUID categoryId) {
        return financeCategoryRepository.findByIdAndOwnerId(categoryId, ownerId)
                .orElseThrow(() -> FinanceExceptions.categoryNotFound(categoryId));
    }

    private static String snapshot(FinanceCategory category) {
        return "name=" + category.getName()
                + ";type=" + category.getCategoryType()
                + ";status=" + category.getStatus()
                + ";color=" + category.getColor()
                + ";icon=" + category.getIcon();
    }
}
