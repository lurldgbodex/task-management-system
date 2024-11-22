package task_management_system.task.repository;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import task_management_system.task.entity.Task;
import task_management_system.task.entity.TaskRole;
import task_management_system.task.enums.RoleType;
import task_management_system.task.enums.TaskPriority;
import task_management_system.task.enums.TaskStatus;
import task_management_system.user.entity.User;
import task_management_system.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase
@ExtendWith(MockitoExtension.class)
class TaskRoleRepositoryTest {

    @Autowired
    private TaskRoleRepository underTest;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskRepository taskRepository;

    private Task task;
    private User user;
    private User user2;
    private TaskRole taskRole;

    @BeforeEach
    void setup() {
        user = User.builder()
                .email("creator@task.com")
                .password("password")
                .build();

        user2 = User.builder()
                .email("user2@email.com")
                .password("password")
                .build();

        userRepository.saveAllAndFlush(List.of(user, user2));

        task = Task.builder()
                .title("Task title")
                .description("Task description")
                .dueDate(LocalDateTime.now())
                .createdBy(user)
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.MEDIUM)
                .tags(Set.of("jpa-test", "test"))
                .build();



        taskRepository.saveAndFlush(task);


        taskRole = new TaskRole();

        taskRole.setTask(task);
        taskRole.setUser(user);
        taskRole.setRoleType(RoleType.CREATOR);
    }

    @AfterEach
    void tearDown() {
        taskRepository.delete(task);
        userRepository.deleteAll(List.of(user, user2));
    }

    @Nested
    @DisplayName("Task exist by taskID and UserID")
    class ExistsByTaskIDAndUserID {

        @Test
        @DisplayName("should return true when task exists")
        void withValidTaskIDAndUserID() {
            boolean response = underTest.existsByTaskIdAndUserId(task.getId(), user.getId());
            assertTrue(response);
        }

        @Test
        @DisplayName("should return false when incorrect taskID")
        void withInvalidTaskID() {
            boolean response = underTest.existsByTaskIdAndUserId(UUID.randomUUID(), user.getId());
            assertFalse(response);
        }

        @Test
        @DisplayName("should return false when incorrect userID")
        void withInvalidUserID() {
            boolean response = underTest.existsByTaskIdAndUserId(task.getId(), UUID.randomUUID());
            assertFalse(response);
        }

        @Test
        @DisplayName("should return false when incorrect taskID and userID")
        void withInvalidTaskIDAndUserID() {
            boolean response = underTest.existsByTaskIdAndUserId(UUID.randomUUID(), UUID.randomUUID());
            assertFalse(response);
        }
    }


    @Nested
    @DisplayName("User Role on Task")
    class TaskByRoleAndUser {

        @Test
        @DisplayName("should find user role on task")
        void withValidTaskAndUser() {
            Optional<TaskRole> response = underTest.findByTaskAndUser(task, user);

            assertTrue(response.isPresent());
            assertEquals(response.get().getTask(), taskRole.getTask());
            assertEquals(response.get().getUser(), taskRole.getUser());
            assertEquals(response.get().getRoleType(), taskRole.getRoleType());
        }

        @Test
        @DisplayName("should not find role when user do not have role on task")
        void withDifferentTask() {
            Optional<TaskRole> response = underTest.findByTaskAndUser(task, user2);

            assertTrue(response.isEmpty());
        }
    }

    @Nested
    @DisplayName("Task exists by User and RoleType")
    class ExistsByTaskUserAndRole {

        @Test
        @DisplayName("should find tasks with user and role type")
        void withValidTaskAndUserAndRoleType() {
            boolean response = underTest.existsByTaskAndUserAndRoleType(task, user, RoleType.CREATOR);

            assertTrue(response);
        }

        @Test
        @DisplayName("should not find task with different user")
        void withDifferentUser() {
            boolean response = underTest.existsByTaskAndUserAndRoleType(task, user2, RoleType.CREATOR);

            assertFalse(response);
        }

        @Test
        @DisplayName("should not find task with different role")
        void withIncorrectRole() {
            boolean response = underTest.existsByTaskAndUserAndRoleType(task, user, RoleType.ASSIGNEE);

            assertFalse(response);
        }
    }
}