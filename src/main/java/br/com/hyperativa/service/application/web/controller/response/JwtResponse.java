package br.com.hyperativa.service.application.web.controller.response;

import java.io.Serializable;

public record JwtResponse(String token) implements Serializable {
}
