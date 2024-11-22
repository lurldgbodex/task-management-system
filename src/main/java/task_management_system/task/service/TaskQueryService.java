package task_management_system.task.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import task_management_system.task.dto.GetTasks;
import task_management_system.task.entity.Task;
import task_management_system.task.repository.TaskRepository;
import task_management_system.task.specification.TaskSpecifications;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskQueryService {

    private final TaskRepository taskRepository;
    private final TaskCacheManager taskCacheManager;

    public Task getTaskByID(UUID taskID) {
        return taskCacheManager.getTaskFromCache(taskID);
    }

    public Page<Task> getTasks(Pageable pageable, GetTasks request) {

        Specification<Task> spec = Specification
                .where(TaskSpecifications.hasStatus(request.getStatus()))
                .and(TaskSpecifications.hasPriority(request.getPriority()))
                .and(TaskSpecifications.hasTags(request.getTags()))
                .and(TaskSpecifications.hasRoleOnTask(request.getAuthUser()));

        return taskRepository.findAll(spec, pageable);
    }
}
