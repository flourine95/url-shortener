package com.example.urlshortener.domain.url.usecase;

import com.example.urlshortener.domain.url.dto.UrlData;
import org.springframework.data.domain.Page;

public interface UrlManagementUseCase {
    Page<UrlData> list(int page, int size);
    void delete(String shortCode);
}
