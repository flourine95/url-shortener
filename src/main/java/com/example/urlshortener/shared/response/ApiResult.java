package com.example.urlshortener.shared.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResult<T> {
    private T data;
    private String message;
    private boolean success;

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(data, null, true);
    }

    public static <T> ApiResult<T> ok(T data, String message) {
        return new ApiResult<>(data, message, true);
    }

    public static <T> ApiResult<T> error(String message) {
        return new ApiResult<>(null, message, false);
    }
}
