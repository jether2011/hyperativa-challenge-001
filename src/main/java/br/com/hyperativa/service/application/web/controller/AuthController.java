package br.com.hyperativa.service.application.web.controller;

import br.com.hyperativa.service.application.config.security.jwt.JwtUtil;
import br.com.hyperativa.service.domain.entity.User;
import br.com.hyperativa.service.domain.entity.dto.UserCreateDTO;
import br.com.hyperativa.service.domain.entity.dto.UserGetDTO;
import br.com.hyperativa.service.domain.exceptions.UserAlreadyExistsException;
import br.com.hyperativa.service.domain.services.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {
    private final UserService userService;

    private final JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(final UserService userService, final JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody @Valid final LoginRequest request) {
        final User user = userService.getUserEntity(request.username());

        if (user.validatePassword(request.password(), passwordEncoder)) {
            return ResponseEntity.ok(new JwtResponse(getToken(request.username())));
        }

        return ResponseEntity.status(UNAUTHORIZED).build();
    }

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

record JwtResponse(String token) implements Serializable {
}

record LoginRequest(@NotBlank String username, @NotBlank String password) implements Serializable {
}
