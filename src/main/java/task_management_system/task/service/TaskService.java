package task_management_system.task.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskRoleRepository taskRoleRepository;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Transactional
    public TaskDto createTask(CreateTaskRequest taskRequest) {
        User authUser = Utils.getAuthenticatedUser();
        LocalDateTime dueDate;

        try {
            dueDate = LocalDateTime.parse(taskRequest.getDueDate(), DATE_TIME_FORMATTER);
        } catch (DateTimeException dte) {
            throw new BadRequestException(dte.getMessage());
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

        TaskRole role = TaskRole.builder()
                .user(authUser)
                .roleType(RoleType.CREATOR)
                .build();

        taskRoleRepository.save(role);

        taskRepository.saveAndFlush(task);
        return convertToDo(task);
    }

    public TaskDto getTaskByID(UUID taskID) {
        User authUser = Utils.getAuthenticatedUser();
        Task task = findTaskByID(taskID, authUser);

        return convertToDo(task);
    }

    public Page<TaskDto> getTasks(int page, int size) {
        User authUser = Utils.getAuthenticatedUser();

        Pageable pageable = PageRequest.of(page, size);
        Page<Task> tasks = taskRepository.findTasksByUserRoles(
                authUser.getId(),
                pageable
        );

        return tasks.map(this::convertToDo);
    }

    public CustomResponse updateTask(UUID taskID, UpdateTask updateRequest) {
        User authUser = Utils.getAuthenticatedUser();
        Task task = findTaskByID(taskID, authUser);

        TaskRole role = taskRoleRepository.findByTaskAndUser(task, authUser)
                .orElseThrow(() -> new ForbiddenException("user has no role on this task"));

        if (!role.getRoleType().equals(RoleType.CREATOR)) {
            if (updateRequest.getTitle() != null ||
                    updateRequest.getDescription() != null ||
                    updateRequest.getDueDate() != null ||
                    updateRequest.getPriority() != null ||
                    updateRequest.getAssignedTo() != null ||
                    updateRequest.getTags() != null) {
                throw new ForbiddenException("Only the status can be updated by assigned users.");
            }
        }

        task.setTitle(updateRequest.getTitle() != null
                ? updateRequest.getTitle() : task.getTitle());
        task.setDescription(updateRequest.getDescription() != null
                ? updateRequest.getDescription() : task.getDescription());
        task.setStatus(updateRequest.getStatus() != null
                ? updateRequest.getStatus() : task.getStatus());
        task.setDueDate(updateRequest.getDueDate() != null
                ? updateRequest.getDueDate() : task.getDueDate());
        task.setPriority(updateRequest.getPriority() != null
                ? updateRequest.getPriority() : task.getPriority());
        task.setAssignedTo(updateRequest.getDescription() != null
                ? updateRequest.getAssignedTo() : task.getAssignedTo());
        task.setTags(updateRequest.getTags() != null
                ? updateRequest.getTags() : task.getTags());
        task.setUpdatedAt(LocalDateTime.now());

        taskRepository.save(task);

        return CustomResponse.builder()
                .status("success")
                .message("Task with id: " + taskID + " updated")
                .build();
    }

    public CustomResponse deleteTask(UUID taskID) {
        User authUser = Utils.getAuthenticatedUser();
        Task task = findTaskByID(taskID, authUser);

        TaskRole role = taskRoleRepository.findByTaskAndUser(task, authUser)
                .orElseThrow();

        boolean hasAuthority = role.getRoleType().equals(RoleType.CREATOR);
        if (!hasAuthority) {
            throw new ForbiddenException("only creator of task can delete task");
        }

        taskRepository.delete(task);
        return CustomResponse.builder()
                .status("success")
                .message("Task with id: " + taskID + " deleted")
                .build();
    }

    private Task findTaskByID(UUID taskID, User authUser) {

        Task task = taskRepository.findById(taskID)
                .orElseThrow(() -> new NotFoundException("Task not found with id: " +  taskID));

        boolean hasRole = taskRoleRepository.existsByTaskIdAndUserId(
                taskID, authUser.getId()
        );

        if (!hasRole) {
            throw new ForbiddenException("You are not authorized to access this resource");
        }

        return task;
    }

    private TaskDto convertToDo(Task task) {
        return  TaskDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .assignedTo(task.getAssignedTo())
                .dueDate(task.getDueDate())
                .tags(task.getTags())
                .priority(task.getPriority())
                .createdBy(task.getCreatedBy().getId())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
