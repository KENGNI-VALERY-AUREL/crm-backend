package com.crmapp.backend.identity.exception;

import com.crmapp.backend.common.exception.ApiException;

import org.springframework.http.HttpStatus;

/** A user-management request that breaks a business rule (400). */
public class InvalidUserOperationException extends ApiException {

    public InvalidUserOperationException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

}
