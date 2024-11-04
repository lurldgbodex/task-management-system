package task_management_system.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import task_management_system.config.JwtService;
import task_management_system.exception.BadRequestException;
import task_management_system.user.dto.*;
import task_management_system.user.entity.User;
import task_management_system.user.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public UserDto registerUser(CreateUserRequest request) {
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // validate user does not exist already
        userExist(
                request.getEmail(),
                request.getUsername()
        );

        User newUser = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(hashedPassword)
                .build();

        userRepository.saveAndFlush(newUser);

        return convertToDTO(newUser);
    }

    public LoginResponse authenticate(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid user Credential"));

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        TokenDto token = TokenDto.builder()
                .token(jwtService.generateToken(user))
                .expiresIn(jwtService.getExpirationTime())
                .build();

        return LoginResponse.builder()
                .accessToken(token)
                .user(convertToDTO(user))
                .build();
    }

    private UserDto convertToDTO(User user) {
        return UserDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .enabled(user.isEnabled())
                .accountExpired(!user.isAccountNonExpired())
                .accountLocked(!user.isAccountNonLocked())
                .build();
    }

    private void userExist(String userEmail, String username) {
        Optional<User> email = userRepository.findByEmail(userEmail);
        Optional<User> name = userRepository.findByUsername(username);

        if (email.isPresent()) {
            throw new BadRequestException("user with email already exists");
        }

        if (name.isPresent()) {
            throw new BadRequestException("user with username already exists");
        }
    }
}
