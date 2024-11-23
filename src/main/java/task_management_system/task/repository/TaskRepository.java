package task_management_system.task.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import task_management_system.task.entity.Task;

import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

    @Query("SELECT t FROM Task t JOIN t.taskRoles tr WHERE tr.user.id = :userID")
    Page<Task> findTasksByUserRoles(@Param("userID") UUID userID, Pageable pageable);
}
