package task_management_system.task.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import task_management_system.dto.CustomResponse;
import task_management_system.exception.ForbiddenException;
import task_management_system.task.dto.UpdateTask;
import task_management_system.task.entity.Task;
import task_management_system.task.entity.TaskRole;
import task_management_system.task.enums.RoleType;
import task_management_system.task.enums.TaskPriority;
import task_management_system.task.enums.TaskStatus;
import task_management_system.task.enums.UpdateOperation;
import task_management_system.task.repository.TaskRepository;
import task_management_system.task.repository.TaskRoleRepository;
import task_management_system.user.entity.User;
import task_management_system.user.repository.UserRepository;
import task_management_system.utils.Validator;
import task_management_system.utils.TaskUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class TaskUpdateService {

    private final TaskUtils taskUtils;
    private final TaskRepository taskRepository;
    private final TaskRoleRepository taskRoleRepository;
    private final TaskCacheManager taskCacheManager;
    private final UserRepository userRepository;

    @Transactional
    public CustomResponse updateTask(UUID taskID, UpdateTask request, User authUser) {

        TaskStatus status = request.getStatus() != null
                ? TaskUtils.parseEnum(TaskStatus.class, request.getStatus())
                : null;

        TaskPriority priority = request.getPriority() != null
                ? TaskUtils.parseEnum(TaskPriority.class, request.getPriority())
                : null;

        Task task = taskCacheManager.getTaskFromCache(taskID);
        TaskRole role = taskRoleRepository.findByTaskAndUser(task, authUser)
                .orElseThrow(() -> new ForbiddenException("Unauthorized access"));

        // map request fields to operations
        Map<UpdateOperation, Runnable> operations = Map.of(
                UpdateOperation.TITLE, () -> validateAndSet(task::setTitle, request.getTitle(), role, UpdateOperation.TITLE),
                UpdateOperation.DESCRIPTION, () -> validateAndSet(task::setDescription, request.getDescription(), role, UpdateOperation.DESCRIPTION),
                UpdateOperation.STATUS, () -> validateAndSet(task::setStatus, status, role, UpdateOperation.STATUS),
                UpdateOperation.DUE_DATE, () -> validateAndSet(task::setDueDate, request.getDue_date(), role, UpdateOperation.DUE_DATE),
                UpdateOperation.PRIORITY, () -> validateAndSet(task::setPriority, priority, role, UpdateOperation.PRIORITY),
                UpdateOperation.ASSIGNEE, () -> {
                    if (request.getAssigned_to() != null && userExists(request.getAssigned_to())) {
                        validateAndSet(task::setAssignedTo, request.getAssigned_to(), role, UpdateOperation.ASSIGNEE);
                        taskUtils.assignRole(request.getAssigned_to(), task, RoleType.ASSIGNEE);
                    }
                },
                UpdateOperation.TAGS, () -> validateAndSet(task::setTags, request.getTags(), role, UpdateOperation.TAGS)
        );

        // Perform updates based on non-null fields
        operations.forEach((op, action) -> {
            if (isFieldSet(request, op)) {
                action.run();
            }
        });

        task.setUpdatedAt(LocalDateTime.now());

        taskRepository.saveAndFlush(task);

        taskCacheManager.evictTaskFromCache(taskID);
        taskCacheManager.addTaskToCache(task);

        return CustomResponse.builder()
                .status("success")
                .message("Task with id: " + taskID + " updated")
                .build();
    }

    private <T> void validateAndSet(Consumer<T> setter, T value, TaskRole role, UpdateOperation operation) {
        Validator.validateUpdatePermission(role.getRoleType(), operation);
        setter.accept(value);
    }

    private boolean isFieldSet(UpdateTask request, UpdateOperation operation) {
        return switch (operation) {
            case TITLE -> request.getTitle() != null;
            case DESCRIPTION -> request.getDescription() != null;
            case STATUS -> request.getStatus() != null;
            case DUE_DATE -> request.getDue_date() != null;
            case PRIORITY -> request.getPriority() != null;
            case ASSIGNEE -> request.getAssigned_to() != null;
            case TAGS -> request.getTags() != null;
        };
    }

    private boolean userExists(String email) {
        if (email == null) return false;
        return userRepository.existsByEmail(email);
    }
}

