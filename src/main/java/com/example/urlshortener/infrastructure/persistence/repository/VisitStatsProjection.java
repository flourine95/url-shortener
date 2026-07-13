package com.example.urlshortener.infrastructure.persistence.repository;

import java.time.Instant;

public interface VisitStatsProjection {
    String getShortCode();
    long getTotalClicks();
    Instant getLastClickedAt();
}
