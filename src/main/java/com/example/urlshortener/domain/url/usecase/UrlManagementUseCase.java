package com.example.urlshortener.domain.url.usecase;

import com.example.urlshortener.domain.url.dto.UrlListItem;
import com.example.urlshortener.domain.url.exception.InvalidPaginationException;
import com.example.urlshortener.domain.url.exception.InvalidUrlQueryException;
import com.example.urlshortener.domain.url.exception.UrlNotFoundException;
import com.example.urlshortener.domain.url.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UrlManagementUseCase {
    private final UrlRepository urlRepository;

    public Page<UrlListItem> list(String q, String status, String sort, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new InvalidPaginationException();
        }
        String normalizedStatus = normalizeStatus(status);
        return urlRepository.findList(q, normalizedStatus, PageRequest.of(page, size, parseSort(sort)));
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        String normalized = status.trim().toLowerCase(Locale.ROOT);
        if (!"active".equals(normalized) && !"expired".equals(normalized)) {
            throw new InvalidUrlQueryException("Status must be active or expired");
        }
        return normalized;
    }

    private Sort parseSort(String sort) {
        String[] parts = (sort == null || sort.isBlank() ? "createdAt,desc" : sort).split(",", 2);
        if (parts.length != 2) {
            throw new InvalidUrlQueryException("Sort must use the format field,direction");
        }
        String property = parts[0].trim();
        if (!Set.of("createdAt", "updatedAt", "originalUrl", "shortCode", "expiresAt").contains(property)) {
            throw new InvalidUrlQueryException("Unsupported sort field");
        }
        Sort.Direction direction = Sort.Direction.fromOptionalString(parts[1].trim())
            .orElseThrow(() -> new InvalidUrlQueryException("Sort direction must be asc or desc"));
        return Sort.by(direction, property);
    }

    public void delete(String shortCode) {
        urlRepository.findByShortCode(shortCode)
            .orElseThrow(() -> new UrlNotFoundException(shortCode));
        urlRepository.deleteByShortCode(shortCode);
    }
}
