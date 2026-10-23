package com.crmapp.backend.identity.exception;

import com.crmapp.backend.common.exception.ApiException;
import java.util.UUID;
import org.springframework.http.HttpStatus;

/** 404 whether the group does not exist or is outside the caller's reach. */
public class GroupNotFoundException extends ApiException {

    public GroupNotFoundException(UUID id) {
        super(HttpStatus.NOT_FOUND, "Group with id '" + id + "' was not found.");
    }

}
