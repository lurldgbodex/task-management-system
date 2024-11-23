package task_management_system.task.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import task_management_system.config.JwtService;
import task_management_system.dto.CustomResponse;
import task_management_system.task.dto.*;
import task_management_system.task.enums.TaskPriority;
import task_management_system.task.enums.TaskStatus;
import task_management_system.task.service.TaskService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @MockBean
    private TaskService taskService;
    @MockBean
    private JwtService jwtService;
    @Autowired
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private TaskDto taskDto;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();

        taskDto = TaskDto.builder()
                .id(UUID.randomUUID())
                .title("Task title")
                .build();
    }

    @Test
    void shouldCreateMockMvc() {
        assertNotNull(mockMvc);
    }

    @Test
    @DisplayName("should create task successfully")
    void createTask() throws Exception {
        CreateTaskRequest createRequest = CreateTaskRequest.builder()
                .title("Test Task Title")
                .description("Test Task description")
                .status(TaskStatus.PENDING.toString())
                .priority(TaskPriority.LOW.toString())
                .assigned_to("john@doe.com")
                .tags(Collections.singleton("Test"))
                .due_date("2024-11-21T14:20:10")
                .build();

        TaskDto taskDto = TaskDto.builder()
                .id(UUID.randomUUID())
                .title(createRequest.getTitle())
                .description(createRequest.getDescription())
                .due_date(LocalDateTime.parse(createRequest.getDue_date()))
                .created_by(UUID.randomUUID())
                .status(TaskStatus.PENDING)
                .priority(TaskPriority.LOW)
                .assigned_to(createRequest.getAssigned_to())
                .tags(createRequest.getTags())
                .created_at(LocalDateTime.now())
                .updated_at(LocalDateTime.now())
                .build();
        ;
        String request = objectMapper.writeValueAsString(createRequest);

        when(taskService.createTask(createRequest)).thenReturn(taskDto);

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value(createRequest.getTitle()))
                .andExpect(jsonPath("$.description").value(createRequest.getDescription()))
                .andExpect(jsonPath("$.due_date").value(createRequest.getDue_date()))
                .andExpect(jsonPath("$.status").value(createRequest.getStatus()))
                .andExpect(jsonPath("$.priority").value(createRequest.getPriority()))
                .andExpect(jsonPath("$.assigned_to").value(createRequest.getAssigned_to()))
                .andExpect(jsonPath("$.created_by").exists())
                .andExpect(jsonPath("$.created_at").isNotEmpty())
                .andExpect(jsonPath("$.updated_at").isNotEmpty());
    }
    @Test
    @DisplayName("should get all tasks")
    void getAllTasks() throws Exception {

        Pageable pageable = PageRequest.of(0, 20);

        Page<TaskDto> pageDto = new PageImpl<>(List.of(taskDto), pageable, 1);
        PaginatedResponse<TaskDto> response = new PaginatedResponse<>(pageDto);

        when(taskService.getTasks(pageable, null, null, null)).thenReturn(response);

        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.content").isArray());
    }
    @Test
    @DisplayName("should get task by ID")
    void getTaskByID() throws Exception {
        when(taskService.getTaskByID(taskDto.getId())).thenReturn(taskDto);

        mockMvc.perform(get("/api/v1/tasks/" + taskDto.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value(taskDto.getTitle()));
    }

    @Test
    @DisplayName("should update task")
    void updateTask() throws Exception {
        UpdateTask request = UpdateTask.builder().build();
        CustomResponse response = new CustomResponse("success", "updated");

        String updateRequest = objectMapper.writeValueAsString(request);
        when(taskService.updateTask(taskDto.getId(), request)).thenReturn(response);

        mockMvc.perform(put("/api/v1/tasks/" + taskDto.getId())
                        .content(updateRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("should delete task")
    void shouldDeleteTask() throws Exception {
        CustomResponse response = new CustomResponse("success", "deleted");
        when(taskService.deleteTask(taskDto.getId())).thenReturn(response);

        mockMvc.perform(delete("/api/v1/tasks/" + taskDto.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(response.status()))
                .andExpect(jsonPath("$.message").value(response.message()));
    }

    @Test
    @DisplayName("should share task")
    void shouldShareTask() throws Exception {
        CustomResponse res = new CustomResponse("success", "shared");
        ShareRequest request = ShareRequest.builder()
                .email("test@email.com")
                .build();

        when(taskService.shareTask(request, taskDto.getId())).thenReturn(res);

        String shareRequest = objectMapper.writeValueAsString(request);
        mockMvc.perform(put("/api/v1/tasks/" + taskDto.getId() + "/share")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(shareRequest))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(res.status()))
                .andExpect(jsonPath("$.message").value(res.message()));
    }
}