package com.crmapp.backend.identity.exception;

import com.crmapp.backend.common.exception.ApiException;

import org.springframework.http.HttpStatus;

public class GroupNameAlreadyUsedException extends ApiException {

    public GroupNameAlreadyUsedException(String name) {
        super(HttpStatus.CONFLICT, "A group named '" + name + "' already exists in this organization.");
    }

}
