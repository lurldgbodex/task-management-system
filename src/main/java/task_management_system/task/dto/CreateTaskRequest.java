package task_management_system.task.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

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
    private String due_date;

    @NotBlank(message = "status is required")
    private String status;

    @NotBlank(message = "priority is required")
    private String priority;

    @Email(message = "invalid email provided")
    private String assigned_to;

    private Set<String> tags;
}
