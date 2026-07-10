package com.example.urlshortener.domain.url.exception;

public class UrlNotFoundException extends NotFoundException {
    public UrlNotFoundException(String shortCode) {
        super("URL_NOT_FOUND", "URL with code " + shortCode + " not found");
    }
}
