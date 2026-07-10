package com.example.urlshortener.domain.url.exception;

public class InvalidPaginationException extends DomainException {
    public InvalidPaginationException() {
        super("INVALID_PAGINATION", "Page must be non-negative and size must be between 1 and 100", 422);
    }
}
