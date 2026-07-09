package com.example.urlshortener.domain.url.repository;

import com.example.urlshortener.domain.url.dto.UrlData;
import java.util.Optional;

public interface UrlRepository {
    UrlData save(UrlData urlData);
    Optional<UrlData> findByShortCode(String shortCode);
    boolean existsByShortCode(String shortCode);
}
