package com.example.urlshortener.domain.url.dto;

import java.time.Instant;

public record UrlListItem(
    String shortCode,
    String originalUrl,
    Instant expiresAt,
    Instant createdAt,
    long totalClicks,
    Instant lastClickedAt
) {}
