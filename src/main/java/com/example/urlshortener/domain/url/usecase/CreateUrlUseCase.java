package com.example.urlshortener.domain.url.usecase;

import com.example.urlshortener.domain.url.dto.CreateUrlCommand;
import com.example.urlshortener.domain.url.dto.UrlData;

public interface CreateUrlUseCase {
    UrlData execute(CreateUrlCommand command);
}
