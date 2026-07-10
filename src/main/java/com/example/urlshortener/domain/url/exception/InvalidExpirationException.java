package com.example.urlshortener.domain.url.exception;

public class InvalidExpirationException extends DomainException {
    public InvalidExpirationException() {
        super("INVALID_EXPIRATION", "Expiration must be in the future", 422);
    }
}
