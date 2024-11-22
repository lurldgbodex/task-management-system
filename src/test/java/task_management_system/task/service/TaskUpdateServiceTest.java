package task_management_system.task.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import task_management_system.dto.CustomResponse;
import task_management_system.exception.BadRequestException;
import task_management_system.exception.ForbiddenException;
import task_management_system.task.dto.UpdateTask;
import task_management_system.task.entity.Task;
import task_management_system.task.entity.TaskRole;
import task_management_system.task.enums.RoleType;
import task_management_system.task.enums.TaskStatus;
import task_management_system.task.repository.TaskRepository;
import task_management_system.task.repository.TaskRoleRepository;
import task_management_system.user.entity.User;
import task_management_system.user.repository.UserRepository;
import task_management_system.utils.TaskUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskUpdateServiceTest {

    @Mock private TaskUtils taskUtils;
    @Mock private TaskRepository taskRepository;
    @Mock private TaskRoleRepository taskRoleRepository;
    @Mock private TaskCacheManager taskCacheManager;
    @Mock private UserRepository userRepository;
    @InjectMocks private  TaskUpdateService underTest;


    @Test
    void testUpdateTask_Success() {
        UUID taskId = UUID.randomUUID();
        UpdateTask request = new UpdateTask();
        request.setTitle("Updated Title");
        request.setStatus("IN_PROGRESS");

        User authUser = new User();
        authUser.setId(UUID.randomUUID());

        Task task = new Task();
        task.setId(taskId);

        TaskRole role = new TaskRole();
        role.setRoleType(RoleType.CREATOR);

        TaskStatus status = TaskStatus.IN_PROGRESS;

        when(taskCacheManager.getTaskFromCache(taskId)).thenReturn(task);
        when(taskRoleRepository.findByTaskAndUser(task, authUser)).thenReturn(Optional.of(role));

        try (MockedStatic<TaskUtils> mockedStatic = mockStatic(TaskUtils.class)) {
            mockedStatic.when(() -> TaskUtils.parseEnum(TaskStatus.class, "IN_PROGRESS"))
                    .thenReturn(status);

            CustomResponse response = underTest.updateTask(taskId, request, authUser);

            assertNotNull(response);
            assertEquals("success", response.status());
            assertEquals("Task with id: " + taskId + " updated", response.message());

            verify(taskRepository).saveAndFlush(task);
            verify(taskCacheManager).evictTaskFromCache(taskId);
            verify(taskCacheManager).addTaskToCache(task);
        }
    }

    @Test
    void testUpdateTask_UnauthorizedAccess() {
        UUID taskId = UUID.randomUUID();
        UpdateTask request = new UpdateTask();
        User authUser = new User();
        authUser.setId(UUID.randomUUID());
        Task task = new Task();

        when(taskCacheManager.getTaskFromCache(taskId)).thenReturn(task);
        when(taskRoleRepository.findByTaskAndUser(task, authUser)).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () ->
                underTest.updateTask(taskId, request, authUser));
    }

    @Test
    void testUpdateTask_InvalidStatus() {
        UUID taskId = UUID.randomUUID();
        UpdateTask request = new UpdateTask();
        request.setStatus("INVALID_STATUS");

        User authUser = new User();
        authUser.setId(UUID.randomUUID());

        Task task = new Task();
        TaskRole role = new TaskRole();
        role.setRoleType(RoleType.CREATOR);

        try (MockedStatic<TaskUtils> mockedStatic = mockStatic(TaskUtils.class)) {
            mockedStatic.when(() -> TaskUtils.parseEnum(TaskStatus.class, "INVALID_STATUS"))
                    .thenThrow(BadRequestException.class);

            assertThrows(BadRequestException.class, () ->
                    underTest.updateTask(taskId, request, authUser));
        }
    }

    @Test
    void testUpdateTask_NonexistentAssignee() {
        UUID taskId = UUID.randomUUID();
        UpdateTask request = new UpdateTask();
        request.setAssigned_to("nonexistent@example.com");

        User authUser = new User();
        authUser.setId(UUID.randomUUID());

        Task task = new Task();
        TaskRole role = new TaskRole();
        role.setRoleType(RoleType.CREATOR);

        when(taskCacheManager.getTaskFromCache(taskId)).thenReturn(task);
        when(taskRoleRepository.findByTaskAndUser(task, authUser)).thenReturn(Optional.of(role));
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);

        CustomResponse response = underTest.updateTask(taskId, request, authUser);

        assertNotNull(response);
        verify(taskUtils, never()).assignRole(anyString(), any(Task.class), any(RoleType.class));
    }
}