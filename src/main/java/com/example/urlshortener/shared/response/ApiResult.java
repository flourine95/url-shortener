package com.example.urlshortener.shared.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PageMeta meta;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String code;
    private String message;
    private boolean success;

    public ApiResult(T data, String message, boolean success) {
        this(data, null, null, message, success);
    }

    public ApiResult(T data, PageMeta meta, String message, boolean success) {
        this(data, meta, null, message, success);
    }

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(data, null, null, true);
    }

    public static <T> ApiResult<T> ok(T data, String message) {
        return new ApiResult<>(data, null, message, true);
    }

    public static <T> ApiResult<T> page(T data, PageMeta meta, String message) {
        return new ApiResult<>(data, meta, message, true);
    }

    public static <T> ApiResult<T> error(String message) {
        return new ApiResult<>(null, null, null, message, false);
    }

    public static <T> ApiResult<T> error(String code, String message) {
        return new ApiResult<>(null, null, code, message, false);
    }
}
