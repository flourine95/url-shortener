package com.example.urlshortener.domain.url.exception;

public class InvalidOriginalUrlException extends DomainException {
    public InvalidOriginalUrlException() {
        super("INVALID_ORIGINAL_URL", "Original URL must be a valid http or https URL", 422);
    }
}
