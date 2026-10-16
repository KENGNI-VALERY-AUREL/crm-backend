package com.crmapp.backend.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Base type for exceptions that should be turned into a specific HTTP status
 * code and an explicit, English, client-facing message (see
 * {@link GlobalExceptionHandler}). Thrown from services; controllers never
 * build {@link org.springframework.http.ResponseEntity} error responses
 * themselves.
 */
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;

    protected ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

}
