package br.com.hyperativa.service.application.web.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CardRequest(@NotBlank @Size(max = 16, min = 16) String cardNumber) {
}
