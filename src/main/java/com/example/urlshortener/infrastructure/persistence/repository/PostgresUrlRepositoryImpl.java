package com.example.urlshortener.infrastructure.persistence.repository;

import com.example.urlshortener.domain.url.dto.UrlData;
import com.example.urlshortener.domain.url.repository.UrlRepository;
import com.example.urlshortener.infrastructure.persistence.entity.UrlEntity;
import com.example.urlshortener.infrastructure.persistence.mapper.UrlMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("postgresUrlRepository")
@RequiredArgsConstructor
public class PostgresUrlRepositoryImpl implements UrlRepository {
    private final UrlJpaRepository jpaRepository;
    private final UrlMapper urlMapper;

    @Override
    public UrlData save(UrlData urlData) {
        UrlEntity entity = urlMapper.toEntity(urlData);
        UrlEntity saved = jpaRepository.save(entity);
        return urlMapper.toDomain(saved);
    }

    @Override
    public Optional<UrlData> findByShortCode(String shortCode) {
        return jpaRepository.findByShortCode(shortCode)
            .map(urlMapper::toDomain);
    }

    @Override
    public boolean existsByShortCode(String shortCode) {
        return jpaRepository.existsByShortCode(shortCode);
    }
}
