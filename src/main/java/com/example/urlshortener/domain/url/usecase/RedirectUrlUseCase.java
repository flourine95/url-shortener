package com.example.urlshortener.domain.url.usecase;

import com.example.urlshortener.domain.url.dto.UrlData;

public interface RedirectUrlUseCase {
    UrlData execute(String shortCode, String ipAddress, String userAgent);
}
