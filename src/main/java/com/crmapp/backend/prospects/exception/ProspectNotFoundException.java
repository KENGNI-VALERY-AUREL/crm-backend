package com.crmapp.backend.prospects.exception;

import com.crmapp.backend.common.exception.ApiException;
import java.util.UUID;
import org.springframework.http.HttpStatus;

/**
 * Thrown both when a prospect truly does not exist AND when it belongs to a
 * different group than the caller's — same 404 either way, so a request
 * never reveals whether a given ID exists outside the caller's own tenant.
 */
public class ProspectNotFoundException extends ApiException {

    public ProspectNotFoundException(UUID id) {
        super(HttpStatus.NOT_FOUND, "Prospect with id '" + id + "' was not found.");
    }

}
