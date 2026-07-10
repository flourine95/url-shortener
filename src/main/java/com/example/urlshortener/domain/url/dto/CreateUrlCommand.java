package com.example.urlshortener.domain.url.dto;

import java.time.LocalDateTime;

public record CreateUrlCommand(
    String originalUrl,
    String customCode,
    LocalDateTime expiresAt
) {
    public CreateUrlCommand(String originalUrl, String customCode) {
        this(originalUrl, customCode, null);
    }
}
