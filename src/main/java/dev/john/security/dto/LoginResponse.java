package dev.john.security.dto;

import jakarta.validation.constraints.NotNull;

public record LoginResponse(
        String token
) {
}
