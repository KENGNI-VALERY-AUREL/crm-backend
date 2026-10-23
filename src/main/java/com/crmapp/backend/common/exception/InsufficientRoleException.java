package com.crmapp.backend.common.exception;

import org.springframework.http.HttpStatus;

/** Thrown when an authenticated user's role does not allow the requested action (403). */
public class InsufficientRoleException extends ApiException {

    public InsufficientRoleException() {
        super(HttpStatus.FORBIDDEN, "You do not have permission to perform this action.");
    }

}
