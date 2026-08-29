package com.lifebalance.task.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lifebalance.common.error.AppException;
import com.lifebalance.task.dto.request.CreateCategoryRequest;
import com.lifebalance.task.dto.request.UpdateCategoryRequest;
import com.lifebalance.task.dto.response.CategoryResponse;
import com.lifebalance.task.model.Category;
import com.lifebalance.task.repository.CategoryRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    private CategoryServiceImpl categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryServiceImpl(categoryRepository);
    }

    @Test
    void regularUserCanCreateCategoryOwnedByThem() {
        UUID ownerId = UUID.randomUUID();
        CreateCategoryRequest request = createRequest("  Dự án cá nhân  ", "du-an-ca-nhan");
        when(categoryRepository.existsVisibleName(ownerId, "Dự án cá nhân")).thenReturn(false);
        when(categoryRepository.existsVisibleSlug(ownerId, "du-an-ca-nhan")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(UUID.randomUUID());
            return category;
        });

        CategoryResponse response = categoryService.create(ownerId, request);

        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(categoryCaptor.capture());
        assertThat(categoryCaptor.getValue().getOwnerId()).isEqualTo(ownerId);
        assertThat(categoryCaptor.getValue().getName()).isEqualTo("Dự án cá nhân");
        assertThat(response.getOwnerId()).isEqualTo(ownerId);
        assertThat(response.getCanModify()).isTrue();
    }

    @Test
    void listReturnsOnlyRepositoryVisibleCategoriesAndMarksOwnership() {
        UUID ownerId = UUID.randomUUID();
        Category systemCategory = category(UUID.randomUUID(), null, "Work", true);
        Category ownedCategory = category(UUID.randomUUID(), ownerId, "Cá nhân", false);
        Category legacySharedCategory = category(UUID.randomUUID(), null, "Danh mục cũ", false);
        when(categoryRepository.findVisibleByOwnerId(ownerId))
                .thenReturn(List.of(systemCategory, ownedCategory, legacySharedCategory));

        List<CategoryResponse> response = categoryService.getAll(ownerId);

        assertThat(response).extracting(CategoryResponse::getName)
                .containsExactly("Work", "Cá nhân", "Danh mục cũ");
        assertThat(response).extracting(CategoryResponse::getCanModify)
                .containsExactly(false, true, false);
    }

    @Test
    void ownerCanUpdateTheirCategory() {
        UUID ownerId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        Category category = category(categoryId, ownerId, "Cũ", false);
        category.setSlug("cu");
        UpdateCategoryRequest request = updateRequest("Mới", "moi");
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.existsVisibleNameExcludingId(ownerId, "Mới", categoryId))
                .thenReturn(false);
        when(categoryRepository.existsVisibleSlugExcludingId(ownerId, "moi", categoryId))
                .thenReturn(false);
        when(categoryRepository.save(category)).thenReturn(category);

        CategoryResponse response = categoryService.update(ownerId, categoryId, request);

        assertThat(response.getName()).isEqualTo("Mới");
        assertThat(response.getCanModify()).isTrue();
        verify(categoryRepository).save(category);
    }

    @Test
    void anotherUserCannotUpdateOrDiscoverOwnedCategory() {
        UUID ownerId = UUID.randomUUID();
        UUID anotherUserId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        Category category = category(categoryId, ownerId, "Riêng tư", false);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> categoryService.update(
                anotherUserId,
                categoryId,
                updateRequest("Đổi tên", "doi-ten")))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Category not found");

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void systemAndLegacySharedCategoriesRemainReadOnly() {
        UUID ownerId = UUID.randomUUID();
        UUID systemId = UUID.randomUUID();
        UUID legacyId = UUID.randomUUID();
        when(categoryRepository.findById(systemId))
                .thenReturn(Optional.of(category(systemId, null, "Work", true)));
        when(categoryRepository.findById(legacyId))
                .thenReturn(Optional.of(category(legacyId, null, "Legacy", false)));

        assertThatThrownBy(() -> categoryService.delete(ownerId, systemId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("System categories");
        assertThatThrownBy(() -> categoryService.delete(ownerId, legacyId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("current user");

        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void ownerCanDeleteTheirCategory() {
        UUID ownerId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        Category category = category(categoryId, ownerId, "Có thể xóa", false);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        categoryService.delete(ownerId, categoryId);

        verify(categoryRepository).delete(category);
    }

    private static CreateCategoryRequest createRequest(String name, String slug) {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName(name);
        request.setSlug(slug);
        request.setColor("#3b82f6");
        return request;
    }

    private static UpdateCategoryRequest updateRequest(String name, String slug) {
        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setName(name);
        request.setSlug(slug);
        request.setColor("#3b82f6");
        return request;
    }

    private static Category category(UUID id, UUID ownerId, String name, boolean system) {
        return Category.builder()
                .id(id)
                .ownerId(ownerId)
                .name(name)
                .slug(name.toLowerCase())
                .isSystem(system)
                .build();
    }
}
