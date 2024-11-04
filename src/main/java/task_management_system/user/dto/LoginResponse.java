package task_management_system.user.dto;

import lombok.Builder;

@Builder
public record LoginResponse(
        TokenDto accessToken,
        UserDto user
) {}
