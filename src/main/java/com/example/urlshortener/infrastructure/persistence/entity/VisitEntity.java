package com.example.urlshortener.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "visits")
@Getter
@Setter
@NoArgsConstructor
public class VisitEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_code", nullable = false, length = 50)
    private String shortCode;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "clicked_at", nullable = false)
    private Instant clickedAt;

    @PrePersist
    protected void onCreate() {
        if (clickedAt == null) {
            clickedAt = Instant.now();
        }
    }
}
