package com.example.urlshortener.domain.url.dto;

import java.time.Instant;

public record UrlStats(
    String shortCode,
    long totalClicks,
    Instant lastClickedAt
) {}
