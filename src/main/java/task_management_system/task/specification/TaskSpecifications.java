package task_management_system.task.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import task_management_system.task.entity.Task;
import task_management_system.task.enums.TaskPriority;
import task_management_system.task.enums.TaskStatus;
import task_management_system.user.entity.User;

import java.util.Set;

public class TaskSpecifications {

    public static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, criteriaBuilder) -> status == null
                ? null
                : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Task> hasPriority(TaskPriority priority) {
        return (root, query, criteriaBuilder) -> priority == null
                ? null
                : criteriaBuilder.equal(root.get("priority"), priority);
    }

    public static Specification<Task> hasTags(Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }

        return (root, query, criteriaBuilder) -> {
            Predicate tagPredicate = criteriaBuilder.disjunction();
            for (String tag : tags) {
                tagPredicate = criteriaBuilder.or(tagPredicate,
                        criteriaBuilder.isMember(tag, root.get("tags"))
                );
            }
          return tagPredicate;
        };
    }

    public static Specification<Task> hasRoleOnTask(User user) {
        return (root, query, criteriaBuilder) -> {
            if (user == null || user.getId() == null) {
                return criteriaBuilder.disjunction();
            }

            Join<Object, Object> taskRolesJoin = root.join("taskRoles", JoinType.LEFT);

            return criteriaBuilder.equal(taskRolesJoin.get("user").get("id"), user.getId());
        };
    }

}
