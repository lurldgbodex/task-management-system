package task_management_system.task.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import task_management_system.dto.CustomResponse;
import task_management_system.exception.ForbiddenException;
import task_management_system.task.entity.Task;
import task_management_system.task.entity.TaskRole;
import task_management_system.task.enums.RoleType;
import task_management_system.task.repository.TaskRepository;
import task_management_system.task.repository.TaskRoleRepository;
import task_management_system.user.entity.User;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskDeleteService {

    private final TaskRepository taskRepository;
    private final TaskRoleRepository taskRoleRepository;
    private final TaskCacheManager taskCacheManager;

    public CustomResponse deleteTask(UUID taskID, User authUser) {
        Task task = taskCacheManager.getTaskFromCache(taskID);

        TaskRole role = taskRoleRepository.findByTaskAndUser(task, authUser)
                .orElseThrow();

        if (!role.getRoleType().equals(RoleType.CREATOR)) {
            throw new ForbiddenException("only creator of task can delete task");
        }

        taskRepository.delete(task);
        taskCacheManager.evictTaskFromCache(taskID);

        return CustomResponse.builder()
                .status("success")
                .message("Task with id: " + taskID + " deleted")
                .build();
    }
}
