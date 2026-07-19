package com.example.urlshortener.infrastructure.persistence.repository;

import com.example.urlshortener.domain.url.dto.UrlStats;
import com.example.urlshortener.domain.url.dto.VisitData;
import com.example.urlshortener.domain.url.repository.VisitDatabasePort;
import com.example.urlshortener.infrastructure.persistence.entity.VisitEntity;
import com.example.urlshortener.infrastructure.persistence.mapper.VisitMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostgresVisitDatabaseAdapter implements VisitDatabasePort {
    private final VisitJpaRepository visitJpaRepository;
    private final VisitMapper visitMapper;

    @Override
    public void save(VisitData visitData) {
        VisitEntity entity = visitMapper.toEntity(visitData);
        visitJpaRepository.save(entity);
    }

    @Override
    public UrlStats stats(String shortCode) {
        return new UrlStats(
            shortCode,
            visitJpaRepository.countByShortCode(shortCode),
            visitJpaRepository.findTopByShortCodeOrderByClickedAtDesc(shortCode)
                .map(VisitEntity::getClickedAt)
                .orElse(null)
        );
    }
}
