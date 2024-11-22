package task_management_system.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import task_management_system.task.entity.Task;
import task_management_system.task.enums.TaskPriority;
import task_management_system.task.enums.TaskStatus;
import task_management_system.task.repository.TaskRepository;
import task_management_system.user.entity.User;
import task_management_system.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${seed.password:password}")
    private String password;

    @Override
    public void run(String... args) {
        if (isDatabaseEmpty()) {
            seedDatabase();
            log.info("Database seeded");
        } else {
            log.info("Database is not empty, skipping seeding.");
        }
    }

    private boolean isDatabaseEmpty() {
        return userRepository.count() == 0 &&
                taskRepository.count() == 0;
    }

    private void seedDatabase() {
        User user = User.builder()
                .email("random@email.com")
                .username("seed-user")
                .password(passwordEncoder.encode(password))
                .build();

        User user2 = User.builder()
                .email("seeded@user.com")
                .username("seed-user2")
                .password(passwordEncoder.encode(password))
                .build();

        userRepository.saveAllAndFlush(List.of(user, user2));

        Task task1 = Task.builder()
                .title("Seeded Task")
                .description("Task created when application starts")
                .createdBy(user)
                .dueDate(LocalDateTime.now())
                .tags(Set.of("Test", "seeded data"))
                .priority(TaskPriority.LOW)
                .status(TaskStatus.IN_PROGRESS)
                .build();

        Task task2 = Task.builder()
                .title("Seeded Task 2")
                .description("Task created when application starts")
                .createdBy(user2)
                .dueDate(LocalDateTime.now().plusDays(10))
                .tags(Set.of("Test", "seeded data"))
                .priority(TaskPriority.MEDIUM)
                .status(TaskStatus.PENDING)
                .build();

        Task task3 = Task.builder()
                .title("Seeded Task 3")
                .description("Task created when application starts")
                .createdBy(user)
                .dueDate(LocalDateTime.now().plusDays(20))
                .tags(Set.of("Test", "seeded data"))
                .priority(TaskPriority.MEDIUM)
                .status(TaskStatus.PENDING)
                .build();

        Task task4 = Task.builder()
                .title("Seeded Task 4")
                .description("Task created when application starts")
                .createdBy(user2)
                .dueDate(LocalDateTime.now().plusDays(30))
                .tags(Set.of("Test", "seeded data"))
                .priority(TaskPriority.LOW)
                .status(TaskStatus.IN_PROGRESS)
                .build();

        Task task5 = Task.builder()
                .title("Seeded Task 5")
                .description("Task created when application starts")
                .createdBy(user)
                .dueDate(LocalDateTime.now())
                .tags(Set.of("Test", "seeded data"))
                .priority(TaskPriority.MEDIUM)
                .status(TaskStatus.COMPLETED)
                .build();

        taskRepository.saveAll(List.of(task1, task2, task3, task4, task5));
    }
}
