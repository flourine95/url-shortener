package com.example.urlshortener.domain.url.dto;

public record CreateUrlCommand(
    String originalUrl,
    String customCode
) {}
