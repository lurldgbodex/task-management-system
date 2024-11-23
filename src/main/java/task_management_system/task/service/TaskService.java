package task_management_system.task.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import task_management_system.exception.ForbiddenException;
import task_management_system.exception.NotFoundException;
import task_management_system.task.dto.*;
import task_management_system.dto.CustomResponse;
import task_management_system.task.entity.Task;
import task_management_system.task.enums.TaskPriority;
import task_management_system.task.enums.TaskStatus;
import task_management_system.task.repository.TaskRepository;
import task_management_system.task.repository.TaskRoleRepository;
import task_management_system.user.entity.User;
import task_management_system.utils.TaskUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskCreationService taskCreationService;
    private final TaskUpdateService taskUpdateService;
    private final TaskQueryService taskQueryService;
    private final TaskDeleteService taskDeleteService;
    private final TaskShareService taskShareService;
    private final TaskRoleRepository taskRoleRepository;
    private final TaskRepository taskRepository;

    public TaskDto createTask(CreateTaskRequest taskRequest) {
        User authUser = TaskUtils.getAuthenticatedUser();
        Task task = taskCreationService.createTask(authUser, taskRequest);

        return TaskUtils.convertToDto(task);
    }

    public TaskDto getTaskByID(UUID taskID) {
        User authUser = TaskUtils.getAuthenticatedUser();

        validateTaskExists(taskID);
        hasRoleOnTask(taskID, authUser.getId());

        Task task = taskQueryService.getTaskByID(taskID);
        return TaskUtils.convertToDto(task);
    }

    public PaginatedResponse<TaskDto> getTasks(Pageable pageable, TaskStatus status,
                                               TaskPriority priority, Set<String> tags) {

        User authUser = TaskUtils.getAuthenticatedUser();

        GetTasks request = GetTasks.builder()
                .authUser(authUser)
                .priority(priority)
                .status(status)
                .tags(tags)
                .build();

        Page<TaskDto> taskDto = taskQueryService
                .getTasks(pageable, request)
                .map(TaskUtils::convertToDto);

        return new PaginatedResponse<>(taskDto);
    }

    public CustomResponse updateTask(UUID taskID, UpdateTask request) {
        User authUser = TaskUtils.getAuthenticatedUser();

        return taskUpdateService.updateTask(taskID, request, authUser);
    }

    public CustomResponse shareTask(ShareRequest request, UUID taskID) {
        User authUser = TaskUtils.getAuthenticatedUser();

        hasRoleOnTask(taskID, authUser.getId());
        return taskShareService.shareTask(request, taskID);
    }

    public CustomResponse deleteTask(UUID taskID) {
        User authUser = TaskUtils.getAuthenticatedUser();
        return taskDeleteService.deleteTask(taskID, authUser);
    }

    private void validateTaskExists(UUID taskID) {
        boolean exists = taskRepository.existsById(taskID);
        if (!exists) {
            throw new NotFoundException("Task not found with id: " + taskID);
        }
    }
    private void hasRoleOnTask(UUID taskID, UUID userID) {

        boolean hasRole = taskRoleRepository.existsByTaskIdAndUserId(taskID, userID);

        if (!hasRole) {
            throw new ForbiddenException("Unauthorized access");
        }
    }
}
