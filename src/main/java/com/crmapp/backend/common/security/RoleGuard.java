package com.crmapp.backend.common.security;

import com.crmapp.backend.common.exception.InsufficientRoleException;
import com.crmapp.backend.identity.enums.UserRole;
import java.util.Arrays;

/**
 * Explicit role check for services. Used instead of {@code @PreAuthorize}
 * because {@link com.crmapp.backend.common.exception.GlobalExceptionHandler}
 * catches every {@code Exception}: a Spring Security
 * {@code AccessDeniedException} thrown from a controller would be turned
 * into a 500 instead of a 403.
 */
public final class RoleGuard {

    private RoleGuard() {
    }

    public static void requireAnyRole(UserRole... allowed) {
        TenantContext tenant = TenantContext.get();
        if (tenant == null || !Arrays.asList(allowed).contains(tenant.getRole())) {
            throw new InsufficientRoleException();
        }
    }

}
