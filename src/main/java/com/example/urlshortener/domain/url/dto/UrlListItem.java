package com.example.urlshortener.domain.url.dto;

import java.time.LocalDateTime;

public record UrlListItem(
    String shortCode,
    String originalUrl,
    LocalDateTime expiresAt,
    LocalDateTime createdAt,
    long totalClicks,
    LocalDateTime lastClickedAt
) {}
