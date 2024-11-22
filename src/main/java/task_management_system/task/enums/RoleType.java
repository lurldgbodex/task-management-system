package task_management_system.task.enums;


import java.util.Set;

public enum RoleType {
    CREATOR(Set.of(UpdateOperation.TITLE, UpdateOperation.DESCRIPTION,
            UpdateOperation.DUE_DATE, UpdateOperation.ASSIGNEE, UpdateOperation.TAGS,
            UpdateOperation.STATUS, UpdateOperation.PRIORITY)),
    ASSIGNEE(Set.of(UpdateOperation.STATUS, UpdateOperation.TAGS)),
    SHARED(Set.of());

    private final Set<UpdateOperation> allowedOperations;

    RoleType(Set<UpdateOperation> allowedOperations) {
        this.allowedOperations = allowedOperations;
    }

    public boolean canPerform(UpdateOperation operation) {
        return allowedOperations.contains(operation);
    }
}
