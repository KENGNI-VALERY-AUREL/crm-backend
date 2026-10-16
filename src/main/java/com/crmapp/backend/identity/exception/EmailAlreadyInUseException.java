package com.crmapp.backend.identity.exception;

import com.crmapp.backend.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class EmailAlreadyInUseException extends ApiException {

    public EmailAlreadyInUseException(String email) {
        super(HttpStatus.CONFLICT, "An account with email '" + email + "' already exists.");
    }

}
