package com.crmapp.backend.common.exception;

/**
 * Uniform error body returned by {@link GlobalExceptionHandler}.
 */
public record ErrorResponse(String message) {
}
