package com.example.urlshortener.infrastructure.persistence.repository;

import com.example.urlshortener.infrastructure.persistence.entity.UrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UrlJpaRepository extends JpaRepository<UrlEntity, Long> {
    Optional<UrlEntity> findByShortCode(String shortCode);
    boolean existsByShortCode(String shortCode);
}
