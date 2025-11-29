package br.com.hyperativa.service.domain.entity.dto;

import java.util.Objects;

public record UserCreateDTO(String username, String password) {
    public UserCreateDTO {
        Objects.requireNonNull(username, "Username cannot be null");
        Objects.requireNonNull(password, "Password cannot be null");

        if (username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be blank");
        }

        if (password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be blank");
        }
    }
}
