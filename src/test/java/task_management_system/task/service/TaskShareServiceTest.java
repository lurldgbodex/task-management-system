package task_management_system.task.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import task_management_system.dto.CustomResponse;
import task_management_system.task.dto.ShareRequest;
import task_management_system.task.entity.SharedTask;
import task_management_system.task.entity.Task;
import task_management_system.task.enums.RoleType;
import task_management_system.task.repository.SharedTaskRepository;
import task_management_system.user.entity.User;
import task_management_system.utils.TaskUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskShareServiceTest {

    @Mock private TaskUtils taskUtils;
    @Mock private SharedTaskRepository sharedTaskRepository;
    @Mock private TaskCacheManager taskCacheManager;
    @InjectMocks private TaskShareService underTest;

    @Test
    @DisplayName("should share task")
    void shareTaskTest() {
        Task task = Task.builder()
                .id(UUID.randomUUID())
                .title("Task title")
                .build();

        ShareRequest request = ShareRequest.builder()
                .email("test@share.com")
                .build();

        User user = User.builder().build();

        when(taskCacheManager.getTaskFromCache(task.getId())).thenReturn(task);
        when(taskUtils.assignRole(request.getEmail(), task, RoleType.SHARED)).thenReturn(user);

        CustomResponse response = underTest.shareTask(request, task.getId());

        assertEquals("success", response.status());
        assertEquals("Task shared with " + request.getEmail(), response.message());

        verify(taskCacheManager).getTaskFromCache(task.getId());
        verify(taskUtils).assignRole(request.getEmail(), task, RoleType.SHARED);
        verify(sharedTaskRepository).save(any(SharedTask.class));
    }
}