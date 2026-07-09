package com.example.urlshortener.domain.url.dto;

import java.time.LocalDateTime;

public record VisitData(
    Long id,
    String shortCode,
    String ipAddress,
    String userAgent,
    LocalDateTime clickedAt
) {}
