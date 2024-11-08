package task_management_system.user.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserDto(
        UUID userID,
        String email,
        String username,
        boolean enabled,
        boolean accountLocked,
        boolean accountExpired
) {}
