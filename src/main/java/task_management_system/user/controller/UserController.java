package task_management_system.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import task_management_system.dto.CustomResponse;
import task_management_system.dto.ValidationException;
import task_management_system.user.dto.CreateUserRequest;
import task_management_system.user.dto.LoginRequest;
import task_management_system.user.dto.LoginResponse;
import task_management_system.user.dto.UserDto;
import task_management_system.user.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "UserController", description = "Controller for managing user authentication")
public class UserController {

    private final UserService userService;

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201", description = "User created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400", description = "User creation fails due to invalid data",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomResponse.class)
                    )
            ),
    })
    @Operation(
            summary = "Create a new user",
            description = "A user is created using the details provided in the parameter"
    )
    @PostMapping(value = "/register", consumes = "application/json", produces = "application/json")
    public ResponseEntity<UserDto> registerUser(
            @Parameter(description = "Details used to create user") @RequestBody @Valid CreateUserRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.registerUser(request));
    }

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "User authenticated successfully",
                    content =  @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class)
                    )
            ),

            @ApiResponse(
                    responseCode = "400", description = "User trying to authenticate without providing need fields",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ValidationException.class)
                    )
            ),

            @ApiResponse(
                    responseCode = "401", description = "user authentication failed due to bad credentials",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomResponse.class)
                    )
            ),
    })
    @Operation(
            summary = "Login/Authenticate User",
            description = "Authenticates a user and generates a jwt token"
    )
    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<LoginResponse> loginUser(
            @Parameter(description = "user data to use to process request") @RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(userService.authenticate(request));
    }
}
