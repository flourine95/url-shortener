package com.example.urlshortener.shared.config;

import com.example.urlshortener.domain.url.repository.AnalyticsPort;
import com.example.urlshortener.domain.url.repository.UrlRepository;
import com.example.urlshortener.infrastructure.cache.RedisUrlCacheService;
import com.example.urlshortener.infrastructure.persistence.repository.CachedUrlRepositoryImpl;
import com.example.urlshortener.infrastructure.persistence.repository.NoopAnalyticsAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
public class RepositoryConfiguration {

    @Bean
    @Profile("v1")
    @Primary
    public UrlRepository urlRepositoryV1(@Qualifier("postgresUrlRepository") UrlRepository postgresUrlRepository) {
        return postgresUrlRepository;
    }

    @Bean
    @Profile({"v2", "v3", "v4"})
    @Primary
    public UrlRepository urlRepositoryCached(
            @Qualifier("postgresUrlRepository") UrlRepository postgresUrlRepository,
            RedisUrlCacheService cacheService) {
        return new CachedUrlRepositoryImpl(postgresUrlRepository, cacheService);
    }

    @Bean
    @Profile({"v1", "v2"})
    public AnalyticsPort analyticsPortV1V2() {
        return new NoopAnalyticsAdapter();
    }
}
