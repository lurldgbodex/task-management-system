package task_management_system.task.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import task_management_system.dto.CustomResponse;
import task_management_system.task.dto.*;
import task_management_system.task.entity.Task;
import task_management_system.task.enums.TaskPriority;
import task_management_system.task.enums.TaskStatus;
import task_management_system.task.repository.TaskRepository;
import task_management_system.task.repository.TaskRoleRepository;
import task_management_system.user.entity.User;
import task_management_system.utils.TaskUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private TaskCreationService taskCreationService;
    @Mock
    private TaskQueryService taskQueryService;
    @Mock
    private TaskUpdateService taskUpdateService;
    @Mock
    private TaskShareService taskShareService;
    @Mock
    private TaskDeleteService taskDeleteService;
    @Mock
    private TaskRoleRepository roleRepository;
    @InjectMocks
    private TaskService underTest;

    private Task task;
    private User authUser;
    private TaskDto taskDto;

    @BeforeEach
    void setup() {
        authUser = User.builder()
                .email("user@email.com")
                .id(UUID.randomUUID())
                .build();

        task = Task.builder()
                .id(UUID.randomUUID())
                .title("Task title")
                .build();

        taskDto = TaskDto.builder()
                .id(UUID.randomUUID())
                .title(task.getTitle())
                .build();
    }

    @Test
    @DisplayName("should create a Task")
    void createTask() {
        CreateTaskRequest createRequest = CreateTaskRequest.builder()
                .title("Create Task Title")
                .build();

        try (MockedStatic<TaskUtils> mockedStatic = mockStatic(TaskUtils.class)) {
            mockedStatic.when(TaskUtils::getAuthenticatedUser).thenReturn(authUser);
            mockedStatic.when(() -> TaskUtils.convertToDto(task)).thenReturn(taskDto);

            when(taskCreationService.createTask(authUser, createRequest)).thenReturn(task);

            TaskDto response = underTest.createTask(createRequest);

            assertEquals(response.getId(), taskDto.getId());
            assertEquals(response.getTitle(), taskDto.getTitle());

            verify(taskCreationService).createTask(authUser, createRequest);
        }
    }

    @Test
    @Disabled
    @DisplayName("should retrieve task by id")
    void shouldGetTaskByID() {
        try (MockedStatic<TaskUtils> mockedStatic = mockStatic(TaskUtils.class)) {
            mockedStatic.when(TaskUtils::getAuthenticatedUser).thenReturn(authUser);
            mockedStatic.when(() -> TaskUtils.convertToDto(task)).thenReturn(taskDto);

            when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
            when(roleRepository.existsByTaskIdAndUserId(task.getId(), authUser.getId())).thenReturn(true);
            when(taskQueryService.getTaskByID(task.getId())).thenReturn(task);

            TaskDto response = underTest.getTaskByID(task.getId());

            assertEquals(task.getId(), response.getId());
            assertEquals(task.getTitle(), response.getTitle());

            verify(taskRepository, times(1)).findById(task.getId());
            verify(taskQueryService).getTaskByID(task.getId());
            verify(roleRepository).existsByTaskIdAndUserId(task.getId(), authUser.getId());
        }
    }

    @Test
    @Disabled
    @DisplayName("should return list of tasks paginated")
    void shouldGetAllTasks() {
        int pageSize = 10;
        int pageNumber = 0;

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        TaskStatus status = TaskStatus.COMPLETED;
        TaskPriority priority = TaskPriority.LOW;

        GetTasks request = GetTasks.builder()
                .authUser(authUser)
                .status(status)
                .priority(priority)
                .tags(Set.of())
                .build();

        List<Task> tasks = Collections.singletonList(task);
        Page<Task> taskPage = new PageImpl<>(tasks, pageable, tasks.size());
            Page<TaskDto> taskDto = taskPage.map(TaskUtils::convertToDto);

        when(taskQueryService.getTasks(pageable, request)).thenReturn(taskPage);
        PaginatedResponse<TaskDto> response = underTest.getTasks(pageable, null, null, null);

        assertEquals(1, response.getTotalPages(), "Expected one total page");
        assertEquals(10, response.getSize(), "Expected page size of 10");
        assertEquals(task.getId(), response.getContent().get(0).getId(), "Expects task ID to match");
    }

    @Test
    @DisplayName("should update a task")
    void shouldUpdateTAsk() {
        UpdateTask request = UpdateTask.builder()
                .title("Update Title")
                .build();

        try (MockedStatic<TaskUtils> mockedStatic = mockStatic(TaskUtils.class)) {
            mockedStatic.when(TaskUtils::getAuthenticatedUser).thenReturn(authUser);

            when(taskUpdateService.updateTask(task.getId(), request, authUser))
                    .thenReturn(new CustomResponse("s", "success"));


            CustomResponse response = underTest.updateTask(task.getId(), request);

            assertEquals("s", response.status());
            assertEquals("success", response.message());

            verify(taskUpdateService).updateTask(task.getId(), request, authUser);
        }
    }

    @Test
    @DisplayName("should delete task")
    void withValidID() {
        try (MockedStatic<TaskUtils> mockedStatic = mockStatic(TaskUtils.class)) {
            mockedStatic.when(TaskUtils::getAuthenticatedUser).thenReturn(authUser);

            when(taskDeleteService.deleteTask(task.getId(), authUser))
                    .thenReturn(new CustomResponse("success", "Task deleted"));

            CustomResponse response = underTest.deleteTask(task.getId());

            assertEquals("success", response.status());
            assertEquals("Task deleted", response.message());

            verify(taskDeleteService, times(1)).deleteTask(task.getId(), authUser);
        }
    }
}