package com.example.urlshortener.infrastructure.kafka;

import com.example.urlshortener.domain.url.dto.VisitData;
import com.example.urlshortener.domain.url.repository.AnalyticsPort;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@RequiredArgsConstructor
public class KafkaAnalyticsAdapter implements AnalyticsPort {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final String TOPIC = "url-analytics";

    @Override
    public void recordVisit(VisitData visitData) {
        try {
            String json = objectMapper.writeValueAsString(visitData);
            kafkaTemplate.send(TOPIC, visitData.shortCode(), json)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish visit to Kafka for shortCode {}", visitData.shortCode(), ex);
                    } else {
                        log.info("Successfully published visit to Kafka topic {} for shortCode {}", TOPIC, visitData.shortCode());
                    }
                });
        } catch (JacksonException e) {
            log.error("Failed to serialize VisitData for Kafka: {}", visitData.shortCode(), e);
        }
    }
}
