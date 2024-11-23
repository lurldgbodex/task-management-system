package task_management_system.task.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import task_management_system.dto.CustomResponse;
import task_management_system.exception.ForbiddenException;
import task_management_system.task.entity.Task;
import task_management_system.task.entity.TaskRole;
import task_management_system.task.enums.RoleType;
import task_management_system.task.repository.TaskRepository;
import task_management_system.task.repository.TaskRoleRepository;
import task_management_system.user.entity.User;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskDeleteServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private TaskRoleRepository taskRoleRepository;
    @Mock private TaskCacheManager taskCacheManager;
    @InjectMocks private TaskDeleteService underTest;

    @Test
    void shouldDeleteTask() {
        Task task = Task.builder()
                .id(UUID.randomUUID())
                .title("Test Task")
                .build();

        User authUser = User.builder().build();

        TaskRole role = TaskRole.builder()
                .roleType(RoleType.CREATOR)
                .build();

        when(taskCacheManager.getTaskFromCache(task.getId())).thenReturn(task);
        when(taskRoleRepository.findByTaskAndUser(task, authUser))
                .thenReturn(Optional.of(role));

        CustomResponse response = underTest.deleteTask(task.getId(), authUser);

        assertEquals("success", response.status());
        assertEquals("Task with id: " + task.getId() + " deleted", response.message());

        verify(taskCacheManager).getTaskFromCache(task.getId());
        verify(taskRoleRepository).findByTaskAndUser(task, authUser);
        verify(taskRepository).delete(task);
        verify(taskCacheManager).evictTaskFromCache(task.getId());
    }

    @Test
    void deleteTask_whenUserNotCreator() {
        Task task = Task.builder()
                .id(UUID.randomUUID())
                .title("Test Task")
                .build();

        User authUser = User.builder().build();

        TaskRole role = TaskRole.builder()
                .roleType(RoleType.ASSIGNEE)
                .build();

        when(taskCacheManager.getTaskFromCache(task.getId())).thenReturn(task);
        when(taskRoleRepository.findByTaskAndUser(task, authUser))
                .thenReturn(Optional.of(role));

        assertThrows(ForbiddenException.class,
                () -> underTest.deleteTask(task.getId(), authUser));
    }
}