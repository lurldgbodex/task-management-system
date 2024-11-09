package task_management_system.task.controller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import task_management_system.config.JwtService;
import task_management_system.task.service.TaskService;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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

    @Test
    void shouldCreateMockMvc() {
        assertNotNull(mockMvc);
    }

    @Test
    @Disabled("Not implemented yet")
    void createTask() {
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