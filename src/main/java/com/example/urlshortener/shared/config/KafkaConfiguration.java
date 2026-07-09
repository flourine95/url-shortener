package com.example.urlshortener.shared.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Profile("v4")
public class KafkaConfiguration {

    @Bean
    public NewTopic urlAnalyticsTopic() {
        return TopicBuilder.name("url-analytics")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
