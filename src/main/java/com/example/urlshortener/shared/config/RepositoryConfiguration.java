package com.example.urlshortener.shared.config;

import com.example.urlshortener.domain.url.repository.AnalyticsPort;
import com.example.urlshortener.infrastructure.persistence.repository.NoopAnalyticsAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfiguration {

    @Bean
    public AnalyticsPort analyticsPort() {
        return new NoopAnalyticsAdapter();
    }
}
