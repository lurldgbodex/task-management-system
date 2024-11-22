package task_management_system.task.dto;

import lombok.Builder;
import lombok.Data;
import task_management_system.task.enums.TaskPriority;
import task_management_system.task.enums.TaskStatus;
import task_management_system.user.entity.User;

import java.util.Set;

@Data
@Builder
public class GetTasks {
    private TaskStatus status;
    private TaskPriority priority;
    private Set<String> tags;
    private User authUser;
}
