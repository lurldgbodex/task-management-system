package task_management_system.utils;

import task_management_system.exception.BadRequestException;
import task_management_system.exception.ForbiddenException;
import task_management_system.task.enums.RoleType;
import task_management_system.task.enums.UpdateOperation;

import java.time.LocalDateTime;

public class Validator {

    public static void validateUpdatePermission(RoleType roleType, UpdateOperation operation) {
        if (!roleType.canPerform(operation)) {
            throw new ForbiddenException("Role " + roleType + " is not authorized to perform " + operation + " operation.");
        }
    }

    public static void validateDueDate(LocalDateTime dueDate) {
        if (dueDate.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Due date must be in the future to be valid");
        }
    }
}
