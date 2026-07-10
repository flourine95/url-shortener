package com.example.urlshortener.domain.url.usecase;

import com.example.urlshortener.domain.url.dto.UrlStats;
import com.example.urlshortener.domain.url.exception.UrlNotFoundException;
import com.example.urlshortener.domain.url.repository.UrlRepository;
import com.example.urlshortener.domain.url.repository.VisitDatabasePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UrlStatsUseCaseImpl implements UrlStatsUseCase {
    private final UrlRepository urlRepository;
    private final VisitDatabasePort visitDatabasePort;

    @Override
    public UrlStats execute(String shortCode) {
        urlRepository.findByShortCode(shortCode)
            .orElseThrow(() -> new UrlNotFoundException(shortCode));
        return visitDatabasePort.stats(shortCode);
    }
}
