package task_management_system.task.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShareRequest {
    @NotBlank(message = "email is required")
    @Email(message = "invalid email provided")
    private String email;

    private boolean can_edit;
}
