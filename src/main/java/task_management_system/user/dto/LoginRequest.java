package task_management_system.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginRequest {

        @NotBlank(message = "email is required for login")
        private String email;

        @NotBlank(message = "password is required for login")
        private String password;
}
