package task_management_system.task.service;

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
import org.springframework.data.jpa.domain.Specification;
import task_management_system.task.dto.GetTasks;
import task_management_system.task.entity.Task;
import task_management_system.task.enums.TaskPriority;
import task_management_system.task.enums.TaskStatus;
import task_management_system.task.repository.TaskRepository;
import task_management_system.user.entity.User;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskQueryServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private TaskCacheManager taskCacheManager;
    @InjectMocks private TaskQueryService underTest;

    private Task task;

    @BeforeEach
    void setup() {
        task = Task.builder()
                .id(UUID.randomUUID())
                .title("Title")
                .build();
    }

    @Test
    void getTaskByID() {
        when(taskCacheManager.getTaskFromCache(task.getId())).thenReturn(task);

        Task response = underTest.getTaskByID(task.getId());

        assertEquals(response.getId(), task.getId());
        assertEquals(response.getTitle(), task.getTitle());

        verify(taskCacheManager).getTaskFromCache(task.getId());
    }

    @Test
    void getTasks() {
        Pageable pageable = PageRequest.of(0, 10);
        GetTasks request = GetTasks.builder()
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.MEDIUM)
                .authUser(new User())
                .build();

        List<Task> tasks = List.of(task);
        Page<Task> mockPage = new PageImpl<>(tasks, pageable, tasks.size());

        when(taskRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockPage);

        Page<Task> response = underTest.getTasks(pageable, request);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(taskRepository).findAll(any(Specification.class), eq(pageable));
    }
}