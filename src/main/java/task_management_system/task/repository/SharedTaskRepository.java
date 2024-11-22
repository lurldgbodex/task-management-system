package task_management_system.task.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import task_management_system.task.entity.SharedTask;

public interface SharedTaskRepository extends JpaRepository<SharedTask, Long> {
}
