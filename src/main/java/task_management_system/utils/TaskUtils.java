package task_management_system.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import task_management_system.exception.BadRequestException;
import task_management_system.exception.NotFoundException;
import task_management_system.exception.UnauthorizedException;
import task_management_system.task.dto.TaskDto;
import task_management_system.task.entity.Task;
import task_management_system.task.entity.TaskRole;
import task_management_system.task.enums.RoleType;
import task_management_system.task.repository.TaskRoleRepository;
import task_management_system.user.entity.User;
import task_management_system.user.repository.UserRepository;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TaskUtils {

    private final UserRepository userRepository;
    private final TaskRoleRepository taskRoleRepository;

    public static User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        } throw new UnauthorizedException("user not authenticated");
    }

    public static LocalDateTime parseDateTime(String dateTime) {
        try {
            return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        } catch (DateTimeException dte) {
            throw new BadRequestException(dte.getMessage());
        }
    }

    public static <T extends Enum<T>> T parseEnum(Class<T> enumType, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Enum.valueOf(enumType, value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(
                    String.format("Invalid value '%s' for %s. Allow values are: %s",
                            value, enumType.getSimpleName(), Arrays.toString(enumType.getEnumConstants())
                    )
            );
        }
    }

    public static TaskDto convertToDto(Task task) {
        return TaskDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .due_date(task.getDueDate())
                .created_at(task.getCreatedAt())
                .updated_at(task.getUpdatedAt())
                .priority(task.getPriority())
                .assigned_to(Optional.ofNullable(task.getAssignedTo())
                        .map(String::valueOf)
                        .orElse(null))
                .tags(Optional.ofNullable(task.getTags())
                        .orElse(Collections.emptySet()))
                .created_by(task.getCreatedBy()
                        .getId())
                .build();
    }

    public User assignRole(String email, Task task, RoleType roleType) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("user not found with email: " + email));

        boolean roleExists = taskRoleRepository.existsByTaskAndUserAndRoleType(task, user, roleType);
        if (!roleExists) {
            TaskRole role = TaskRole.builder()
                    .user(user)
                    .task(task)
                    .roleType(roleType)
                    .build();

            taskRoleRepository.save(role);
        }
        return user;
    }
}
