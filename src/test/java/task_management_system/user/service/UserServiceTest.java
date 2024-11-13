package task_management_system.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import task_management_system.config.JwtService;
import task_management_system.exception.ConflictException;
import task_management_system.exception.UnauthorizedException;
import task_management_system.user.dto.CreateUserRequest;
import task_management_system.user.dto.LoginRequest;
import task_management_system.user.dto.LoginResponse;
import task_management_system.user.dto.UserDto;
import task_management_system.user.entity.User;
import task_management_system.user.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authManager;
    @InjectMocks private UserService underTest;

    private User user;

    @BeforeEach
    void setup() {
        user = User.builder()
                .id(UUID.randomUUID())
                .password("encoded-password")
                .email("john@doe.com")
                .username("john-doe")
                .build();
    }

    @Nested
    @DisplayName("User Registration Tests")
    class RegisterUserTest {

        private CreateUserRequest request;
        private User registerUser;

        @BeforeEach
        void setUpRegisterUser() {
            request = CreateUserRequest.builder()
                    .password("password")
                    .email("register@user.com")
                    .build();

            registerUser = User.builder()
                    .id(UUID.randomUUID())
                    .email(request.getEmail())
                    .password("encoded-password")
                    .build();
        }

        @Test
        @DisplayName("should register user successfully Without username field")
        void registerUser() {
            when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
            when(userRepository.saveAndFlush(any(User.class))).then(invocation -> registerUser);

            UserDto response = underTest.registerUser(request);

            ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);

            verify(userRepository).saveAndFlush(userArgumentCaptor.capture());
            User capturedUser = userArgumentCaptor.getValue();

            assertEquals(capturedUser.getId(), response.userID(), "Expects response userID to be same as captured UserID");
            assertEquals(capturedUser.getEmail(), response.email(), "Expects captured email to be same as response email");
            assertNull(capturedUser.getName(), "Expects no username in the captured data as it's no provided");
            assertEquals(capturedUser.getPassword(), "encoded-password", "Expects captured password to be encoded");

            // validate the response
            assertEquals(response.email(), registerUser.getEmail(), "Expects response email to be same as register email");
            assertNull(response.username(), "Expects response username to be null as it's not provided");
            assertTrue(response.enabled(), "Expects user to be enabled");
            assertFalse(response.accountExpired(), "Expects user account to not be expired");
            assertFalse(response.accountLocked(), "Expects user account not to be locked");
        }

        @Test
        @DisplayName("should register user with username")
        void registerUserWithUsername() {
            request.setUsername("john-doe");
            registerUser.setUsername("john-doe");

            when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
            when(userRepository.saveAndFlush(any(User.class))).then(invocation -> registerUser);

            UserDto response = underTest.registerUser(request);

            ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);

            verify(userRepository).saveAndFlush(userArgumentCaptor.capture());
            User capturedUser = userArgumentCaptor.getValue();

            assertEquals(capturedUser.getId(), response.userID(), "Expects response userID to be same as captured UserID");
            assertEquals(capturedUser.getEmail(), response.email(), "Expects captured email to be same as response email");
            assertEquals(capturedUser.getName(), response.username(), "Expects no username in the captured data as it's no provided");
            assertEquals(capturedUser.getPassword(), "encoded-password", "Expects captured password to be encoded");

            // validate the response
            assertEquals(response.email(), registerUser.getEmail(), "Expects response email to be same as register email");
            assertEquals(response.username(),  "john-doe", "Expects response username to be null as it's not provided");
            assertTrue(response.enabled(), "Expects user to be enabled");
            assertFalse(response.accountExpired(), "Expects user account to not be expired");
            assertFalse(response.accountLocked(), "Expects user account not to be locked");
        }

        @Test
        @DisplayName("should throw an exception when username already exist")
        void registerUserWithExistingUsername() {
            request.setUsername("username");
            registerUser.setUsername("username");

            when(userRepository.findByUsername(request.getUsername()))
                    .thenReturn(Optional.of(registerUser));

            Exception ex = assertThrows(ConflictException.class, () ->
                    underTest.registerUser(request));

            assertEquals("user with username already exists", ex.getMessage());
            verify(userRepository, never()).saveAndFlush(any(User.class));
        }

        @Test
        @DisplayName("should throw an exception when email already exist")
        void registerUserWithExistingEmail() {
            when(userRepository.findByEmail(request.getEmail()))
                    .thenReturn(Optional.of(registerUser));

            Exception ex = assertThrows(ConflictException.class, () ->
                    underTest.registerUser(request));

            assertEquals("user with email already exists", ex.getMessage());
            verify(userRepository, never()).saveAndFlush(any(User.class));
        }
    }

    @Nested
    @DisplayName("User Authentication Tests")
    class LoginUserTest {

        private LoginRequest request;

        @BeforeEach
        void setup() {
            request = LoginRequest.builder()
                    .email(user.getEmail())
                    .password(user.getPassword())
                    .build();
        }

        @Test
        @DisplayName("should successfully authenticate valid user")
        void shouldAuthenticateUser() {
            String token = "dummy-jwt-token-string";

            when(jwtService.generateToken(user)).thenReturn(token);
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

            LoginResponse response = underTest.authenticate(request);

            assertEquals(response.accessToken(), token);

            verify(userRepository, times(1)).findByEmail(request.getEmail());
            verify(jwtService, times(1)).generateToken(user);
            verify(authManager).authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        }

        @Test
        @DisplayName("Should throw exception when user is not found")
        void authenticateWithInvalidEmail() {
            // Arrange
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

            // Act & Assert
            Exception ex = assertThrows(UnauthorizedException.class,
                    () -> underTest.authenticate(request));

            assertEquals("Invalid user credential", ex.getMessage());

            verify(authManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(jwtService, never()).generateToken(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception for invalid credentials")
        void authenticateWithInvalidPassword() {
            // Arrange
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
            doThrow(new BadCredentialsException("Invalid credentials"))
                    .when(authManager)
                    .authenticate(any(UsernamePasswordAuthenticationToken.class));

            // Act & Assert
            assertThrows(BadCredentialsException.class, () -> underTest.authenticate(request));

            verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(jwtService, never()).generateToken(any(User.class));
        }
    }
}