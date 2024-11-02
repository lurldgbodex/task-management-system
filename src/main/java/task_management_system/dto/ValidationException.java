package task_management_system.dto;

import lombok.Builder;
import java.util.List;

public record ValidationException(List<ValidationField> errors) {
    @Builder
    public record ValidationField (String field, String message) {}
}
