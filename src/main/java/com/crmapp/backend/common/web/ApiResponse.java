package com.crmapp.backend.common.web;

import org.springframework.http.HttpStatus;

/**
 * Uniform response shape for every endpoint, success or failure, so the
 * client always parses the same structure: HTTP {@code code}, an explicit
 * English {@code message}, and the {@code data} payload (null on failure,
 * or when there is nothing to return).
 */
public record ApiResponse<T>(int code, String message, T data) {

    public static <T> ApiResponse<T> success(HttpStatus status, String message, T data) {
        return new ApiResponse<>(status.value(), message, data);
    }

    public static ApiResponse<Void> success(HttpStatus status, String message) {
        return new ApiResponse<>(status.value(), message, null);
    }

    public static ApiResponse<Void> failure(HttpStatus status, String message) {
        return new ApiResponse<>(status.value(), message, null);
    }

}
