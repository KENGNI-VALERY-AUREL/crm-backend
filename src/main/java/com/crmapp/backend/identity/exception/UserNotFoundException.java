package com.crmapp.backend.identity.exception;

import com.crmapp.backend.common.exception.ApiException;
import java.util.UUID;
import org.springframework.http.HttpStatus;

/** 404 whether the user does not exist or is outside the caller's reach: never reveals which. */
public class UserNotFoundException extends ApiException {

    public UserNotFoundException(UUID id) {
        super(HttpStatus.NOT_FOUND, "User with id '" + id + "' was not found.");
    }

}
