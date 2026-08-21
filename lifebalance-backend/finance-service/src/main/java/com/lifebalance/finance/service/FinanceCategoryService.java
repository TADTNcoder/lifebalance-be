package com.lifebalance.finance.service;

import com.lifebalance.finance.domain.FinanceCategoryStatus;
import com.lifebalance.finance.domain.FinanceCategoryType;
import com.lifebalance.finance.dto.CreateFinanceCategoryRequest;
import com.lifebalance.finance.dto.FinanceCategoryResponse;
import com.lifebalance.finance.dto.UpdateFinanceCategoryRequest;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FinanceCategoryService {

    FinanceCategoryResponse create(UUID ownerId, CreateFinanceCategoryRequest request);

    FinanceCategoryResponse update(UUID ownerId, UUID categoryId, UpdateFinanceCategoryRequest request);

    FinanceCategoryResponse archive(UUID ownerId, UUID categoryId, String reason);

    FinanceCategoryResponse getById(UUID ownerId, UUID categoryId);

    Page<FinanceCategoryResponse> getCategories(
            UUID ownerId,
            FinanceCategoryType type,
            FinanceCategoryStatus status,
            Pageable pageable);
}
