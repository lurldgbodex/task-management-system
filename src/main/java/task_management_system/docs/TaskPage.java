package task_management_system.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import task_management_system.task.dto.TaskDto;

import java.util.List;

@Schema(name = "TaskPage", description = "Pagination tasks response")
public class TaskPage extends PageImpl<TaskDto> {

    public TaskPage(List<TaskDto> content) {
        super(content, PageRequest.of(0, 10), content.size());
    }

    public TaskPage(List<TaskDto> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }
}
