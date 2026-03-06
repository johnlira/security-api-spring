package dev.john.security.dto;

import jakarta.validation.constraints.NotNull;

public record LoginRequest (
        @NotNull String email,
        @NotNull String password
){
}
