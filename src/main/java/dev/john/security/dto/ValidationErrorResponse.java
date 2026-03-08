package dev.john.security.dto;

import java.time.Instant;
import java.util.Map;

public record ValidationErrorResponse(
        String code,
        String message,
        Instant timestamp,
        String path,
        Map<String, String> errors
) {
}
