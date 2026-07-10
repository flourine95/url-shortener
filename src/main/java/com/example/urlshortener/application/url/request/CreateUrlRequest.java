package com.example.urlshortener.application.url.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateUrlRequest(
    @NotBlank(message = "Original URL is required")
    @Size(max = 2048, message = "Original URL must be at most 2048 characters")
    String originalUrl,

    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,50}$", message = "Custom code must be alphanumeric and between 3 and 50 characters")
    String customCode,

    LocalDateTime expiresAt
) {}
