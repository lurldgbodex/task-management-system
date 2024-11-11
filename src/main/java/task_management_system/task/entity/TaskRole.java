package task_management_system.task.entity;

import jakarta.persistence.*;
import lombok.*;
import task_management_system.task.enums.RoleType;
import task_management_system.user.entity.User;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "task_roles",
        uniqueConstraints = {
                @UniqueConstraint(columnNames =
                        { "task_id", "role_type" },
                        name = "unique_task_creator"
                )
        }
)
public class TaskRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    @ManyToOne
    private User user;

    @ManyToOne
    private Task task;
}
