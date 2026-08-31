package com.lifebalance.task.service;

import com.lifebalance.common.error.AppException;
import com.lifebalance.task.dto.request.CreateTaskRequest;
import com.lifebalance.task.dto.request.TaskLifecycleActionRequest;
import com.lifebalance.task.dto.request.TaskPlanningRequest;
import com.lifebalance.task.dto.request.UpdateTaskRequest;
import com.lifebalance.task.dto.request.UpdateTaskProgressRequest;
import com.lifebalance.task.dto.response.TaskResponse;
import com.lifebalance.task.error.TaskErrorCode;
import com.lifebalance.task.error.TaskExceptions;
import com.lifebalance.task.history.TaskChangeHistoryService;
import com.lifebalance.task.integration.TaskIntegrationPublisher;
import com.lifebalance.task.model.Category;
import com.lifebalance.task.model.Tag;
import com.lifebalance.task.model.Task;
import com.lifebalance.task.model.TimelinePlacement;
import com.lifebalance.task.model.enums.PriorityLevel;
import com.lifebalance.task.model.enums.TaskHistoryActionType;
import com.lifebalance.task.model.enums.TaskStatus;
import com.lifebalance.task.model.enums.TimelinePlacementStatus;
import com.lifebalance.task.repository.CategoryRepository;
import com.lifebalance.task.repository.TaskRepository;
import com.lifebalance.task.repository.TimelinePlacementRepository;
import com.lifebalance.task.service.impl.TaskServiceImpl;
import com.lifebalance.task.validation.TaskLifecyclePolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TimelinePlacementRepository timelinePlacementRepository;

    @Mock
    private TaskLifecyclePolicy taskLifecyclePolicy;

    @Mock
    private TaskChangeHistoryService taskChangeHistoryService;

    @Mock
    private TaskIntegrationPublisher taskIntegrationPublisher;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task mockTask;
    private final UUID TASK_ID = UUID.randomUUID();
    private final UUID USER_ID = UUID.randomUUID();
    private final UUID FINANCE_ACCOUNT_ID = UUID.randomUUID();
    private final UUID UPDATED_FINANCE_ACCOUNT_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockTask = Task.builder()
                .ownerId(USER_ID)
                .userId(USER_ID)
                .name("Học Spring Boot")
                .description("Học để làm đồ án")
                .note("Ôn lại dependency injection")
                .currency("USD")
                .estimatedMinutes(120)
                .estimatedCost(new BigDecimal("500000"))
                .financeAccountId(FINANCE_ACCOUNT_ID)
                .status(TaskStatus.DRAFT)
                .build();
        try {
            java.lang.reflect.Field idField = mockTask.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(mockTask, TASK_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void create_Success() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setName("Học Spring Boot");
        request.setDescription("Học để làm đồ án");
        request.setNote("Ôn lại dependency injection");
        request.setCurrency("USD");
        request.setPriority(PriorityLevel.HIGH);
        request.setDeadline(LocalDate.now().plusDays(3));
        request.setEstimatedMinutes(120);
        request.setEstimatedCost(new BigDecimal("500000"));
        request.setFinanceAccountId(FINANCE_ACCOUNT_ID);

        when(taskRepository.findAllByNameAndOwnerId(request.getName(), USER_ID))
                .thenReturn(List.of());
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> {
            Task saved = i.getArgument(0);
            try {
                java.lang.reflect.Field idField = saved.getClass().getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(saved, TASK_ID);
            } catch (Exception ignored) {}
            return saved;
        });

        TaskResponse response = taskService.create(USER_ID, request);

        assertNotNull(response);
        assertEquals("Học Spring Boot", response.getName());
        assertEquals("Ôn lại dependency injection", response.getNote());
        assertEquals("USD", response.getCurrency());
        assertEquals(USER_ID, response.getOwnerId());
        assertEquals(USER_ID, response.getUserId());
        assertEquals(TaskStatus.DRAFT, response.getStatus());
        assertEquals(PriorityLevel.HIGH, response.getPriority());
        assertEquals(120, response.getEstimatedMinutes());
        assertEquals(new BigDecimal("500000"), response.getEstimatedCost());
        assertEquals(FINANCE_ACCOUNT_ID, response.getFinanceAccountId());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createRejectsCategoryOutsideOwnerVisibility() {
        UUID categoryId = UUID.randomUUID();
        CreateTaskRequest request = new CreateTaskRequest();
        request.setName("Công việc riêng");
        request.setPriority(PriorityLevel.MEDIUM);
        request.setCategoryId(categoryId);

        when(taskRepository.findAllByNameAndOwnerId(request.getName(), USER_ID))
                .thenReturn(List.of());
        when(categoryRepository.findVisibleByIdAndOwnerId(categoryId, USER_ID))
                .thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> taskService.create(USER_ID, request));

        assertEquals(TaskErrorCode.CATEGORY_NOT_FOUND, exception.getCode());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void create_DuplicateName_ThrowsException() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setName("Học Spring Boot");

        when(taskRepository.findAllByNameAndOwnerId(request.getName(), USER_ID))
                .thenReturn(List.of(mockTask));

        assertThrows(RuntimeException.class, () -> taskService.create(USER_ID, request));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void create_AllowsDuplicateNameWhenDeadlineDiffers() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setName("Học Spring Boot");
        request.setDeadline(LocalDate.of(2026, 9, 2));
        mockTask.setDeadline(LocalDate.of(2026, 9, 1));

        when(taskRepository.findAllByNameAndOwnerId(request.getName(), USER_ID))
                .thenReturn(List.of(mockTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> {
            Task saved = i.getArgument(0);
            try {
                java.lang.reflect.Field idField = saved.getClass().getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(saved, UUID.randomUUID());
            } catch (Exception ignored) {}
            return saved;
        });

        TaskResponse response = taskService.create(USER_ID, request);

        assertEquals("Học Spring Boot", response.getName());
        assertEquals(LocalDate.of(2026, 9, 2), response.getDeadline());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void create_AllowsDuplicateNameWhenPlannedWindowDiffers() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setName("Học Spring Boot");
        request.setDeadline(LocalDate.of(2026, 9, 1));
        request.setPlannedStartAt(OffsetDateTime.parse("2026-09-01T09:00:00+07:00"));
        request.setPlannedEndAt(OffsetDateTime.parse("2026-09-01T10:00:00+07:00"));
        mockTask.setDeadline(request.getDeadline());
        mockTask.setPlannedStartAt(OffsetDateTime.parse("2026-09-01T11:00:00+07:00"));
        mockTask.setPlannedEndAt(OffsetDateTime.parse("2026-09-01T12:00:00+07:00"));

        when(taskRepository.findAllByNameAndOwnerId(request.getName(), USER_ID))
                .thenReturn(List.of(mockTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        TaskResponse response = taskService.create(USER_ID, request);

        assertEquals(request.getPlannedStartAt(), response.getPlannedStartAt());
        assertEquals(request.getPlannedEndAt(), response.getPlannedEndAt());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void update_Success() {
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setName("Học Spring Boot Nâng Cao");
        request.setDescription("Cập nhật mô tả");
        request.setNote("Cập nhật ghi chú");
        request.setPriority(PriorityLevel.CRITICAL);
        request.setDeadline(LocalDate.now().plusDays(5));
        request.setProgress(50);
        request.setEstimatedMinutes(180);
        request.setEstimatedCost(new BigDecimal("600000"));
        request.setFinanceAccountId(UPDATED_FINANCE_ACCOUNT_ID);
        request.setStatus(TaskStatus.PLANNED);

        when(taskRepository.findByIdAndOwnerId(TASK_ID, USER_ID)).thenReturn(Optional.of(mockTask));
        when(taskRepository.findAllByNameAndOwnerId(request.getName(), USER_ID)).thenReturn(List.of());
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        TaskResponse response = taskService.update(TASK_ID, USER_ID, request);

        assertNotNull(response);
        assertEquals("Học Spring Boot Nâng Cao", response.getName());
        assertEquals("Cập nhật ghi chú", response.getNote());
        assertEquals(PriorityLevel.CRITICAL, response.getPriority());
        assertEquals(50, response.getProgress());
        assertEquals(TaskStatus.PLANNED, response.getStatus());
        assertEquals(180, response.getEstimatedMinutes());
        assertEquals(UPDATED_FINANCE_ACCOUNT_ID, response.getFinanceAccountId());
        verify(taskRepository).save(mockTask);
    }

    @Test
    void getById_Success() {
        when(taskRepository.findByIdAndOwnerId(TASK_ID, USER_ID)).thenReturn(Optional.of(mockTask));

        TaskResponse response = taskService.getById(TASK_ID, USER_ID);

        assertNotNull(response);
        assertEquals(TASK_ID, response.getId());
        assertEquals(USER_ID, response.getOwnerId());
        assertEquals("Học Spring Boot", response.getName());
        assertEquals("Ôn lại dependency injection", response.getNote());
    }

    @Test
    void getByIdIncludesAssignedTagIds() {
        UUID tagId = UUID.randomUUID();
        Tag tag = Tag.builder()
                .id(tagId)
                .userId(USER_ID)
                .name("Thường dùng")
                .slug("thuong-dung")
                .build();
        mockTask.assignTag(tag);
        when(taskRepository.findByIdAndOwnerId(TASK_ID, USER_ID)).thenReturn(Optional.of(mockTask));

        TaskResponse response = taskService.getById(TASK_ID, USER_ID);

        assertNotNull(response.getTagIds());
        assertTrue(response.getTagIds().contains(tagId));
    }

    @Test
    void getById_NotFound_ThrowsException() {
        when(taskRepository.findByIdAndOwnerId(TASK_ID, USER_ID)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> taskService.getById(TASK_ID, USER_ID));
    }

    @Test
    void search_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> page = new PageImpl<>(List.of(mockTask), pageable, 1);
        when(taskRepository.findByOwnerIdAndNameContainingIgnoreCase(USER_ID, "Spring", pageable))
                .thenReturn(page);

        Page<TaskResponse> result = taskService.search(USER_ID, "Spring", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Học Spring Boot", result.getContent().get(0).getName());
    }

    @Test
    void delete_Success() {
        when(taskRepository.findByIdAndOwnerId(TASK_ID, USER_ID)).thenReturn(Optional.of(mockTask));

        taskService.delete(TASK_ID, USER_ID);

        assertNotNull(mockTask.getDeletedAt());
        assertEquals(mockTask.getDeletedAt(), mockTask.getUpdatedAt());
        verify(taskRepository).save(mockTask);
        verify(taskRepository, never()).delete(any(Task.class));
        verify(taskChangeHistoryService).recordTaskChange(
                eq(mockTask),
                eq(USER_ID),
                eq(TaskHistoryActionType.TASK_DELETED),
                isNull(),
                any(),
                isNull(),
                isNull());
    }

    @Test
    void delete_HidesSoftDeletedTaskFromSubsequentLookup() {
        when(taskRepository.findByIdAndOwnerId(TASK_ID, USER_ID))
                .thenReturn(Optional.of(mockTask))
                .thenReturn(Optional.empty());

        taskService.delete(TASK_ID, USER_ID);

        assertThrows(RuntimeException.class, () -> taskService.getById(TASK_ID, USER_ID));
        assertNotNull(mockTask.getDeletedAt());
        verify(taskRepository).save(mockTask);
    }

    @Test
    void deleteFinanceLinkedTask_AllowsCompletedTaskWithoutChangingRegularDeletePolicy() {
        mockTask.setStatus(TaskStatus.COMPLETED);
        when(taskRepository.findByIdAndOwnerId(TASK_ID, USER_ID)).thenReturn(Optional.of(mockTask));

        taskService.deleteFinanceLinkedTask(TASK_ID, USER_ID);

        assertNotNull(mockTask.getDeletedAt());
        assertEquals(mockTask.getDeletedAt(), mockTask.getUpdatedAt());
        verify(taskLifecyclePolicy, never()).validateDeleteAllowed(mockTask);
        verify(taskRepository).save(mockTask);
        verify(taskChangeHistoryService).recordTaskChange(
                eq(mockTask),
                eq(USER_ID),
                eq(TaskHistoryActionType.TASK_DELETED),
                isNull(),
                any(),
                isNull(),
                eq("Linked finance transaction was voided"));
        verify(taskIntegrationPublisher).publishTaskChanged(
                eq(mockTask),
                eq(USER_ID),
                eq(com.lifebalance.task.integration.TaskIntegrationAction.TASK_DELETED),
                eq("Linked finance transaction was voided"));
    }

    @Test
    void planUpdatesPlanningFieldsAndWritesHistory() {
        TaskPlanningRequest request = new TaskPlanningRequest();
        request.setPriority(PriorityLevel.CRITICAL);
        request.setDeadline(LocalDate.now().plusDays(7));
        request.setEstimatedMinutes(180);
        request.setEstimatedCost(new BigDecimal("800000"));
        request.setCurrency("VND");
        request.setFinanceAccountId(UPDATED_FINANCE_ACCOUNT_ID);
        request.setReason("Sprint planning");

        when(taskRepository.findByIdAndOwnerId(TASK_ID, USER_ID)).thenReturn(Optional.of(mockTask));
        when(taskRepository.save(mockTask)).thenReturn(mockTask);

        TaskResponse response = taskService.plan(TASK_ID, USER_ID, request);

        assertEquals(TaskStatus.PLANNED, response.getStatus());
        assertEquals(PriorityLevel.CRITICAL, response.getPriority());
        assertEquals(180, response.getEstimatedMinutes());
        assertEquals("VND", response.getCurrency());
        assertEquals(UPDATED_FINANCE_ACCOUNT_ID, response.getFinanceAccountId());
        verify(taskChangeHistoryService).recordTaskChange(
                eq(mockTask),
                eq(USER_ID),
                eq(TaskHistoryActionType.TASK_PLANNED),
                isNull(),
                any(),
                any(),
                eq("Sprint planning"));
    }

    @Test
    void planPreservesFinanceAccountWhenFieldIsOmitted() {
        TaskPlanningRequest request = new TaskPlanningRequest();
        request.setPriority(PriorityLevel.HIGH);
        request.setEstimatedMinutes(120);

        when(taskRepository.findByIdAndOwnerId(TASK_ID, USER_ID)).thenReturn(Optional.of(mockTask));
        when(taskRepository.save(mockTask)).thenReturn(mockTask);

        TaskResponse response = taskService.plan(TASK_ID, USER_ID, request);

        assertEquals(FINANCE_ACCOUNT_ID, response.getFinanceAccountId());
    }

    @Test
    void updateProgressRejectsDraftTaskBeforeMutation() {
        UpdateTaskProgressRequest request = new UpdateTaskProgressRequest();
        request.setProgress(40);

        when(taskRepository.findByIdAndOwnerId(TASK_ID, USER_ID)).thenReturn(Optional.of(mockTask));
        doThrow(TaskExceptions.progressNotAllowed(TaskStatus.DRAFT))
                .when(taskLifecyclePolicy)
                .validateProgressEditable(mockTask);

        AppException exception = assertThrows(AppException.class, () -> taskService.updateProgress(TASK_ID, USER_ID, request));

        assertEquals(TaskErrorCode.TASK_PROGRESS_NOT_ALLOWED, exception.getCode());
        assertEquals(0, mockTask.getProgress());
        verify(taskRepository, never()).save(any(Task.class));
        verify(taskChangeHistoryService, never()).recordTaskChange(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void completeSetsProgressToHundredAndWritesHistory() {
        mockTask.setStatus(TaskStatus.IN_PROGRESS);
        mockTask.setProgress(35);
        TaskLifecycleActionRequest request = new TaskLifecycleActionRequest();
        request.setReason("Done");

        when(taskRepository.findByIdAndOwnerId(TASK_ID, USER_ID)).thenReturn(Optional.of(mockTask));
        when(taskRepository.save(mockTask)).thenReturn(mockTask);

        TaskResponse response = taskService.complete(TASK_ID, USER_ID, request);

        assertEquals(TaskStatus.COMPLETED, response.getStatus());
        assertEquals(100, response.getProgress());
        assertNotNull(mockTask.getCompletedAt());
        verify(taskChangeHistoryService).recordTaskChange(
                eq(mockTask),
                eq(USER_ID),
                eq(TaskHistoryActionType.TASK_STATUS_CHANGED),
                eq("status"),
                eq(String.valueOf(TaskStatus.IN_PROGRESS)),
                eq(String.valueOf(TaskStatus.COMPLETED)),
                eq("Done"));
    }

    @Test
    void completeDoesNotRecordMonthlyIncomeForAnIntermediateOccurrence() {
        mockTask.setStatus(TaskStatus.IN_PROGRESS);
        mockTask.setMonthlyIncomeGroupId(UUID.randomUUID());
        mockTask.setMonthlyIncomeAccountId(UUID.randomUUID());
        mockTask.setMonthlyIncomeCurrency("VND");
        mockTask.setMonthlyIncomePeriod("2026-08");
        mockTask.setMonthlyIncomeBase(new BigDecimal("12000000"));
        mockTask.setMonthlyIncomeBonus(BigDecimal.ZERO);
        mockTask.setMonthlyIncomeDeduction(BigDecimal.ZERO);

        when(taskRepository.findByIdAndOwnerId(TASK_ID, USER_ID)).thenReturn(Optional.of(mockTask));
        when(taskRepository.save(mockTask)).thenReturn(mockTask);
        taskService.complete(TASK_ID, USER_ID, new TaskLifecycleActionRequest());

        verify(taskIntegrationPublisher, never()).publishMonthlyIncomeReady(any(), any(), any());
    }

    @Test
    void completeWaitsForUserConfirmationWhenItIsTheFinalMonthlyOccurrence() {
        mockTask.setStatus(TaskStatus.IN_PROGRESS);
        mockTask.setMonthlyIncomeGroupId(UUID.randomUUID());
        mockTask.setMonthlyIncomeAccountId(UUID.randomUUID());
        mockTask.setMonthlyIncomeCurrency("VND");
        mockTask.setMonthlyIncomePeriod("2026-08");
        mockTask.setMonthlyIncomeBase(new BigDecimal("12000000"));
        mockTask.setMonthlyIncomeBonus(BigDecimal.ZERO);
        mockTask.setMonthlyIncomeDeduction(BigDecimal.ZERO);

        when(taskRepository.findByIdAndOwnerId(TASK_ID, USER_ID)).thenReturn(Optional.of(mockTask));
        when(taskRepository.save(mockTask)).thenReturn(mockTask);
        taskService.complete(TASK_ID, USER_ID, new TaskLifecycleActionRequest());

        verify(taskIntegrationPublisher, never()).publishMonthlyIncomeReady(any(), any(), any());
    }

    @Test
    void cancelCancelsActiveTimelinePlacementsAndClearsScheduledWindow() {
        mockTask.setStatus(TaskStatus.SCHEDULED);
        OffsetDateTime startAt = OffsetDateTime.parse("2026-08-21T09:00:00+07:00");
        OffsetDateTime endAt = OffsetDateTime.parse("2026-08-21T10:00:00+07:00");
        mockTask.setScheduledWindow(startAt, endAt);
        TimelinePlacement placement = TimelinePlacement.builder()
                .id(UUID.randomUUID())
                .ownerId(USER_ID)
                .userId(USER_ID)
                .task(mockTask)
                .startAt(startAt)
                .endAt(endAt)
                .status(TimelinePlacementStatus.ACTIVE)
                .source("MANUAL")
                .build();
        TaskLifecycleActionRequest request = new TaskLifecycleActionRequest();
        request.setReason("Not needed");

        when(taskRepository.findByIdAndOwnerId(TASK_ID, USER_ID)).thenReturn(Optional.of(mockTask));
        when(timelinePlacementRepository.findByOwnerIdAndTaskIdAndStatus(
                USER_ID,
                TASK_ID,
                TimelinePlacementStatus.ACTIVE)).thenReturn(List.of(placement));
        when(taskRepository.save(mockTask)).thenReturn(mockTask);

        TaskResponse response = taskService.cancel(TASK_ID, USER_ID, request);

        assertEquals(TaskStatus.CANCELLED, response.getStatus());
        assertNull(response.getScheduledStartAt());
        assertEquals(TimelinePlacementStatus.CANCELLED, placement.getStatus());
        verify(timelinePlacementRepository).save(placement);
        verify(taskChangeHistoryService).recordTimelineChange(
                eq(mockTask),
                eq(placement),
                eq(USER_ID),
                eq(TaskHistoryActionType.TIMELINE_CANCELLED),
                any(),
                any(),
                eq("Not needed"));
    }

    @Test
    void deleteRejectsActiveTaskBeforeHistoryOrDelete() {
        mockTask.setStatus(TaskStatus.IN_PROGRESS);
        when(taskRepository.findByIdAndOwnerId(TASK_ID, USER_ID)).thenReturn(Optional.of(mockTask));
        doThrow(TaskExceptions.deleteNotAllowed(TaskStatus.IN_PROGRESS))
                .when(taskLifecyclePolicy)
                .validateDeleteAllowed(mockTask);

        AppException exception = assertThrows(AppException.class, () -> taskService.delete(TASK_ID, USER_ID));

        assertEquals(TaskErrorCode.TASK_DELETE_NOT_ALLOWED, exception.getCode());
        verify(taskChangeHistoryService, never()).recordTaskChange(any(), any(), any(), any(), any(), any(), any());
        verify(taskRepository, never()).save(any(Task.class));
        verify(taskRepository, never()).delete(any(Task.class));
    }

    // 1. KỊCH BẢN: TEST DUPLICATE TASK (NHÂN BẢN)
    @Test
    void duplicate_Success() {
        when(taskRepository.findByIdAndOwnerId(TASK_ID, USER_ID)).thenReturn(Optional.of(mockTask));
        when(taskRepository.findAllByNameAndOwnerId("Học Spring Boot (Copy)", USER_ID))
                .thenReturn(List.of());
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        TaskResponse response = taskService.duplicate(TASK_ID, USER_ID);

        assertNotNull(response);
        assertEquals("Học Spring Boot (Copy)", response.getName());
        assertEquals(120, response.getEstimatedMinutes());
        assertEquals(new BigDecimal("500000"), response.getEstimatedCost());
        assertEquals(FINANCE_ACCOUNT_ID, response.getFinanceAccountId());
        assertEquals(TaskStatus.DRAFT, response.getStatus());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void duplicate_TaskNotFound_ThrowsException() {
        when(taskRepository.findByIdAndOwnerId(TASK_ID, USER_ID)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> taskService.duplicate(TASK_ID, USER_ID));
        assertEquals("Task not found", exception.getMessage());
    }

    // 2. KỊCH BẢN: TEST ARCHIVE (LƯU TRỮ)
    @Test
    void archive_Success() {
        when(taskRepository.findByIdAndOwnerId(TASK_ID, USER_ID)).thenReturn(Optional.of(mockTask));

        taskService.archive(TASK_ID, USER_ID);

        verify(taskRepository, times(1)).save(mockTask);
        assertEquals(TaskStatus.ARCHIVED, mockTask.getStatus());
    }

    // 3. KỊCH BẢN: TEST RESTORE (KHÔI PHỤC)
    @Test
    void restore_Success() {
        mockTask.archive();
        when(taskRepository.findByIdAndOwnerId(TASK_ID, USER_ID)).thenReturn(Optional.of(mockTask));

        taskService.restore(TASK_ID, USER_ID);

        assertEquals(TaskStatus.PLANNED, mockTask.getStatus());
        verify(taskRepository, times(1)).save(mockTask);
    }
}
