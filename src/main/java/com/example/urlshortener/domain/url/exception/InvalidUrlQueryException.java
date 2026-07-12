package com.example.urlshortener.domain.url.exception;

public class InvalidUrlQueryException extends DomainException {
    public InvalidUrlQueryException(String message) {
        super("INVALID_URL_QUERY", message, 422);
    }
}
