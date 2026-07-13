package com.example.urlshortener.infrastructure.persistence.repository;

import com.example.urlshortener.domain.url.dto.UrlData;
import com.example.urlshortener.domain.url.dto.UrlListItem;
import com.example.urlshortener.domain.url.repository.UrlRepository;
import com.example.urlshortener.infrastructure.persistence.entity.UrlEntity;
import com.example.urlshortener.infrastructure.persistence.mapper.UrlMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Repository("postgresUrlRepository")
@RequiredArgsConstructor
public class PostgresUrlRepositoryImpl implements UrlRepository {
    private final UrlJpaRepository jpaRepository;
    private final UrlMapper urlMapper;
    private final VisitJpaRepository visitJpaRepository;

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
    public Optional<UrlData> findByOriginalUrl(String originalUrl) {
        return jpaRepository.findByOriginalUrl(originalUrl)
            .map(urlMapper::toDomain);
    }

    @Override
    public boolean existsByShortCode(String shortCode) {
        return jpaRepository.existsByShortCode(shortCode);
    }

    @Override
    public Page<UrlListItem> findList(String q, String status, Pageable pageable) {
        Specification<UrlEntity> specification = (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (q != null && !q.isBlank()) {
                String pattern = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("shortCode")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("originalUrl")), pattern)
                ));
            }
            if ("active".equals(status)) {
                predicates.add(criteriaBuilder.or(
                    criteriaBuilder.isNull(root.get("expiresAt")),
                    criteriaBuilder.greaterThan(root.get("expiresAt"), Instant.now())
                ));
            } else if ("expired".equals(status)) {
                predicates.add(criteriaBuilder.and(
                    criteriaBuilder.isNotNull(root.get("expiresAt")),
                    criteriaBuilder.lessThanOrEqualTo(root.get("expiresAt"), Instant.now())
                ));
            }
            return criteriaBuilder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };

        Page<UrlEntity> urls = jpaRepository.findAll(specification, pageable);
        List<String> shortCodes = urls.getContent().stream().map(UrlEntity::getShortCode).toList();
        Map<String, VisitStatsProjection> stats = shortCodes.isEmpty()
            ? Map.of()
            : new HashMap<>(visitJpaRepository.findStatsByShortCodes(shortCodes).stream()
                .collect(java.util.stream.Collectors.toMap(VisitStatsProjection::getShortCode, value -> value)));

        return urls.map(url -> {
            VisitStatsProjection stat = stats.get(url.getShortCode());
            return new UrlListItem(
                url.getShortCode(),
                url.getOriginalUrl(),
                url.getExpiresAt(),
                url.getCreatedAt(),
                stat == null ? 0 : stat.getTotalClicks(),
                stat == null ? null : stat.getLastClickedAt()
            );
        });
    }

    @Override
    public void deleteByShortCode(String shortCode) {
        jpaRepository.findByShortCode(shortCode).ifPresent(jpaRepository::delete);
    }
}
