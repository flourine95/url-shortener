package com.example.urlshortener.infrastructure.persistence.repository;

import java.time.Instant;

public record VisitStats(
    String shortCode,
    long totalClicks,
    Instant lastClickedAt
) {}
