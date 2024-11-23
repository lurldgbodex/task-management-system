package task_management_system.task.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import task_management_system.dto.CustomResponse;
import task_management_system.task.dto.ShareRequest;
import task_management_system.task.entity.SharedTask;
import task_management_system.task.entity.Task;
import task_management_system.task.enums.RoleType;
import task_management_system.task.repository.SharedTaskRepository;
import task_management_system.user.entity.User;
import task_management_system.utils.TaskUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskShareService {

    private final TaskUtils taskUtils;
    private final SharedTaskRepository sharedTaskRepository;
    private final TaskCacheManager taskCacheManager;

    public CustomResponse shareTask(ShareRequest request, UUID taskID) {
        Task task = taskCacheManager.getTaskFromCache(taskID);

        User sharedToUser = taskUtils.assignRole(request.getEmail(), task, RoleType.SHARED);

        SharedTask sharedTask = new SharedTask();
        sharedTask.setTask(task);
        sharedTask.setUser(sharedToUser);
        sharedTask.setCanEdit(request.isCan_edit());

        sharedTaskRepository.save(sharedTask);

        return CustomResponse.builder()
                .status("success")
                .message("Task shared with " + request.getEmail())
                .build();
    }
}
