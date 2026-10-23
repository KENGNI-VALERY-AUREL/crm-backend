package com.crmapp.backend.pipeline.exception;

import com.crmapp.backend.common.exception.ApiException;
import java.util.UUID;
import org.springframework.http.HttpStatus;

/** 404 both when the pipeline does not exist and when it belongs to another organization. */
public class PipelineNotFoundException extends ApiException {

    private PipelineNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

    public static PipelineNotFoundException forId(UUID id) {
        return new PipelineNotFoundException("Pipeline with id '" + id + "' was not found.");
    }

    public static PipelineNotFoundException forOrganization(UUID organizationId) {
        return new PipelineNotFoundException(
                "No pipeline is configured for organization '" + organizationId + "'.");
    }

}
