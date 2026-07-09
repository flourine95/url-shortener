package com.example.urlshortener.domain.url.exception;

import lombok.Getter;

@Getter
public abstract class DomainException extends RuntimeException {
    private final String code;
    private final int status;

    protected DomainException(String code, String message, int status) {
        super(message);
        this.code = code;
        this.status = status;
    }
}
