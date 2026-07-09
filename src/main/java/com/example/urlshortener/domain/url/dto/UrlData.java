package com.example.urlshortener.domain.url.dto;

import java.time.LocalDateTime;

public record UrlData(
    Long id,
    String originalUrl,
    String shortCode,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
