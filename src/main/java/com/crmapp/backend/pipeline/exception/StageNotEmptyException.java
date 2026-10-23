package com.crmapp.backend.pipeline.exception;

import com.crmapp.backend.common.exception.ApiException;
import org.springframework.http.HttpStatus;

/** A stage that still holds prospects cannot be deleted (409). */
public class StageNotEmptyException extends ApiException {

    public StageNotEmptyException() {
        super(HttpStatus.CONFLICT,
                "This stage still contains prospects. Move them to another stage before deleting it.");
    }

}
