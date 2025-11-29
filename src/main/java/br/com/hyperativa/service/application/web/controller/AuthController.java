package br.com.hyperativa.service.application.web.controller;

import br.com.hyperativa.service.application.config.security.jwt.JwtUtil;
import br.com.hyperativa.service.application.web.controller.request.LoginRequest;
import br.com.hyperativa.service.application.web.controller.response.JwtResponse;
import br.com.hyperativa.service.domain.entity.User;
import br.com.hyperativa.service.domain.entity.dto.UserCreateDTO;
import br.com.hyperativa.service.domain.entity.dto.UserGetDTO;
import br.com.hyperativa.service.domain.exceptions.UserAlreadyExistsException;
import br.com.hyperativa.service.domain.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * REST controller for authentication operations.
 * Provides endpoints for user registration and login.
 */
@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {
    private final UserService userService;

    private final JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(final UserService userService, final JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "User login", description = "Authenticate user and receive JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody @Valid final LoginRequest request) {
        final User user = userService.getUserEntity(request.username());

        if (user.validatePassword(request.password(), passwordEncoder)) {
            return ResponseEntity.ok(new JwtResponse(getToken(request.username())));
        }

        return ResponseEntity.status(UNAUTHORIZED).build();
    }

    @Operation(summary = "Register new user", description = "Create a new user account for API access")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully",
                    content = @Content(schema = @Schema(implementation = UserGetDTO.class))),
            @ApiResponse(responseCode = "400", description = "User already exists or invalid data")
    })
    @PostMapping("/register")
    public ResponseEntity<UserGetDTO> register(@RequestBody @Valid LoginRequest request) {
        if (userService.validateIfUserExists(request.username())) {
            throw new UserAlreadyExistsException(String.format("User [ %s ] already exists", request.username()));
        }

        final UserGetDTO created = userService.createUser(
                new UserCreateDTO(request.username(), getEncodedPassword(request.password()))
        );

        return ResponseEntity.status(CREATED).body(created);
    }

    private String getEncodedPassword(final String password) {
        return passwordEncoder.encode(password);
    }

    private String getToken(final String username) {
        return jwtUtil.generateToken(username);
    }
}
