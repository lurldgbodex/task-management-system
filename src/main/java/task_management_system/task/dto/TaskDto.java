package task_management_system.task.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import task_management_system.task.enums.TaskPriority;
import task_management_system.task.enums.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class TaskDto {

    private UUID id;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private TaskStatus status;
    private TaskPriority priority;
    private String assignedTo;
    private List<String> tags;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
