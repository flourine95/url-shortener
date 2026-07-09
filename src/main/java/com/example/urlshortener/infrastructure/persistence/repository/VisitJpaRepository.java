package com.example.urlshortener.infrastructure.persistence.repository;

import com.example.urlshortener.infrastructure.persistence.entity.VisitEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitJpaRepository extends JpaRepository<VisitEntity, Long> {
}
