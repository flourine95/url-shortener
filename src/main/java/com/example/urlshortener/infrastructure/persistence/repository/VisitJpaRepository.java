package com.example.urlshortener.infrastructure.persistence.repository;

import com.example.urlshortener.infrastructure.persistence.entity.VisitEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VisitJpaRepository extends JpaRepository<VisitEntity, Long> {
    long countByShortCode(String shortCode);
    Optional<VisitEntity> findTopByShortCodeOrderByClickedAtDesc(String shortCode);
}
