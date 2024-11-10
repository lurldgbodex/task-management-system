package task_management_system.task.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import task_management_system.task.enums.TaskPriority;
import task_management_system.task.enums.TaskStatus;

import java.util.List;

@Data
@Builder
public class CreateTaskRequest {

    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "description is required")
    private String description;

    @NotBlank(message = "dueDate is required")
    @Pattern(
            regexp = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}",
            message = "dueDate must be in the format yyyy-MM-ddTHH:mm:ss"
    )
    private String dueDate;

    @NotNull(message = "status is required")
    private TaskStatus status;

    private TaskPriority priority;

    @Email(message = "assignedTo must be a valid email")
    private String assignedTo;

    private List<String> tags;
}
