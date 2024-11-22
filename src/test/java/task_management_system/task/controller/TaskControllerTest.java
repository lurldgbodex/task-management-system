package task_management_system.task.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import task_management_system.config.JwtService;
import task_management_system.task.dto.CreateTaskRequest;
import task_management_system.task.dto.TaskDto;
import task_management_system.task.enums.TaskPriority;
import task_management_system.task.enums.TaskStatus;
import task_management_system.task.service.TaskService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @MockBean
    private TaskService taskService;
    @MockBean
    private JwtService jwtService;
    @Autowired
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldCreateMockMvc() {
        assertNotNull(mockMvc);
    }

    @Nested
    @DisplayName("Create Task Controller Tests")
    class CreateTaskTests {

        private CreateTaskRequest createRequest;
        private TaskDto taskDto;

        @BeforeEach
        void setUpCreateTask() {
            createRequest = CreateTaskRequest.builder()
                    .title("Test Task Title")
                    .description("Test Task description")
                    .status(TaskStatus.PENDING.toString())
                    .priority(TaskPriority.LOW.toString())
                    .assigned_to("john@doe.com")
                    .tags(Collections.singleton("Test"))
                    .due_date("2024-11-21T14:20:10")
                    .build();

            taskDto = TaskDto.builder()
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
        }

        @Test
        @WithMockUser
        @DisplayName("should create task successfully")
        void createTask() throws Exception {
            String request = objectMapper.writeValueAsString(createRequest);

            when(taskService.createTask(createRequest)).thenReturn(taskDto);

            mockMvc.perform(post("/api/v1/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title").value(createRequest.getTitle()))
                    .andExpect(jsonPath("$.description").value(createRequest.getDescription()))
                    .andExpect(jsonPath("$.dueDate").value(createRequest.getDue_date()))
                    .andExpect(jsonPath("$.status").value(createRequest.getStatus()))
                    .andExpect(jsonPath("$.priority").value(createRequest.getPriority()))
                    .andExpect(jsonPath("$.assignedTo").value(createRequest.getAssigned_to()))
                    .andExpect(jsonPath("$.createdBy").exists())
                    .andExpect(jsonPath("$.createdAt").isNotEmpty())
                    .andExpect(jsonPath("$.updatedAt").isNotEmpty());
        }

        @Test
        @WithMockUser
        @Disabled("Not implemented")
        void createTaskBadRequest() {
        }

        @Test
        @Disabled("Not implemented")
        void createTaskUnauthenticated() {
        }
    }

    @Test
    @Disabled("Not implemented")
    void getAllTasks() {
    }

    @Test
    @Disabled("Not implemented")
    void getTaskByID() {
    }

    @Test
    @Disabled("Not implemented")
    void updateTask() {
    }

    @Test
    @Disabled("Not implemented")
    void deleteTask() {
    }
}