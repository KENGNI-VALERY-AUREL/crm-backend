package com.crmapp.backend.common.web;

/**
 * Uniform response shape for every endpoint, success or failure, so the
 * mobile client always parses the same structure: {@code success},
 * an explicit English {@code message}, and the {@code data} payload
 * (null on failure, or when there is nothing to return).
 */
public record ApiResponse<T>(boolean success, String message, T data) {

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    public static ApiResponse<Void> failure(String message) {
        return new ApiResponse<>(false, message, null);
    }

}
