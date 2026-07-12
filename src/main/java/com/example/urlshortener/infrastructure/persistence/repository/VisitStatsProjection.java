package com.example.urlshortener.infrastructure.persistence.repository;

import java.time.LocalDateTime;

public interface VisitStatsProjection {
    String getShortCode();
    long getTotalClicks();
    LocalDateTime getLastClickedAt();
}
