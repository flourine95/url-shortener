package com.example.urlshortener.domain.url.usecase;

import com.example.urlshortener.domain.url.dto.UrlStats;

public interface UrlStatsUseCase {
    UrlStats execute(String shortCode);
}
