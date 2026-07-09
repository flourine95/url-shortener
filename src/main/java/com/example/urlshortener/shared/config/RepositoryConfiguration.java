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

    @Bean
    @Profile("v3")
    public AnalyticsPort analyticsPortV3(com.example.urlshortener.domain.url.usecase.SaveVisitUseCase saveVisitUseCase) {
        return new com.example.urlshortener.infrastructure.persistence.repository.SyncAnalyticsAdapter(saveVisitUseCase);
    }

    @Bean
    @Profile("v4")
    public AnalyticsPort analyticsPortV4(
            org.springframework.kafka.core.KafkaTemplate<String, String> kafkaTemplate,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        return new com.example.urlshortener.infrastructure.kafka.KafkaAnalyticsAdapter(kafkaTemplate, objectMapper);
    }

    @Bean
    public com.fasterxml.jackson.databind.ObjectMapper objectMapper() {
        return new com.fasterxml.jackson.databind.ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }
}
