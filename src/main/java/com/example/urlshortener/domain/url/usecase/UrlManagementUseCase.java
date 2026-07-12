package com.example.urlshortener.domain.url.usecase;

import com.example.urlshortener.domain.url.dto.UrlListItem;
import org.springframework.data.domain.Page;

public interface UrlManagementUseCase {
    Page<UrlListItem> list(String q, String status, String sort, int page, int size);
    void delete(String shortCode);
}
