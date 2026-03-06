package dev.john.security.dto;

import lombok.NonNull;

public record RegisterRequest(
        @NonNull String username,
        @NonNull String password,
        @NonNull String email
) {
}
