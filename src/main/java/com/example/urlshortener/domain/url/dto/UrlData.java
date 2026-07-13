package com.example.urlshortener.domain.url.dto;

import java.time.Instant;

public record UrlData(
    Long id,
    String originalUrl,
    String shortCode,
    Instant createdAt,
    Instant updatedAt,
    Instant expiresAt
) {
    public UrlData(Long id, String originalUrl, String shortCode, Instant createdAt, Instant updatedAt) {
        this(id, originalUrl, shortCode, createdAt, updatedAt, null);
    }
}
