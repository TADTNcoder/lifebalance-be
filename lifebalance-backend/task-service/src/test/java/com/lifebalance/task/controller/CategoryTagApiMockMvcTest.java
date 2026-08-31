package com.lifebalance.task.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lifebalance.common.error.AuthErrorCode;
import com.lifebalance.common.error.GlobalExceptionHandler;
import com.lifebalance.security.keycloak.KeycloakUserMappingFilter;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import com.lifebalance.task.dto.request.CreateCategoryRequest;
import com.lifebalance.task.dto.request.CreateTagRequest;
import com.lifebalance.task.dto.response.CategoryResponse;
import com.lifebalance.task.dto.response.TagResponse;
import com.lifebalance.task.error.TaskErrorCode;
import com.lifebalance.task.error.TaskExceptions;
import com.lifebalance.task.service.CategoryService;
import com.lifebalance.task.service.TagService;
import com.lifebalance.task.service.TaskTagService;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * MockMvc scenarios for Category + Tag APIs.
 *
 * <p>Thay cho việc phải bắn Postman cho các case validation/duplicate/assign/remove.
 * UI vẫn nên demo riêng để chứng minh hiển thị trực quan.
 */
class CategoryTagApiMockMvcTest {

    private static final UUID OWNER_ID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TASK_ID =
            UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID TAG_ID =
            UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID CATEGORY_ID =
            UUID.fromString("44444444-4444-4444-4444-444444444444");

    private CategoryService categoryService;
    private TagService tagService;
    private TaskTagService taskTagService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        categoryService = org.mockito.Mockito.mock(CategoryService.class);
        tagService = org.mockito.Mockito.mock(TagService.class);
        taskTagService = org.mockito.Mockito.mock(TaskTagService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new CategoryController(categoryService),
                        new TagController(tagService),
                        new TaskTagController(taskTagService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("TC_CAT_01 - Manager tạo Category hợp lệ thành công")
    void categoryCreate_validManager_returnsOk() throws Exception {
        CategoryResponse response = new CategoryResponse();
        response.setId(CATEGORY_ID);
        response.setName("Work");
        response.setColor("#22C55E");

        when(categoryService.create(eq(OWNER_ID), any(CreateCategoryRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/categories")
                        .requestAttr(
                                KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE,
                                managerUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Work",
                                  "slug": "work",
                                  "description": "Công việc",
                                  "color": "#22C55E",
                                  "icon": "briefcase"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(CATEGORY_ID.toString()))
                .andExpect(jsonPath("$.name").value("Work"))
                .andExpect(jsonPath("$.color").value("#22C55E"));

        verify(categoryService).create(eq(OWNER_ID), any(CreateCategoryRequest.class));
    }

    @Test
    @DisplayName("TC_CAT_02 - Category trùng tên trả về 409 Conflict")
    void categoryCreate_duplicateName_returnsConflict() throws Exception {
        when(categoryService.create(eq(OWNER_ID), any(CreateCategoryRequest.class)))
                .thenThrow(TaskExceptions.categoryNameAlreadyExists());

        mockMvc.perform(post("/api/categories")
                        .requestAttr(
                                KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE,
                                managerUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Work",
                                  "color": "#22C55E"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code")
                        .value(TaskErrorCode.CATEGORY_NAME_ALREADY_EXISTS));
    }

    @Test
    @DisplayName("Category - User thường được tạo Category thuộc sở hữu của chính mình")
    void categoryCreate_normalUser_returnsOk() throws Exception {
        CategoryResponse response = new CategoryResponse();
        response.setId(CATEGORY_ID);
        response.setName("Personal");
        response.setColor("#3B82F6");

        when(categoryService.create(
                eq(OWNER_ID),
                any(CreateCategoryRequest.class)
        )).thenReturn(response);

        mockMvc.perform(post("/api/categories")
                        .requestAttr(
                                KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE,
                                normalUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Personal",
                                  "color": "#3B82F6"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(CATEGORY_ID.toString()))
                .andExpect(jsonPath("$.name").value("Personal"))
                .andExpect(jsonPath("$.color").value("#3B82F6"));

        verify(categoryService).create(
                eq(OWNER_ID),
                any(CreateCategoryRequest.class)
        );
    }

    @Test
    @DisplayName("Tag - User tạo Tag hợp lệ thành công")
    void tagCreate_validUser_returnsOk() throws Exception {
        TagResponse response = new TagResponse();
        response.setId(TAG_ID);
        response.setName("important");
        response.setDescription("Việc quan trọng");

        when(tagService.create(eq(OWNER_ID), any(CreateTagRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/tags")
                        .requestAttr(
                                KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE,
                                normalUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "important",
                                  "description": "Việc quan trọng"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TAG_ID.toString()))
                .andExpect(jsonPath("$.name").value("important"));

        verify(tagService).create(eq(OWNER_ID), any(CreateTagRequest.class));
    }

    @Test
    @DisplayName("TC_TAG_01 - Gắn Tag vào Task thành công bằng MockMvc")
    void taskTag_assign_returnsOk() throws Exception {
        mockMvc.perform(put("/api/tasks/{taskId}/tags/{tagId}", TASK_ID, TAG_ID)
                        .requestAttr(
                                KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE,
                                normalUser()))
                .andExpect(status().isOk());

        verify(taskTagService).assignTag(OWNER_ID, TASK_ID, TAG_ID);
    }

    @Test
    @DisplayName("TC_TAG_02 - Gỡ Tag khỏi Task thành công bằng MockMvc")
    void taskTag_remove_returnsOk() throws Exception {
        mockMvc.perform(delete("/api/tasks/{taskId}/tags/{tagId}", TASK_ID, TAG_ID)
                        .requestAttr(
                                KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE,
                                normalUser()))
                .andExpect(status().isOk());

        verify(taskTagService).removeTag(OWNER_ID, TASK_ID, TAG_ID);
    }

    @Test
    @DisplayName("Task Tag - Thiếu user xác thực trả về 401")
    void taskTag_assignWithoutUser_returnsUnauthorized() throws Exception {
        mockMvc.perform(put("/api/tasks/{taskId}/tags/{tagId}", TASK_ID, TAG_ID))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.UNAUTHORIZED));

        verify(taskTagService, never()).assignTag(any(), any(), any());
    }

    private static KeycloakUserPrincipal managerUser() {
        return principal(Set.of("manager"));
    }

    private static KeycloakUserPrincipal normalUser() {
        return principal(Set.of("user"));
    }

    private static KeycloakUserPrincipal principal(Set<String> roles) {
        return new KeycloakUserPrincipal(
                "kc-user-1",
                OWNER_ID,
                "lifebalance-user",
                "user@lifebalance.local",
                "LifeBalance User",
                "LifeBalance",
                "User",
                "lifebalance-web",
                Set.of("lifebalance-web"),
                roles,
                roles,
                roles
        );
    }
}
