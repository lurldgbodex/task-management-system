package task_management_system.task.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import task_management_system.exception.BadRequestException;
import task_management_system.task.dto.CreateTaskRequest;
import task_management_system.task.entity.Task;
import task_management_system.task.enums.TaskPriority;
import task_management_system.task.enums.TaskStatus;
import task_management_system.task.repository.TaskRepository;
import task_management_system.user.entity.User;
import task_management_system.utils.TaskUtils;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class TaskCreationServiceTest {

    @Mock private TaskUtils taskUtils;
    @Mock private TaskRepository taskRepository;
    @Mock private TaskCacheManager taskCacheManager;
    @InjectMocks private TaskCreationService underTest;

    private CreateTaskRequest createRequest;

    private User authUser;

    @BeforeEach
    void setup() {
        authUser = User.builder()
                .email("user@email.com")
                .id(UUID.randomUUID())
                .build();

        createRequest = CreateTaskRequest.builder()
                .title("Create Task Title")
                .description("Create Task description")
                .due_date("2100-10-21T20:12:40")
                .status("pending")
                .priority("High")
                .assigned_to("self@engineer-task.com")
                .tags(Set.of("Create", "Task", "Test"))
                .build();
    }

    @Test
    @DisplayName("should create a Task when all fields are provided")
    void withAllFieldsProvided() {
        UUID id = UUID.randomUUID();
        LocalDateTime parsedDueDate = LocalDateTime.of(2100, 10, 21, 20, 12, 40);

        try (MockedStatic<TaskUtils> mockedStatic = mockStatic(TaskUtils.class)) {
            mockedStatic.when(() -> TaskUtils.parseDateTime(createRequest.getDue_date()))
                    .thenReturn(parsedDueDate);
            mockedStatic.when(() -> TaskUtils.parseEnum(TaskStatus.class, createRequest.getStatus()))
                            .thenReturn(TaskStatus.PENDING);
            mockedStatic.when(() -> TaskUtils.parseEnum(TaskPriority.class, createRequest.getPriority()))
                            .thenReturn(TaskPriority.HIGH);
            doNothing().when(taskCacheManager).addTaskToCache(any(Task.class));

            when(taskRepository.saveAndFlush(any(Task.class))).thenAnswer(invocation -> {
                Task newTask = invocation.getArgument(0);
                newTask.setId(id);
                return newTask;
            });

            Task response = underTest.createTask(authUser, createRequest);

            ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
            verify(taskRepository, times(1)).saveAndFlush(taskCaptor.capture());

            Task savedTask = taskCaptor.getValue();

            // assert that the request data correct with saved data
            assertEquals(createRequest.getTitle(), savedTask.getTitle());
            assertEquals(createRequest.getDescription(), savedTask.getDescription());
            assertEquals(createRequest.getTags(), savedTask.getTags());
            assertEquals(createRequest.getStatus().toUpperCase(), savedTask.getStatus().toString());
            assertEquals(createRequest.getPriority().toUpperCase(), savedTask.getPriority().toString());
            assertEquals(createRequest.getAssigned_to(), savedTask.getAssignedTo());

            // assert that the response data is correct
            assertNotNull(response);
            assertEquals(response.getId(), savedTask.getId());
            assertEquals(response.getTitle(), savedTask.getTitle());
            assertEquals(response.getDescription(), savedTask.getDescription());
            assertEquals(response.getDueDate(), savedTask.getDueDate());
            assertEquals(response.getTags(), savedTask.getTags());
            assertEquals(response.getStatus(), savedTask.getStatus());
            assertEquals(response.getPriority(), savedTask.getPriority());
            assertEquals(response.getAssignedTo(), savedTask.getAssignedTo());
            assertEquals(response.getCreatedAt(), savedTask.getCreatedAt());
            assertEquals(response.getUpdatedAt(), savedTask.getUpdatedAt());
        }
    }

    @Test
    @DisplayName("should throw an exception when invalid due date is passed")
    void withInvalidDueDateFormat() {
        createRequest.setDue_date(LocalDateTime.now().toString());
        LocalDateTime parsedDueDate = LocalDateTime.now().minusDays(2);


        try (MockedStatic<TaskUtils> mockedStatic = mockStatic(TaskUtils.class)) {
            mockedStatic.when(() -> TaskUtils.parseDateTime(createRequest.getDue_date()))
                    .thenReturn(parsedDueDate);

            assertThrows(BadRequestException.class, () ->
                    underTest.createTask(authUser, createRequest));
        }
    }

    @Test
    @DisplayName("should throw an exception when invalid Task status provided")
    void withInvalidStatus() {
        createRequest.setStatus("invalid-status");
        createRequest.setDue_date(LocalDateTime.now().toString());
        LocalDateTime parsedDueDate = LocalDateTime.now().plusDays(10);

        try (MockedStatic<TaskUtils> mockedStatic = mockStatic(TaskUtils.class)) {
            mockedStatic.when(() -> TaskUtils.parseDateTime(createRequest.getDue_date()))
                    .thenReturn(parsedDueDate);
            mockedStatic.when(() -> TaskUtils.parseEnum(TaskStatus.class, createRequest.getStatus()))
                            .thenThrow(BadRequestException.class);

            assertThrows(BadRequestException.class, () ->
                    underTest.createTask(authUser, createRequest));

        }
    }

    @Test
    @DisplayName("should throw an exception when invalid priority is provided")
    void withInvalidPriority() {
        createRequest.setPriority("invalid-priority");
        createRequest.setDue_date(LocalDateTime.now().toString());

        LocalDateTime parsedDueDate = LocalDateTime.now().plusMonths(2);

        try (MockedStatic<TaskUtils> mockedStatic = mockStatic(TaskUtils.class)) {
            mockedStatic.when(TaskUtils::getAuthenticatedUser).thenReturn(authUser);
            mockedStatic.when(() -> TaskUtils.parseDateTime(createRequest.getDue_date()))
                    .thenReturn(parsedDueDate);
            mockedStatic.when(() -> TaskUtils.parseEnum(TaskPriority.class, createRequest.getPriority()))
                    .thenThrow(BadRequestException.class);

            assertThrows(BadRequestException.class, () ->
                    underTest.createTask(authUser, createRequest));
        }
    }
}