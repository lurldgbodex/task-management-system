package task_management_system.task.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import task_management_system.task.entity.Task;
import task_management_system.task.entity.TaskRole;
import task_management_system.task.enums.RoleType;
import task_management_system.user.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface TaskRoleRepository extends JpaRepository<TaskRole, Long> {
    boolean existsByTaskIdAndUserId(UUID taskID, UUID userID);
    Optional<TaskRole> findByTaskAndUser(Task task, User user);
    boolean existsByTaskAndUserAndRoleType(Task task, User user, RoleType roleType);
}
