package com.example.urlshortener.domain.url.exception;

public class UrlExpiredException extends DomainException {
    public UrlExpiredException(String shortCode) {
        super("URL_EXPIRED", "URL with code " + shortCode + " has expired", 410);
    }
}
