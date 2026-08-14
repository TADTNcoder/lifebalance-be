package com.lifebalance.task.service;

import com.lifebalance.task.dto.request.CreateTagRequest;
import com.lifebalance.task.dto.response.TagResponse;
import com.lifebalance.task.model.Tag;
import com.lifebalance.task.repository.TagRepository;
import com.lifebalance.task.service.impl.TagServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagServiceImpl tagService;

    private final UUID USER_ID = UUID.randomUUID();

    // 1. KỊCH BẢN: TEST TẠO TAG THÀNH CÔNG
    @Test
    void create_Success() {
        CreateTagRequest request = new CreateTagRequest();
        request.setName("Important");
        request.setDescription("Thẻ quan trọng");

        when(tagRepository.existsByUserIdAndName(USER_ID, "Important")).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenAnswer(i -> {
            Tag tag = i.getArgument(0);
            // Setup ID giả lập qua reflection nếu cần, hoặc cứ trả về tag gốc
            return tag;
        });

        TagResponse response = tagService.create(USER_ID, request);

        assertNotNull(response);
        assertEquals("Important", response.getName());
        verify(tagRepository, times(1)).existsByUserIdAndName(USER_ID, "Important");
        verify(tagRepository, times(1)).save(any(Tag.class));
    }

    // 2. KỊCH BẢN: TEST TẠO TAG BỊ TRÙNG TÊN (LỖI)
    @Test
    void create_DuplicateName_ThrowsException() {
        CreateTagRequest request = new CreateTagRequest();
        request.setName("Important");

        when(tagRepository.existsByUserIdAndName(USER_ID, "Important")).thenReturn(true);

        Exception exception = assertThrows(RuntimeException.class, () -> tagService.create(USER_ID, request));
        assertEquals("Tag name already exists", exception.getMessage());
        verify(tagRepository, never()).save(any(Tag.class)); // Bị trùng tên thì KHÔNG được lưu
    }
}
