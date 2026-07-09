package com.example.urlshortener.infrastructure.persistence.repository;

import com.example.urlshortener.domain.url.dto.UrlData;
import com.example.urlshortener.domain.url.repository.UrlRepository;
import com.example.urlshortener.infrastructure.cache.RedisUrlCacheService;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class CachedUrlRepositoryImpl implements UrlRepository {
    private final UrlRepository delegate;
    private final RedisUrlCacheService cacheService;

    @Override
    public UrlData save(UrlData urlData) {
        UrlData saved = delegate.save(urlData);
        cacheService.put(saved);
        return saved;
    }

    @Override
    public Optional<UrlData> findByShortCode(String shortCode) {
        Optional<UrlData> cached = cacheService.get(shortCode);
        if (cached.isPresent()) {
            return cached;
        }

        Optional<UrlData> dbResult = delegate.findByShortCode(shortCode);
        dbResult.ifPresent(cacheService::put);

        return dbResult;
    }

    @Override
    public boolean existsByShortCode(String shortCode) {
        if (cacheService.get(shortCode).isPresent()) {
            return true;
        }
        return delegate.existsByShortCode(shortCode);
    }
}
