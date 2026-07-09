package com.example.urlshortener.domain.url.usecase;

import com.example.urlshortener.domain.url.dto.VisitData;

public interface SaveVisitUseCase {
    void execute(VisitData visitData);
}
