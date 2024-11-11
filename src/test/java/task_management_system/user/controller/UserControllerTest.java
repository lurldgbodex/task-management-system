package task_management_system.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import task_management_system.config.JwtService;
import task_management_system.user.dto.CreateUserRequest;
import task_management_system.user.dto.LoginRequest;
import task_management_system.user.dto.LoginResponse;
import task_management_system.user.dto.UserDto;
import task_management_system.user.service.UserService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private UserService userService;
    @MockBean private JwtService jwtService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldCreateMockMvc() {
        assertNotNull(mockMvc, "should create mock mvc");
    }

    @Nested
    @DisplayName("User Registration Controller Test")
    class RegisterUserTest {

        private CreateUserRequest userRequest;

        @BeforeEach
        void setUpRegisterUser() {
            userRequest = CreateUserRequest.builder().build();
        }

        @Test
        @DisplayName("should not register user when no data is provided")
        void registerUserWithNoData() throws Exception {
            String requestString = objectMapper.writeValueAsString(userRequest);

            mockMvc.perform(post("/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestString))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.errors.email").value("email is required"))
                    .andExpect(jsonPath("$.errors.password").value("password is required"));
        }

        @Test
        @DisplayName("should not register user when invalid email is provided")
        void registerUserWithInvalidEmail() throws Exception {
            userRequest.setEmail("invalid-email");
            userRequest.setPassword("P@sswarD-cu3d");
            String requestString = objectMapper.writeValueAsString(userRequest);

            mockMvc.perform(post("/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestString))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.errors.email").value("you need to provide a valid email"));
        }

        @Test
        @DisplayName("should not register user when invalid password is provided")
        void registerUserWithInvalidPassword() throws Exception {
            userRequest.setEmail("user@email.com");
            userRequest.setPassword("invalid-password");

            String requestString = objectMapper.writeValueAsString(userRequest);

            mockMvc.perform(post("/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestString))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.errors.password").value(
                            "Password must be at least 8 with one uppercase, lowercase, number and special character."));
        }

        @Test
        @DisplayName("should register user successfully")
        void registerData() throws Exception {

           userRequest.setEmail("johndoe@unknown.com");
           userRequest.setPassword("p@ssUs3r93t");
           userRequest.setUsername("username");

            UserDto response = UserDto.builder()
                    .userID(UUID.randomUUID())
                    .email(userRequest.getEmail())
                    .username(userRequest.getUsername())
                    .enabled(true)
                    .accountLocked(false)
                    .accountExpired(false)
                    .build();

            when(userService.registerUser(userRequest)).thenReturn(response);

            String requestString = objectMapper.writeValueAsString(userRequest);

            mockMvc.perform(post("/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestString))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.email").value(response.email()))
                    .andExpect(jsonPath("$.username").value(response.username()))
                    .andExpect(jsonPath("$.userID").value(response.userID().toString()))
                    .andExpect(jsonPath("$.enabled").value(response.enabled()))
                    .andExpect(jsonPath("$.accountLocked").value(response.accountLocked()))
                    .andExpect(jsonPath("$.accountExpired").value(response.accountExpired()));
        }
    }

    @Nested
    @DisplayName("User Authentication Controller Test")
    class LoginUserTest {

        private LoginRequest loginRequest;

        @BeforeEach
        void setupLoginUser() {
            loginRequest = LoginRequest.builder()
                    .email("user@email.com")
                    .password("P@ssw0rd222")
                    .build();
        }

        @Test
        @DisplayName("should successfully login user")
        void shouldLoginUser() throws Exception {

            LoginResponse response = LoginResponse.builder()
                    .accessToken("dummy-access-token")
                    .build();

            when(userService.authenticate(loginRequest)).thenReturn(response);

            String requestString = objectMapper.writeValueAsString(loginRequest);

            mockMvc.perform(post("/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestString))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.accessToken").value(response.accessToken()));
        }

        @Test
        @DisplayName("should not process request with invalid data")
        void loginUserWithNoData() throws Exception {
            loginRequest = LoginRequest.builder().build();

            String requestString = objectMapper.writeValueAsString(loginRequest);

            mockMvc.perform(post("/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestString))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.errors.email").value("email is required for login"))
                    .andExpect(jsonPath("$.errors.password").value("password is required for login"));
        }

        @Test
        @DisplayName("should not authenticate request with invalid credential")
        void loginUserWithInvalidCredential() throws Exception {
            String requestString = objectMapper.writeValueAsString(loginRequest);

            when(userService.authenticate(loginRequest)).thenThrow(BadCredentialsException.class);

            mockMvc.perform(post("/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestString))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value("401"))
                    .andExpect(jsonPath("$.message").value("Invalid authentication credentials"));
        }
    }
}