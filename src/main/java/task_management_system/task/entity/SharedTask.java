package task_management_system.task.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import task_management_system.user.entity.User;

@Getter
@Setter
@Entity
public class SharedTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column
    private boolean canEdit = false;
}
