package com.crmapp.backend.prospects.exception;

import com.crmapp.backend.common.exception.ApiException;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class ActionNotFoundException extends ApiException {

    public ActionNotFoundException(UUID id) {
        super(HttpStatus.NOT_FOUND, "Action with id '" + id + "' was not found.");
    }

}
