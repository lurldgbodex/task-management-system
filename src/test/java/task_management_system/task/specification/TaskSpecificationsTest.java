package task_management_system.task.specification;

import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import task_management_system.task.entity.Task;
import task_management_system.task.enums.TaskPriority;
import task_management_system.task.enums.TaskStatus;
import task_management_system.user.entity.User;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskSpecificationsTest {

    @Mock private CriteriaBuilder builder;
    @Mock private CriteriaQuery<Task> query;
    @Mock private Root<Task> root;
    @Mock private Predicate predicate;
    @Mock private Join<Object, Object> taskRolesJoin;

    @Test
    void testHasStatusSpecification() {
        Specification<Task> spec = TaskSpecifications.hasStatus(TaskStatus.COMPLETED);

        Path<TaskStatus> path = mock(Path.class);
        when(root.get("status")).thenReturn((Path) path);
        when(builder.equal(path, TaskStatus.COMPLETED)).thenReturn(predicate);

        Predicate response = spec.toPredicate(root, query, builder);

        assertNotNull(response);
        verify(root, times(1)).get("status");
        verify(builder, times(1)).equal(path, TaskStatus.COMPLETED);
    }

    @Test
    void testHasPrioritySpecification() {
        Specification<Task> spec = TaskSpecifications.hasPriority(TaskPriority.LOW);

        Path<TaskPriority> path = mock(Path.class);
        when(root.get("priority")).thenReturn((Path) path);
        when(builder.equal(path, TaskPriority.LOW)).thenReturn(predicate);

        Predicate response = spec.toPredicate(root, query, builder);

        assertNotNull(response);
        verify(root, times(1)).get("priority");
        verify(builder, times(1)).equal(path, TaskPriority.LOW);
    }

    @Test
    void testHasTags() {
        Set<String> tags = Set.of("tag1", "tag2");
        Specification<Task> spec = TaskSpecifications.hasTags(tags);

        Path<String> tagPath = mock(Path.class);
        when(root.get("tags")).thenReturn((Path) tagPath);

        Predicate tagPredicate1 = mock(Predicate.class);
        Predicate tagPredicate2 = mock(Predicate.class);
        Predicate disjunction = mock(Predicate.class);

        when(builder.disjunction()).thenReturn(disjunction);

        when(builder.isMember("tag1", (Path) tagPath)).thenReturn(tagPredicate1);
        when(builder.isMember("tag2", (Path) tagPath)).thenReturn(tagPredicate2);

        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            return mock(Predicate.class); // Return a new Predicate for each OR operation
        }).when(builder).or(any(Predicate.class), any(Predicate.class));

        Predicate response = spec.toPredicate(root, query, builder);

        assertNotNull(response, "Predicate should not be null for valid tags");
        verify(builder, times(1)).disjunction();
    }

    @Test
    void testHasRoleOnTaskSpecification() {
        User user = new User();
        user.setId(UUID.randomUUID());
        Specification<Task> spec = TaskSpecifications.hasRoleOnTask(user);

        when(root.join("taskRoles", JoinType.LEFT)).thenReturn(taskRolesJoin);

        Path<UUID> userIdPath = mock(Path.class);
        when(taskRolesJoin.get("user")).thenReturn(mock(Path.class));
        when(taskRolesJoin.get("user").get("id")).thenReturn((Path) userIdPath);

        Predicate userPredicate = mock(Predicate.class);
        when(builder.equal(userIdPath, user.getId())).thenReturn(userPredicate);

        Predicate result = spec.toPredicate(root, query, builder);

        assertNotNull(result, "Predicate should not be null for a valid user");
        verify(root, times(1)).join("taskRoles", JoinType.LEFT);
        verify(builder, times(1)).equal(userIdPath, user.getId());
    }
}