package task_management_system.task.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import task_management_system.exception.BadRequestException;
import task_management_system.exception.ForbiddenException;
import task_management_system.exception.NotFoundException;
import task_management_system.task.dto.CreateTaskRequest;
import task_management_system.dto.CustomResponse;
import task_management_system.task.dto.TaskDto;
import task_management_system.task.dto.UpdateTask;
import task_management_system.task.entity.Task;
import task_management_system.task.entity.TaskRole;
import task_management_system.task.enums.RoleType;
import task_management_system.task.repository.TaskRepository;
import task_management_system.task.repository.TaskRoleRepository;
import task_management_system.user.entity.User;
import task_management_system.utils.Utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Service
@EnableCaching
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskRoleRepository taskRoleRepository;

    public TaskDto createTask(CreateTaskRequest taskRequest) {
        User authUser = Utils.getAuthenticatedUser();
        LocalDateTime dueDate = Utils.parseDateTime(taskRequest.getDueDate());

        if (dueDate.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("due date must be in the future to be valid");
        }

        Task task = Task.builder()
                .title(taskRequest.getTitle())
                .description(taskRequest.getDescription())
                .dueDate(dueDate)
                .status(taskRequest.getStatus())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(authUser)
                .priority(taskRequest.getPriority())
                .assignedTo(taskRequest.getAssignedTo())
                .tags(taskRequest.getTags())
                .build();

        task = saveTask(task);
        return convertToDto(task);
    }

    public TaskDto getTaskByID(UUID taskID) {
        User authUser = Utils.getAuthenticatedUser();
        Task task = getATaskByUser(taskID, authUser);

        return convertToDto(task);
    }

    @Cacheable(value = "tasks", key = "#authUser.id + ':' + #page + ':' + #size")
    public Page<TaskDto> getTasks(int page, int size) {
        User authUser = Utils.getAuthenticatedUser();

        Pageable pageable = PageRequest.of(page, size);
        Page<Task> tasks = taskRepository.findTasksByUserRoles(
                authUser.getId(),
                pageable
        );

        return tasks.map(this::convertToDto);
    }

    public CustomResponse updateTask(UUID taskID, UpdateTask updateRequest) {
        User authUser = Utils.getAuthenticatedUser();
        Task task = getATaskByUser(taskID, authUser);

        TaskRole role = taskRoleRepository.findByTaskAndUser(task, authUser)
                .orElseThrow(() -> new ForbiddenException("user has no role on this task"));

        if (!role.getRoleType().equals(RoleType.CREATOR) &&
                (updateRequest.getTitle() != null || updateRequest.getDescription() != null ||
                        updateRequest.getDueDate() != null || updateRequest.getPriority() != null ||
                        updateRequest.getAssignedTo() != null || updateRequest.getTags() != null)) {
            throw new ForbiddenException("Only the status can be updated by assigned users.");
        }

        updateFieldIfPresent(updateRequest.getTitle(), task::setTitle);
        updateFieldIfPresent(updateRequest.getDescription(), task::setDescription);
        updateFieldIfPresent(updateRequest.getStatus(), task::setStatus);
        updateFieldIfPresent(updateRequest.getDueDate(), task::setDueDate);
        updateFieldIfPresent(updateRequest.getPriority(), task::setPriority);
        updateFieldIfPresent(updateRequest.getAssignedTo(), task::setAssignedTo);
        updateFieldIfPresent(updateRequest.getTags(), task::setTags);

        task.setUpdatedAt(LocalDateTime.now());
        saveTask(task);

        return CustomResponse.builder()
                .status("success")
                .message("Task with id: " + taskID + " updated")
                .build();
    }

    public CustomResponse deleteTask(UUID taskID) {
        User authUser = Utils.getAuthenticatedUser();
        Task task = getATaskByUser(taskID, authUser);

        TaskRole role = taskRoleRepository.findByTaskAndUser(task, authUser)
                .orElseThrow();

        if (!role.getRoleType().equals(RoleType.CREATOR)) {
            throw new ForbiddenException("only creator of task can delete task");
        }

        deleteTask(task);
        return CustomResponse.builder()
                .status("success")
                .message("Task with id: " + taskID + " deleted")
                .build();
    }

    @CacheEvict(value = "tasks", key = "#taskID")
    private void deleteTask(Task task) {
        taskRepository.delete(task);
    }
    @Cacheable(value = "tasks", key = "#taskID", unless = "#result == null")
    private Optional<Task> findTaskByID(UUID taskID) {

        return taskRepository.findById(taskID);
    }

    private Task getATaskByUser(UUID taskID, User authUser) {

        Task task = findTaskByID(taskID)
                .orElseThrow(() -> new NotFoundException("Task not found with id: " +  taskID));

        boolean hasRole = checkUserRoleOnTask(taskID, authUser.getId());

        if (!hasRole) {
            throw new ForbiddenException("You are not authorized to access this resource");
        }

        return task;
    }

    @CachePut(value = "tasks", key = "#task.id")
    private Task saveTask(Task task) {
        return taskRepository.save(task);
    }

    @Cacheable(value = "taskRoles", key = "#taskID + ':' + #authUser.id", unless = "#result == false")
    private boolean checkUserRoleOnTask(UUID taskID, UUID userID) {
        return taskRoleRepository.existsByTaskIdAndUserId(taskID, userID);
    }
    private TaskDto convertToDto(Task task) {
        return TaskDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .priority(task.getPriority())
                .assignedTo(Optional.ofNullable(task.getAssignedTo())
                        .map(String::valueOf)
                        .orElse(null))
                .tags(Optional.ofNullable(task.getTags())
                        .orElse(Collections.emptyList()))
                .createdBy(task.getCreatedBy()
                        .getId())
                .build();
    }

    private <T> void updateFieldIfPresent(T fieldValue, Consumer<T> setter) {
        if (fieldValue != null) {
            setter.accept(fieldValue);
        }
    }
}
