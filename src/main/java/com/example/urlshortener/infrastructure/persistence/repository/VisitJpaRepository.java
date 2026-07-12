package com.example.urlshortener.infrastructure.persistence.repository;

import com.example.urlshortener.infrastructure.persistence.entity.VisitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VisitJpaRepository extends JpaRepository<VisitEntity, Long> {
    long countByShortCode(String shortCode);
    Optional<VisitEntity> findTopByShortCodeOrderByClickedAtDesc(String shortCode);

    @Query("""
        select v.shortCode as shortCode, count(v.id) as totalClicks, max(v.clickedAt) as lastClickedAt
        from VisitEntity v
        where v.shortCode in :shortCodes
        group by v.shortCode
        """)
    List<VisitStatsProjection> findStatsByShortCodes(@Param("shortCodes") List<String> shortCodes);
}
