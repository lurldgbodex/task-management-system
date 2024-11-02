package task_management_system.task.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import task_management_system.task.entity.Task;

import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

}
