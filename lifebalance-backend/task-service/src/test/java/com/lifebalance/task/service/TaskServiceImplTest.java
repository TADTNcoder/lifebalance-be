package com.lifebalance.task.service;

import com.lifebalance.task.dto.request.CreateTaskRequest;
import com.lifebalance.task.dto.request.UpdateTaskRequest;
import com.lifebalance.task.dto.response.TaskResponse;
import com.lifebalance.task.history.TaskChangeHistoryService;
import com.lifebalance.task.model.Category;
import com.lifebalance.task.model.Task;
import com.lifebalance.task.model.enums.PriorityLevel;
import com.lifebalance.task.model.enums.TaskStatus;
import com.lifebalance.task.repository.CategoryRepository;
import com.lifebalance.task.repository.TaskRepository;
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
    private TaskLifecyclePolicy taskLifecyclePolicy;

    @Mock
    private TaskChangeHistoryService taskChangeHistoryService;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task mockTask;
    private final UUID TASK_ID = UUID.randomUUID();
    private final UUID USER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockTask = Task.builder()
                .ownerId(USER_ID)
                .userId(USER_ID)
                .name("Học Spring Boot")
                .description("Học để làm đồ án")
                .estimatedMinutes(120)
                .estimatedCost(new BigDecimal("500000"))
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
        request.setPriority(PriorityLevel.HIGH);
        request.setDeadline(LocalDate.now().plusDays(3));
        request.setEstimatedMinutes(120);
        request.setEstimatedCost(new BigDecimal("500000"));

        when(taskRepository.findByNameAndOwnerId(request.getName(), USER_ID))
                .thenReturn(Optional.empty());
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
        assertEquals(USER_ID, response.getOwnerId());
        assertEquals(USER_ID, response.getUserId());
        assertEquals(TaskStatus.DRAFT, response.getStatus());
        assertEquals(PriorityLevel.HIGH, response.getPriority());
        assertEquals(120, response.getEstimatedMinutes());
        assertEquals(new BigDecimal("500000"), response.getEstimatedCost());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void create_DuplicateName_ThrowsException() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setName("Học Spring Boot");

        when(taskRepository.findByNameAndOwnerId(request.getName(), USER_ID))
                .thenReturn(Optional.of(mockTask));

        assertThrows(RuntimeException.class, () -> taskService.create(USER_ID, request));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void update_Success() {
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setName("Học Spring Boot Nâng Cao");
        request.setDescription("Cập nhật mô tả");
        request.setPriority(PriorityLevel.CRITICAL);
        request.setDeadline(LocalDate.now().plusDays(5));
        request.setProgress(50);
        request.setEstimatedMinutes(180);
        request.setEstimatedCost(new BigDecimal("600000"));
        request.setStatus(TaskStatus.PLANNED);

        when(taskRepository.findByIdAndOwnerId(TASK_ID, USER_ID)).thenReturn(Optional.of(mockTask));
        when(taskRepository.findByNameAndOwnerId(request.getName(), USER_ID)).thenReturn(Optional.empty());
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        TaskResponse response = taskService.update(TASK_ID, USER_ID, request);

        assertNotNull(response);
        assertEquals("Học Spring Boot Nâng Cao", response.getName());
        assertEquals(PriorityLevel.CRITICAL, response.getPriority());
        assertEquals(50, response.getProgress());
        assertEquals(TaskStatus.PLANNED, response.getStatus());
        assertEquals(180, response.getEstimatedMinutes());
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

        verify(taskRepository).delete(mockTask);
    }

    // 1. KỊCH BẢN: TEST DUPLICATE TASK (NHÂN BẢN)
    @Test
    void duplicate_Success() {
        when(taskRepository.findByIdAndOwnerId(TASK_ID, USER_ID)).thenReturn(Optional.of(mockTask));
        when(taskRepository.findByNameAndOwnerId("Học Spring Boot (Copy)", USER_ID))
                .thenReturn(Optional.empty());
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        TaskResponse response = taskService.duplicate(TASK_ID, USER_ID);

        assertNotNull(response);
        assertEquals("Học Spring Boot (Copy)", response.getName());
        assertEquals(120, response.getEstimatedMinutes());
        assertEquals(new BigDecimal("500000"), response.getEstimatedCost());
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
