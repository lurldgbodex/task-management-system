package task_management_system.task.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import task_management_system.dto.ValidationException;
import task_management_system.task.dto.*;
import task_management_system.dto.CustomResponse;
import task_management_system.task.enums.TaskPriority;
import task_management_system.task.enums.TaskStatus;
import task_management_system.task.service.TaskService;
import task_management_system.utils.TaskUtils;

import java.util.Set;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tasks")
@Tag(name = "Task Controller", description = "Operations for managing tasks")
public class TaskController {

    private final TaskService taskService;

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201", description = "Task created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400", description = "Invalid task request data",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ValidationException.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401", description = "Unauthenticated user trying to create task",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomResponse.class)
                    )
            ),
    })
    @Operation(
            summary = "Create a new task",
            description = "Creates a new task for a user"
    )
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<TaskDto> createTask(
            @Parameter(description = "Task data to be created") @RequestBody @Valid CreateTaskRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(taskService.createTask(request));
    }


    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Page of tasks",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PaginatedResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400", description = "Invalid pagination parameters",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401", description = "unauthenticated user trying to access task",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomResponse.class)
                    )
            ),
    })
    @Operation(
            summary = "Get all tasks with pagination",
            description = "Retrieves a paginated list of task with a default page size of 10."
    )
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = "application/json")
    public PaginatedResponse<TaskDto> getAllTasks(
            @Parameter(description = "Page number (starting from 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default is 20)") @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "filter by status") @RequestParam(required = false) String status,
            @Parameter(description = "filter by priority") @RequestParam(required = false) String priority,
            @Parameter(description = "filter by tag") @RequestParam(required = false) Set<String> tags
    ) {
        TaskStatus taskStatus = TaskUtils.parseEnum(TaskStatus.class, status);
        TaskPriority taskPriority = TaskUtils.parseEnum(TaskPriority.class, priority);

        Pageable pageable = PageRequest.of(page, limit);
        return taskService.getTasks(pageable, taskStatus, taskPriority, tags);
    }


    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Task retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404", description = "Task not found using the provided id",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401", description = "unauthenticated user trying to access task",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403", description = "Authenticated user trying to access resource they are not authorized",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomResponse.class)
                    )
            )
    })
    @Operation(
            summary = "Get a task by its ID",
            description = "Fetches a specific task by its unique ID."
    )
    @GetMapping(value = "/{taskID}", produces = "application/json")
    public ResponseEntity<TaskDto> getTaskByID(
            @Parameter(description = "Unique ID of task to retrieve") @PathVariable UUID taskID) {
        return ResponseEntity.ok(taskService.getTaskByID(taskID));
    }


    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Task update successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400", description = "Invalid task data",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ValidationException.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404", description = "Task not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401", description = "unauthenticated user trying to update task",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403", description = "Authenticated user trying to update resource they are not authorized",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomResponse.class)
                    )
            )
    })
    @Operation(
            summary = "Update an existing task",
            description = "Update a task's details for the given task ID"
    )
    @PutMapping(value = "/{taskID}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<CustomResponse> updateTask(
            @Parameter(description = "Unique ID of the task") @PathVariable UUID taskID,
            @Parameter(description = "Update task data") @RequestBody @Valid UpdateTask updateTask) {
        return ResponseEntity.ok(taskService.updateTask(taskID, updateTask));
    }


    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Task shared successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400", description = "Invalid task data",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ValidationException.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404", description = "Task or user not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401", description = "unauthenticated user trying to update task",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403", description = "Unauthorized user trying to share task",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomResponse.class)
                    )
            )
    })
    @Operation(
            summary = "Share an existing task to a user",
            description = "Share a task with another user using the user email address"
    )
    @PutMapping(value = "/{taskID}/share", consumes = "application/json", produces = "application/json")
    public ResponseEntity<CustomResponse> shareTask(
            @Parameter(description = "Unique ID of the task") @PathVariable UUID taskID,
            @Parameter(description = "Request body of task share") @RequestBody @Valid ShareRequest request) {

        return ResponseEntity.ok(taskService.shareTask(request, taskID));
    }


    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Task deleted",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404", description = "Task not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401", description = "unauthenticated user trying to delete task",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403", description = "Authenticated user trying to access resource they are not authorized",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomResponse.class)
                    )
            )
    })
    @Operation(
            summary = "Delete a task",
            description = "Deletes the task with the specified ID"
    )
    @DeleteMapping(value = "/{taskID}", produces = "application/json")
    public ResponseEntity<CustomResponse> deleteTask(
            @Parameter(description = "Unique ID of the task") @PathVariable UUID taskID) {
        return ResponseEntity.ok(taskService.deleteTask(taskID));
    }
}
