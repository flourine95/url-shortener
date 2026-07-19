package com.example.urlshortener.infrastructure.kafka;

import com.example.urlshortener.domain.url.dto.VisitData;
import com.example.urlshortener.domain.url.usecase.SaveVisitUseCase;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("v4")
@RequiredArgsConstructor
public class KafkaAnalyticsConsumer {
    private final SaveVisitUseCase saveVisitUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "url-analytics", groupId = "url-shortener-analytics-group")
    public void consume(String json) {
        log.info("Received visit event from Kafka: {}", json);
        try {
            VisitData visitData = objectMapper.readValue(json, VisitData.class);
            saveVisitUseCase.execute(visitData);
            log.info("Successfully saved visit record for shortCode {}", visitData.shortCode());
        } catch (JacksonException e) {
            log.error("Failed to deserialize VisitData from Kafka event: {}", json, e);
        }
    }
}
