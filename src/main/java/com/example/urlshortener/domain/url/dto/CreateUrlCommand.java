package com.example.urlshortener.domain.url.dto;

import java.time.Instant;

public record CreateUrlCommand(
    String originalUrl,
    String customCode,
    Instant expiresAt
) {
    public CreateUrlCommand(String originalUrl, String customCode) {
        this(originalUrl, customCode, null);
    }
}
