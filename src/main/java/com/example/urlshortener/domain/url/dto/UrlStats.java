package com.example.urlshortener.domain.url.dto;

import java.time.LocalDateTime;

public record UrlStats(
    String shortCode,
    long totalClicks,
    LocalDateTime lastClickedAt
) {}
