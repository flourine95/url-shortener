package com.example.urlshortener.domain.url.repository;

import com.example.urlshortener.domain.url.dto.VisitData;

public interface VisitDatabasePort {
    void save(VisitData visitData);
}
