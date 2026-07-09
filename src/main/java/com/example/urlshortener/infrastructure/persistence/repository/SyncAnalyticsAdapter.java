package com.example.urlshortener.infrastructure.persistence.repository;

import com.example.urlshortener.domain.url.dto.VisitData;
import com.example.urlshortener.domain.url.repository.AnalyticsPort;
import com.example.urlshortener.domain.url.usecase.SaveVisitUseCase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SyncAnalyticsAdapter implements AnalyticsPort {
    private final SaveVisitUseCase saveVisitUseCase;

    @Override
    public void recordVisit(VisitData visitData) {
        saveVisitUseCase.execute(visitData);
    }
}
