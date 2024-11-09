package task_management_system.task.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import task_management_system.dto.CustomResponse;
import task_management_system.exception.BadRequestException;
import task_management_system.exception.ForbiddenException;
import task_management_system.exception.NotFoundException;
import task_management_system.task.dto.CreateTaskRequest;
import task_management_system.task.dto.TaskDto;
import task_management_system.task.dto.UpdateTask;
import task_management_system.task.entity.Task;
import task_management_system.task.entity.TaskRole;
import task_management_system.task.enums.RoleType;
import task_management_system.task.enums.TaskPriority;
import task_management_system.task.enums.TaskStatus;
import task_management_system.task.repository.TaskRepository;
import task_management_system.task.repository.TaskRoleRepository;
import task_management_system.user.entity.User;
import task_management_system.utils.Utils;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private TaskRoleRepository roleRepository;
    @InjectMocks
    private TaskService underTest;

    private Task task;
    private User authUser;

    @BeforeEach
    void setup() {
        List<String> tags = new ArrayList<>();
        tags.add("setup-task");
        tags.add("Testing");

        authUser = User.builder()
                .email("user@email.com")
                .id(UUID.randomUUID())
                .build();

        task = Task.builder()
                .id(UUID.randomUUID())
                .title("Task title")
                .description("Task description")
                .status(TaskStatus.PENDING)
                .dueDate(LocalDateTime.now())
                .priority(TaskPriority.LOW)
                .assignedTo("unkwnown@jondoe.com")
                .tags(tags)
                .createdBy(authUser)
                .createdAt(LocalDateTime.MIN)
                .updatedAt(LocalDateTime.MIN)
                .build();
    }

    @Nested
    @DisplayName("Create Task Tests")
    class CreateTaskTest {
        private CreateTaskRequest createRequest;

        @BeforeEach
        void setUpCreateTask() {
            createRequest = CreateTaskRequest.builder()
                    .title("Create Task Title")
                    .description("Create Task description")
                    .dueDate("2020-10-21T20:12:40")
                    .status(TaskStatus.PENDING)
                    .priority(TaskPriority.HIGH)
                    .assignedTo("self@engineer-task.com")
                    .tags(List.of("Create", "Task", "Test"))
                    .build();
        }

        @Test
        @DisplayName("should create a Task when all fields are provided")
        void withAllFieldsProvided() {
            UUID id = UUID.randomUUID();

            try (MockedStatic<Utils> mockedStatic = mockStatic(Utils.class)) {
                mockedStatic.when(Utils::getAuthenticatedUser).thenReturn(authUser);

                when(taskRepository.saveAndFlush(any(Task.class))).thenAnswer(invocation -> {
                    Task newTask = invocation.getArgument(0);
                    newTask.setId(id);
                    return newTask;
                });

                TaskDto response = underTest.createTask(createRequest);

                ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
                verify(taskRepository, times(1)).saveAndFlush(taskCaptor.capture());

                Task savedTask = taskCaptor.getValue();

                // assert that the request data correct with saved data
                assertEquals(createRequest.getTitle(), savedTask.getTitle());
                assertEquals(createRequest.getDescription(), savedTask.getDescription());
                assertEquals(createRequest.getTags(), savedTask.getTags());
                assertEquals(createRequest.getStatus(), savedTask.getStatus());
                assertEquals(createRequest.getPriority(), savedTask.getPriority());
                assertEquals(createRequest.getAssignedTo(), savedTask.getAssignedTo());

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
        @DisplayName("should throw an exception when invalid dueDate format is passed")
        void withInvalidDueDateFormat() {
            createRequest.setDueDate("2024/11/21");

            try (MockedStatic<Utils> mockedStatic = mockStatic(Utils.class)) {
                mockedStatic.when(Utils::getAuthenticatedUser).thenReturn(authUser);

                assertThrows(BadRequestException.class, () ->
                        underTest.createTask(createRequest));
            }
        }
    }

    @Nested
    @DisplayName("Get Task By ID Tests")
    class GetTaskByIDTest {

        @Test
        @DisplayName("should successfully get task with valid taskID")
        void withValidID() {
            try (MockedStatic<Utils> mockedStatic = mockStatic(Utils.class)) {
                mockedStatic.when(Utils::getAuthenticatedUser).thenReturn(authUser);

                when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
                when(roleRepository.existsByTaskIdAndUserId(task.getId(), authUser.getId())).thenReturn(true);

                TaskDto response = underTest.getTaskByID(task.getId());

                assertEquals(task.getId(), response.getId());
                assertEquals(task.getTitle(), response.getTitle());
                assertEquals(task.getStatus(), response.getStatus());
                assertEquals(task.getDescription(), response.getDescription());
                assertEquals(task.getTags(), response.getTags());
                assertEquals(task.getPriority(), response.getPriority());
                assertEquals(task.getCreatedAt(), response.getCreatedAt());
                assertEquals(task.getAssignedTo(), response.getAssignedTo());
                assertEquals(task.getDueDate(), response.getDueDate());
                assertEquals(task.getUpdatedAt(), response.getUpdatedAt());

                verify(taskRepository, times(1)).findById(task.getId());
            }
        }

        @Test
        @DisplayName("should throw exception when get task with invalid id")
        void withInvalidID() {
            try (MockedStatic<Utils> mockedStatic = mockStatic(Utils.class)) {
                mockedStatic.when(Utils::getAuthenticatedUser).thenReturn(authUser);

                Exception exception = assertThrows(NotFoundException.class, () -> underTest.getTaskByID(task.getId()));
                assertEquals("Task not found with id: " +  task.getId(), exception.getMessage());

                verify(taskRepository, times(1)).findById(task.getId());
            }
        }
    }

    @Nested
    @DisplayName("Get All Task Tests")
    class GetAllTasksTest {

        @Test
        @DisplayName("should return list of tasks with valid page number")
        void withValidPageNumber() {
            int pageSize = 10;
            int pageNumber = 0;
            Pageable pageable = PageRequest.of(pageNumber, pageSize);

            List<Task> tasks = Collections.singletonList(task);
            Page<Task> taskPage = new PageImpl<>(tasks, pageable, tasks.size());

            try (MockedStatic<Utils> mockedStatic = mockStatic(Utils.class)) {
                mockedStatic.when(Utils::getAuthenticatedUser).thenReturn(authUser);

                when(taskRepository.findTasksByUserRoles(authUser.getId(), pageable)).thenReturn(taskPage);

                Page<TaskDto> response = underTest.getTasks(pageNumber, pageSize);

                assertEquals(1, response.getTotalPages(), "Expected one total page");
                assertEquals(10, response.getSize(), "Expected page size of 10");
                assertEquals(task.getId(), response.getContent().get(0).getId(), "Expects task ID to match");
            }
        }

        @Test
        @DisplayName("should return empty list with invalid page number")
        void withInvalidPageNumber() {

            int outOfRangePageNumber = 999;
            int pageSize = 10;
            Pageable pageable = PageRequest.of(outOfRangePageNumber, pageSize);

            try (MockedStatic<Utils> mockedStatic = mockStatic(Utils.class)) {
                mockedStatic.when(Utils::getAuthenticatedUser).thenReturn(authUser);

                when(taskRepository.findTasksByUserRoles(
                        authUser.getId(), pageable)).thenReturn(Page.empty(pageable));

                Page<TaskDto> result = underTest.getTasks(outOfRangePageNumber, pageSize);

                assertTrue(result.isEmpty(), "Expected an empty page for an out-of-range page number");
            }
        }

    }

    @Nested
    @DisplayName("Update Task Tests")
    class UpdateTaskTest {
        private TaskRole role;

        @BeforeEach
        void setup() {
            role = TaskRole.builder().build();
        }

        @Test
        @DisplayName("should update a task when a valid id is provided")
        void withValidID() {
            UpdateTask request = UpdateTask.builder()
                    .title("Update Title")
                    .build();

            role.setRoleType(RoleType.CREATOR);

            try (MockedStatic<Utils> mockedStatic = mockStatic(Utils.class)) {
                mockedStatic.when(Utils::getAuthenticatedUser).thenReturn(authUser);

                when(roleRepository.existsByTaskIdAndUserId(task.getId(), authUser.getId())).thenReturn(true);
                when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
                when(roleRepository.findByTaskAndUser(task, authUser)).thenReturn(Optional.of(role));

                assertEquals(task.getCreatedAt(), task.getUpdatedAt(),
                        "expects the created and updated at to be equal before update");

                String expectedMessage = "Task with id: " + task.getId() + " updated";
                CustomResponse response = underTest.updateTask(task.getId(), request);

                assertEquals("success", response.status(), "expects status to be 'success'");
                assertEquals(expectedMessage, response.message(), "expects response message to match");
                verify(taskRepository, times(1)).findById(task.getId());

                ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
                verify(taskRepository, times(1)).save(taskCaptor.capture());

                Task savedTask = taskCaptor.getValue();
                assertEquals(request.getTitle(), savedTask.getTitle(), "Expects title to be updated");
                assertEquals(task.getDescription(), savedTask.getDescription(), "Expects description to be updated");
                assertNotEquals(task.getCreatedAt(), task.getUpdatedAt(), "Expects updatedAt to be updated");
            }
        }

        @Test
        @DisplayName("should throw an exception when update with Invalid task id")
        void withInvalidID() {
            UUID invalidID = UUID.randomUUID();

            UpdateTask request = UpdateTask.builder()
                    .title("Full update title")
                    .description("Full update description")
                    .build();

            try (MockedStatic<Utils> mockedStatic = mockStatic(Utils.class)) {
                mockedStatic.when(Utils::getAuthenticatedUser).thenReturn(authUser);

                Exception exception = assertThrows(NotFoundException.class, () ->
                        underTest.updateTask(invalidID, request));
                assertEquals("Task not found with id: " +  invalidID, exception.getMessage());

                verify(taskRepository, times(1)).findById(invalidID);
                verify(taskRepository, never()).save(any(Task.class));
            }
        }

        @Test
        @DisplayName("should update task with full fields provided")
        void fullTaskUpdate() {
            List<String> tags = task.getTags();
            tags.add("new tag");

            UpdateTask request = UpdateTask.builder()
                    .title("Full update title")
                    .description("Full update description")
                    .status(TaskStatus.IN_PROGRESS)
                    .dueDate(LocalDateTime.MAX)
                    .priority(TaskPriority.MEDIUM)
                    .assignedTo("newuser@assigned.com")
                    .tags(tags)
                    .build();

            role.setRoleType(RoleType.CREATOR);

            try (MockedStatic<Utils> mockedStatic = mockStatic(Utils.class)) {
                mockedStatic.when(Utils::getAuthenticatedUser).thenReturn(authUser);

                when(roleRepository.existsByTaskIdAndUserId(task.getId(), authUser.getId())).thenReturn(true);
                when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
                when(roleRepository.findByTaskAndUser(task, authUser)).thenReturn(Optional.of(role));

                CustomResponse response = underTest.updateTask(task.getId(), request);

                String expectedMessage = "Task with id: " + task.getId() + " updated";

                // assert that response is correct
                assertEquals("success", response.status(), "Expects status to be 'success'");
                assertEquals(expectedMessage, response.message(), "Expects response message to match");
                verify(taskRepository, times(1)).findById(task.getId());

                ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
                verify(taskRepository, times(1)).save(taskCaptor.capture());

                Task savedTask = taskCaptor.getValue();

                // assert that data is correctly saved
                assertEquals(request.getTitle(), savedTask.getTitle(), "Expects title to be updated");
                assertEquals(request.getDescription(), savedTask.getDescription(), "Expects description to be updated");
                assertEquals(request.getStatus(), savedTask.getStatus(), "Expects status to be updated");
                assertEquals(request.getPriority(), savedTask.getPriority(), "Expects priority to be updated");
                assertEquals(request.getTags(), savedTask.getTags(), "Expects tags to be updated");
                assertEquals(request.getDueDate(), savedTask.getDueDate(), "Expects dueDate to be updated");
                assertEquals(request.getAssignedTo(), savedTask.getAssignedTo(),"Expects assignedTo to be updated");
            }
        }

        @Test
        @DisplayName("should partially update task")
        void partialTaskUpdate() {
            UpdateTask request = UpdateTask.builder()
                    .status(TaskStatus.COMPLETED)
                    .priority(TaskPriority.MEDIUM)
                    .build();

            role.setRoleType(RoleType.CREATOR);

            try (MockedStatic<Utils> mockedStatic = mockStatic(Utils.class)) {
                mockedStatic.when(Utils::getAuthenticatedUser).thenReturn(authUser);

                when(roleRepository.existsByTaskIdAndUserId(task.getId(), authUser.getId())).thenReturn(true);
                when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
                when(roleRepository.findByTaskAndUser(task, authUser)).thenReturn(Optional.of(role));

                CustomResponse response = underTest.updateTask(task.getId(), request);

                String expectedMessage = "Task with id: " + task.getId() + " updated";

                // assert that response is correct
                assertEquals("success", response.status(), "Expects status to be 'success'");
                assertEquals(expectedMessage, response.message(), "Expects response message to match");
                verify(taskRepository, times(1)).findById(task.getId());

                ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
                verify(taskRepository, times(1)).save(taskCaptor.capture());

                Task savedTask = taskCaptor.getValue();

                // assert that data is correctly saved
                assertEquals(request.getStatus(), savedTask.getStatus(), "Expects status to be updated");
                assertEquals(request.getPriority(), savedTask.getPriority(), "Expects priority to be updated");
            }
        }

        @Test
        @DisplayName("should only allow status update for non creator")
        void withUserNotCreator() {
            UpdateTask request = UpdateTask.builder()
                    .status(TaskStatus.COMPLETED)
                    .priority(TaskPriority.MEDIUM)
                    .build();

            role.setRoleType(RoleType.ASSIGNEE);

            try (MockedStatic<Utils> mockedStatic = mockStatic(Utils.class)) {
                mockedStatic.when(Utils::getAuthenticatedUser).thenReturn(authUser);

                when(roleRepository.existsByTaskIdAndUserId(task.getId(), authUser.getId())).thenReturn(true);
                when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
                when(roleRepository.findByTaskAndUser(task, authUser)).thenReturn(Optional.of(role));

                Exception ex = assertThrows(ForbiddenException.class,
                        () -> underTest.updateTask(task.getId(), request));

                assertEquals("Only the status can be updated by assigned users.", ex.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("Delete Task Tests")
    class DeleteTaskTests {

        @Test
        @DisplayName("should delete task with valid id")
        void withValidID() {
            try (MockedStatic<Utils> mockedStatic = mockStatic(Utils.class)) {
                mockedStatic.when(Utils::getAuthenticatedUser).thenReturn(authUser);

                TaskRole role = new TaskRole();
                role.setRoleType(RoleType.CREATOR);

                when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
                when(roleRepository.existsByTaskIdAndUserId(task.getId(), authUser.getId())).thenReturn(true);
                when(roleRepository.findByTaskAndUser(task, authUser)).thenReturn(Optional.of(role));

                CustomResponse response = underTest.deleteTask(task.getId());

                String expectedMessage = "Task with id: " + task.getId() + " deleted";

                assertEquals("success", response.status());
                assertEquals(expectedMessage, response.message());

                verify(taskRepository, times(1)).findById(task.getId());
                verify(taskRepository, times(1)).delete(task);
            }
        }

        @Test
        @DisplayName("should throw exception when delete with invalid id")
        void withInvalidID() {
            UUID invalidID = UUID.randomUUID();

            try (MockedStatic<Utils> mockedStatic = mockStatic(Utils.class)) {
                mockedStatic.when(Utils::getAuthenticatedUser).thenReturn(authUser);

                Exception exception = assertThrows(NotFoundException.class, () ->
                        underTest.deleteTask(invalidID));
                assertEquals("Task not found with id: " +  invalidID, exception.getMessage());

                verify(taskRepository, times(1)).findById(invalidID);
                verify(taskRepository, never()).save(any(Task.class));
            }
        }

        @Test
        @DisplayName("should throw forbidden when delete task without creator role")
        void withoutAuthorization() {
            try (MockedStatic<Utils> mockedStatic = mockStatic(Utils.class)) {
                mockedStatic.when(Utils::getAuthenticatedUser).thenReturn(authUser);

                TaskRole role = new TaskRole();
                role.setRoleType(RoleType.ASSIGNEE);

                when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
                when(roleRepository.existsByTaskIdAndUserId(task.getId(), authUser.getId())).thenReturn(true);
                when(roleRepository.findByTaskAndUser(task, authUser)).thenReturn(Optional.of(role));

                Exception exception = assertThrows(ForbiddenException.class, () ->
                        underTest.deleteTask(task.getId()));
                assertEquals("only creator of task can delete task", exception.getMessage());

                verify(taskRepository, times(1)).findById(task.getId());
                verify(roleRepository).findByTaskAndUser(task, authUser);
                verify(taskRepository, never()).save(any(Task.class));
            }
        }

        @Test
        @DisplayName("should throw forbidden when user is not associated to task")
        void withoutRoleOnTask() {
            try (MockedStatic<Utils> mockedStatic = mockStatic(Utils.class)) {
                mockedStatic.when(Utils::getAuthenticatedUser).thenReturn(authUser);

                when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

                Exception exception = assertThrows(ForbiddenException.class, () ->
                        underTest.deleteTask(task.getId()));

                assertEquals("You are not authorized to access this resource", exception.getMessage());

                verify(taskRepository, times(1)).findById(task.getId());
                verify(taskRepository, never()).save(any(Task.class));
            }
        }
    }
}