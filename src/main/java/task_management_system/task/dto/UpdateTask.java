package task_management_system.task.dto;

import lombok.Builder;
import lombok.Data;
import task_management_system.task.enums.TaskPriority;
import task_management_system.task.enums.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UpdateTask {
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private TaskStatus status;
    private TaskPriority priority;
    private String assignedTo;
    private List<String> tags;
}
