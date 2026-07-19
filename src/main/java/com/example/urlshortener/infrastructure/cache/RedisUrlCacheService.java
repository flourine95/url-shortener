package com.example.urlshortener.infrastructure.cache;

import com.example.urlshortener.domain.url.dto.UrlData;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisUrlCacheService {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String CACHE_PREFIX = "url:";
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    public void put(UrlData urlData) {
        try {
            String json = objectMapper.writeValueAsString(urlData);
            redisTemplate.opsForValue().set(CACHE_PREFIX + urlData.shortCode(), json, CACHE_TTL);
            log.info("Cached URL with code {} in Redis", urlData.shortCode());
        } catch (JacksonException e) {
            log.error("Failed to serialize UrlData for caching: {}", urlData.shortCode(), e);
        }
    }

    public Optional<UrlData> get(String shortCode) {
        String json = redisTemplate.opsForValue().get(CACHE_PREFIX + shortCode);
        if (json == null) {
            return Optional.empty();
        }
        try {
            UrlData urlData = objectMapper.readValue(json, UrlData.class);
            log.info("Cache hit for URL with code {} in Redis", shortCode);
            return Optional.of(urlData);
        } catch (JacksonException e) {
            log.error("Failed to deserialize UrlData from cache: {}", shortCode, e);
            return Optional.empty();
        }
    }

    public void evict(String shortCode) {
        redisTemplate.delete(CACHE_PREFIX + shortCode);
    }
}
