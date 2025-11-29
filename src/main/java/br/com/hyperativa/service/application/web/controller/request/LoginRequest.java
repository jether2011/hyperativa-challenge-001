package br.com.hyperativa.service.application.web.controller.request;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public record LoginRequest(@NotBlank String username, @NotBlank String password) implements Serializable {
}
