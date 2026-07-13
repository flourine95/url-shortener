package com.example.urlshortener.domain.url.dto;

import java.time.Instant;

public record VisitData(
    Long id,
    String shortCode,
    String ipAddress,
    String userAgent,
    Instant clickedAt
) {}
