package com.example.urlshortener.domain.url.repository;

import com.example.urlshortener.domain.url.dto.VisitData;
import com.example.urlshortener.domain.url.dto.UrlStats;

public interface VisitDatabasePort {
    void save(VisitData visitData);
    UrlStats stats(String shortCode);
}
