package com.example.urlshortener.domain.url.usecase;

import com.example.urlshortener.domain.url.dto.UrlData;
import com.example.urlshortener.domain.url.dto.VisitData;
import com.example.urlshortener.domain.url.exception.NotFoundException;
import com.example.urlshortener.domain.url.repository.AnalyticsPort;
import com.example.urlshortener.domain.url.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RedirectUrlUseCaseImpl implements RedirectUrlUseCase {
    private final UrlRepository urlRepository;
    private final AnalyticsPort analyticsPort;

    @Override
    @Transactional
    public UrlData execute(String shortCode, String ipAddress, String userAgent) {
        UrlData urlData = urlRepository.findByShortCode(shortCode)
            .orElseThrow(() -> new NotFoundException("URL_NOT_FOUND", "URL with code " + shortCode + " not found"));

        // Record visit via output port
        VisitData visitData = new VisitData(
            null,
            shortCode,
            ipAddress,
            userAgent,
            LocalDateTime.now()
        );
        analyticsPort.recordVisit(visitData);

        return urlData;
    }
}
