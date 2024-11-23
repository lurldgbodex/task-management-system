package task_management_system.task.dto;

import lombok.Builder;
import lombok.Data;
import task_management_system.task.enums.TaskPriority;
import task_management_system.task.enums.TaskStatus;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class TaskDto {

    private UUID id;
    private String title;
    private String description;
    private LocalDateTime due_date;
    private TaskStatus status;
    private TaskPriority priority;
    private String assigned_to;
    private Set<String> tags;
    private UUID created_by;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}
