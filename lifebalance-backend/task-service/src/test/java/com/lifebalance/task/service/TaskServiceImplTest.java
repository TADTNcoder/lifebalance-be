package com.lifebalance.task.service;

import com.lifebalance.task.dto.response.TaskResponse;
import com.lifebalance.task.model.Task;
import com.lifebalance.task.model.enums.TaskStatus;
import com.lifebalance.task.repository.CategoryRepository;
import com.lifebalance.task.repository.TaskRepository;
import com.lifebalance.task.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task mockTask;
    private final UUID TASK_ID = UUID.randomUUID();
    private final UUID USER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockTask = Task.builder()
                .userId(USER_ID)
                .name("Học Spring Boot")
                .description("Học để làm đồ án")
                .estimatedMinutes(120)
                .estimatedCost(new BigDecimal("500000"))
                .status(TaskStatus.DRAFT)
                .build();
        // Set ID thủ công vì Builder không có ID (do @Id @GeneratedValue)
        try {
            java.lang.reflect.Field idField = mockTask.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(mockTask, TASK_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 1. KỊCH BẢN: TEST DUPLICATE TASK (NHÂN BẢN)
    @Test
    void duplicate_Success() {
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(mockTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> {
            Task savedTask = i.getArgument(0);
            return savedTask;
        });

        TaskResponse response = taskService.duplicate(TASK_ID);

        assertNotNull(response);
        assertEquals("Học Spring Boot (Copy)", response.getName()); // Kiểm tra có chữ (Copy) không
        assertEquals(120, response.getEstimatedMinutes());
        assertEquals(new BigDecimal("500000"), response.getEstimatedCost());
        assertEquals(TaskStatus.DRAFT, response.getStatus()); // Task mới phải là DRAFT
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void duplicate_TaskNotFound_ThrowsException() {
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> taskService.duplicate(TASK_ID));
        assertEquals("Task not found", exception.getMessage());
    }

    // 2. KỊCH BẢN: TEST ARCHIVE (LƯU TRỮ)
    @Test
    void archive_Success() {
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(mockTask));

        // Gọi hàm giả lập task.archive() vì không thấy ruột của hàm này trong file gửi
        // Thông thường hàm này sẽ đổi trạng thái hoặc set deleted_at
        taskService.archive(TASK_ID);

        verify(taskRepository, times(1)).save(mockTask);
    }

    // 3. KỊCH BẢN: TEST RESTORE (KHÔI PHỤC)
    @Test
    void restore_Success() {
        mockTask.archive();
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(mockTask));

        taskService.restore(TASK_ID);

        assertEquals(TaskStatus.PLANNED, mockTask.getStatus());
        verify(taskRepository, times(1)).save(mockTask);
    }
}
