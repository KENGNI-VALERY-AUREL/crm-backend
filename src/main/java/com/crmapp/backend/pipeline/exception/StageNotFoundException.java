package com.crmapp.backend.pipeline.exception;

import com.crmapp.backend.common.exception.ApiException;
import java.util.UUID;
import org.springframework.http.HttpStatus;

/** 404 both when the stage does not exist and when it belongs to another organization. */
public class StageNotFoundException extends ApiException {

    public StageNotFoundException(UUID id) {
        super(HttpStatus.NOT_FOUND, "Pipeline stage with id '" + id + "' was not found.");
    }

}
