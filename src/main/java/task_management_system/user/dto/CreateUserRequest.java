package task_management_system.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateUserRequest {
    @Email(message = "you need to provide a valid email")
    @NotBlank(message = "email is required")
    private String email;

    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must be at least 8 with one uppercase, lowercase, number and special character."
    )
    @NotBlank(message = "password is required")
    private String password;

    private String username;
}
