package dev.john.security.dto;

import java.util.List;

public record TokenVerificationResponse(
        String id,
        String email,
        List<String> roles
) {
}
