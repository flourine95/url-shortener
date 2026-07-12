package com.example.urlshortener.infrastructure.persistence.repository;

import com.example.urlshortener.infrastructure.persistence.entity.UrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UrlJpaRepository extends JpaRepository<UrlEntity, Long>, JpaSpecificationExecutor<UrlEntity> {
    Optional<UrlEntity> findByShortCode(String shortCode);

    @Query(value = """
        SELECT *
        FROM urls
        WHERE original_url_hash = md5(:originalUrl)
          AND original_url = :originalUrl
        LIMIT 1
        """, nativeQuery = true)
    Optional<UrlEntity> findByOriginalUrl(@Param("originalUrl") String originalUrl);

    boolean existsByShortCode(String shortCode);

}
