package task_management_system.dto;

import lombok.Builder;

@Builder
public record CustomResponse(String status, String message) {
}
