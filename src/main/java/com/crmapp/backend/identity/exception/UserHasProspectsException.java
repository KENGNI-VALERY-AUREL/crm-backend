package com.crmapp.backend.identity.exception;

import com.crmapp.backend.common.exception.ApiException;

import org.springframework.http.HttpStatus;

/** Prospects of a removed user would silently disappear from every list, so removal is refused (409). */
public class UserHasProspectsException extends ApiException {

    public UserHasProspectsException() {
        super(HttpStatus.CONFLICT, "This user still has prospects assigned. Reassign them before removing the user.");
    }

}
