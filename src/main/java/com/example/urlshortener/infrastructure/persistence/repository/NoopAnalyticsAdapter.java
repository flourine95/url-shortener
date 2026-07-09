package com.example.urlshortener.infrastructure.persistence.repository;

import com.example.urlshortener.domain.url.dto.VisitData;
import com.example.urlshortener.domain.url.repository.AnalyticsPort;

public class NoopAnalyticsAdapter implements AnalyticsPort {
    @Override
    public void recordVisit(VisitData visitData) {
        // No-op for v1 and v2
    }
}
