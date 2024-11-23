package task_management_system.task.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import task_management_system.task.enums.TaskPriority;
import task_management_system.task.enums.TaskStatus;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTask {
    private String title;
    private String description;
    private LocalDateTime due_date;
    private String status;
    private String priority;

    @Email(message = "invalid email provided")
    private String assigned_to;
    private Set<String> tags;
}
