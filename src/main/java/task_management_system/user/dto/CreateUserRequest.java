package task_management_system.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateUserRequest {
    @NotBlank(message = "email is required")
    @Email(message = "you need to provide a valid email")
    private String email;

    @NotBlank(message = "password is required")
    private String password;

    private String username;
}
