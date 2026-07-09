package com.example.urlshortener.domain.url.exception;

public class NotFoundException extends DomainException {
    public NotFoundException(String code, String message) {
        super(code, message, 404);
    }
}
