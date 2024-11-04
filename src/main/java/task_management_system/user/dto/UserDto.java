package task_management_system.user.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record UserDto(
        UUID userId,
        String email,
        String username,
        boolean enabled,
        boolean accountLocked,
        boolean accountExpired
) {}
