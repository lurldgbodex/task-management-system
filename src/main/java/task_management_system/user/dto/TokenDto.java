package task_management_system.user.dto;

import lombok.Builder;

@Builder
public record TokenDto(String token, long expiresIn) {
}
