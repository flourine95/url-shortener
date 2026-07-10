package com.example.urlshortener.domain.url.exception;

public class ShortCodeAlreadyExistsException extends DomainException {
    public ShortCodeAlreadyExistsException(String shortCode) {
        super("SHORT_CODE_ALREADY_EXISTS", "Short code " + shortCode + " already exists", 409);
    }
}
