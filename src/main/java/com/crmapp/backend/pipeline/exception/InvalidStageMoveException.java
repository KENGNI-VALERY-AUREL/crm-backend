package com.crmapp.backend.pipeline.exception;

import com.crmapp.backend.common.exception.ApiException;
import org.springframework.http.HttpStatus;

/** A stage move that breaks a business rule (400), e.g. a LOST move without a reason. */
public class InvalidStageMoveException extends ApiException {

    public InvalidStageMoveException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

}
