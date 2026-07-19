package com.example.urlshortener.domain.url.repository;

import com.example.urlshortener.domain.url.UrlStatus;
import com.example.urlshortener.domain.url.dto.UrlData;
import com.example.urlshortener.domain.url.dto.UrlListItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UrlRepository {
    UrlData save(UrlData urlData);
    Optional<UrlData> findByShortCode(String shortCode);
    Optional<UrlData> findByOriginalUrl(String originalUrl);
    boolean existsByShortCode(String shortCode);
    Page<UrlListItem> findList(String q, UrlStatus status, Pageable pageable);
    void deleteByShortCode(String shortCode);
}
