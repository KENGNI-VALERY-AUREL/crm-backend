package com.crmapp.backend.common.security;

import com.crmapp.backend.identity.enums.UserRole;
import java.util.UUID;
import lombok.Getter;

/**
 * Multi-tenant isolation (cahier des charges, arbitrage 8.6): the central
 * filtering context for the current request, set once by
 * {@link JwtAuthenticationFilter} and read by every module instead of
 * trusting a free-form organizationId/groupId parameter.
 * <p>
 * Backed by a {@link ThreadLocal}: Tomcat/Spring MVC handles one request per
 * thread by default, so this is safe as long as {@link #clear()} is always
 * called at the end of the request (done in {@link JwtAuthenticationFilter}'s
 * {@code finally} block) — otherwise a pooled thread could leak one user's
 * context into the next request.
 */
@Getter
public final class TenantContext {

    private static final ThreadLocal<TenantContext> CURRENT = new ThreadLocal<>();

    private final UUID userId;
    private final UUID organizationId;
    private final UUID groupId;
    private final UserRole role;

    private TenantContext(UUID userId, UUID organizationId, UUID groupId, UserRole role) {
        this.userId = userId;
        this.organizationId = organizationId;
        this.groupId = groupId;
        this.role = role;
    }

    public static void set(UUID userId, UUID organizationId, UUID groupId, UserRole role) {
        CURRENT.set(new TenantContext(userId, organizationId, groupId, role));
    }

    /**
     * @return the context for the current request, or {@code null} on an
     * unauthenticated endpoint (e.g. {@code /auth/**}).
     */
    public static TenantContext get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }

}
