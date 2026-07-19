package com.example.urlshortener.shared.config;

import com.example.urlshortener.domain.url.repository.UrlRepository;
import com.example.urlshortener.domain.url.usecase.SaveVisitUseCase;
import com.example.urlshortener.infrastructure.cache.RedisUrlCacheService;
import com.example.urlshortener.infrastructure.kafka.KafkaAnalyticsAdapter;
import com.example.urlshortener.infrastructure.persistence.repository.CachedUrlRepositoryImpl;
import com.example.urlshortener.infrastructure.persistence.repository.NoopAnalyticsAdapter;
import com.example.urlshortener.infrastructure.persistence.repository.SyncAnalyticsAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.ObjectMapper;

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
    public CachedUrlRepositoryImpl urlRepositoryCached(
            @Qualifier("postgresUrlRepository") UrlRepository postgresUrlRepository,
            RedisUrlCacheService cacheService) {
        return new CachedUrlRepositoryImpl(postgresUrlRepository, cacheService);
    }

    @Bean
    @Profile({"v1", "v2"})
    public NoopAnalyticsAdapter analyticsPortV1V2() {
        return new NoopAnalyticsAdapter();
    }

    @Bean
    @Profile("v3")
    public SyncAnalyticsAdapter analyticsPortV3(SaveVisitUseCase saveVisitUseCase) {
        return new SyncAnalyticsAdapter(saveVisitUseCase);
    }

    @Bean
    @Profile("v4")
    public KafkaAnalyticsAdapter analyticsPortV4(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        return new KafkaAnalyticsAdapter(kafkaTemplate, objectMapper);
    }
}

