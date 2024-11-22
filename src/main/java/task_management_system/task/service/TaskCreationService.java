package task_management_system.task.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import task_management_system.task.dto.CreateTaskRequest;
import task_management_system.task.entity.Task;
import task_management_system.task.enums.RoleType;
import task_management_system.task.enums.TaskPriority;
import task_management_system.task.enums.TaskStatus;
import task_management_system.task.repository.TaskRepository;
import task_management_system.user.entity.User;
import task_management_system.utils.TaskUtils;
import task_management_system.utils.Validator;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TaskCreationService {

    private final TaskUtils taskUtils;
    private final TaskRepository taskRepository;
    private final TaskCacheManager taskCacheManager;

    public Task createTask(User authUser, CreateTaskRequest request) {
        LocalDateTime dueDate = TaskUtils.parseDateTime(request.getDue_date());
        TaskStatus status = TaskUtils.parseEnum(TaskStatus.class, request.getStatus());
        TaskPriority priority = TaskUtils.parseEnum(TaskPriority.class, request.getPriority());

        Validator.validateDueDate(dueDate);

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(dueDate)
                .status(status)
                .createdBy(authUser)
                .assignedTo(request.getAssigned_to())
                .priority(priority)
                .tags(request.getTags())
                .build();

        taskRepository.saveAndFlush(task);
        taskUtils.assignRole(request.getAssigned_to(), task, RoleType.ASSIGNEE);

        taskCacheManager.addTaskToCache(task);
        return task;
    }
}

