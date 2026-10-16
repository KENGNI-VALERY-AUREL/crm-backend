package com.crmapp.backend.common.security;

import com.crmapp.backend.identity.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Reads the "Authorization: Bearer &lt;token&gt;" header on every request,
 * and on success:
 * <ol>
 *   <li>authenticates the request for Spring Security (so
 *       {@code .anyRequest().authenticated()} in {@code SecurityConfig} lets
 *       it through), and</li>
 *   <li>populates {@link TenantContext} so every module can filter its
 *       queries by the caller's organization/group (arbitrage 8.6).</li>
 * </ol>
 * An invalid/expired/missing token simply leaves the request unauthenticated
 * — Spring Security then rejects it with 401 for any endpoint that requires
 * authentication. No exception is thrown here on purpose: {@code /auth/**}
 * must still work with no token at all.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith(BEARER_PREFIX)) {
                String token = header.substring(BEARER_PREFIX.length());
                Claims claims = jwtService.parseClaims(token);

                UUID userId = UUID.fromString(claims.getSubject());
                UUID organizationId = UUID.fromString(claims.get("organizationId", String.class));
                UUID groupId = UUID.fromString(claims.get("groupId", String.class));
                UserRole role = UserRole.valueOf(claims.get("role", String.class));

                TenantContext.set(userId, organizationId, groupId, role);

                var authentication = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (JwtException | IllegalArgumentException ex) {
            // Invalid token: leave the request unauthenticated rather than failing hard here.
            SecurityContextHolder.clearContext();
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Prevents a pooled thread from leaking this request's context into the next one.
            TenantContext.clear();
        }
    }

}
